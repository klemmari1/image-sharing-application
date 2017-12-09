import os
import uuid
import json
import requests
import logging
from io import BytesIO
from datetime import datetime

import pyrebase
import firebase_admin
from firebase_admin import credentials
from firebase_admin import auth
from flask import Flask, redirect, render_template, request, url_for
from google.cloud import vision
from PIL import Image

from pyfcm import FCMNotification


#CLOUD_STORAGE_BUCKET = os.environ.get('CLOUD_STORAGE_BUCKET')


serviceAcc = json.load(open('mcc-fall-2017-g19-firebase-adminsdk-pzipw-d092116b07.json'))

firebaseConfig = {
          "apiKey": "AIzaSyBNrgNc9VQ0FuEJKYMDsocJbMnPSctix3M",
          "authDomain": "mcc_2017_g19.firebaseapp.com",
          "databaseURL": "https://mcc-fall-2017-g19.firebaseio.com/",
          "storageBucket": "mcc-fall-2017-g19.appspot.com",
          "serviceAccount": serviceAcc
        }


firebase = pyrebase.initialize_app(firebaseConfig)
#auth = firebase.auth()
database = firebase.database()
storage = firebase.storage()

app = Flask(__name__)
cred = credentials.Certificate('mcc-fall-2017-g19-firebase-adminsdk-pzipw-d092116b07.json')
default_app = firebase_admin.initialize_app(cred)


#PyFCM Init (note: not really api_key but server_key. works.)
push_service = FCMNotification(api_key="AAAAXcUhfw0:APA91bEKzuxetCY6h08vRpRTGbmFHTAvzuXsFkLL3-vxs3hBF9nrr1nJ7FeUIUACmRbfvMaK93suYOitRLJVU94ENxmfeLivbMtYBMXNeiHikYERHXjVrOcUrTZ6P8qCmakjsYAfPy6m")



#Implement listeners
@app.route('/')
def homepage():
    return "Homepage"
    # return redirect(url_for('create_group', user_id='testuserID', group_name='testgroupName'))

    # Testing: replace group_id with real id from db
    # Real implementation: Group id is passed from application
    #return redirect(url_for('delete_group', group_id='-KziglkrFvdzfQE3wHhQ'))

    #return redirect(url_for('join_group', user_id='testuser2ID', group_id='-KzihPzu45uBnue10kM9'))  # Similar as above


@app.route('/groups', methods=['POST'])
def create_group():
    try:
        id_token = request.form['id_token']
        user_id = get_uid(id_token)
        group_name = request.form['group_name']
        group_expiration = request.form['group_expiration']

        user_name = database.child("users").child(user_id).child("name").get().val()

        group_reference = database.child("groups").push({"name": group_name, "expiration": group_expiration})
        group_key = group_reference["name"]
        database.child("groups").child(group_key).child("members").update({user_id: user_name})
        database.child("groups").child(group_key).update({"creator": user_id})
        database.child("users").child(user_id).update({"group": group_key})

        token = set_new_token(group_key)
        return token
    except Exception as e:
        return "Unexpected error: " + str(e)


@app.route('/groups/join', methods=['POST'])
def join_group():
    try:
        id_token = request.form['id_token']
        joining_token = request.form['token']
        group_id = joining_token.split(":")[0]
        user_id = get_uid(id_token)
        group_token = database.child("groups").child(group_id).child("token").get().val()

        if joining_token == group_token:
            user_name = database.child("users").child(user_id).child("name").get().val()
            database.child("groups").child(group_id).child("members").update({user_id: user_name})
            database.child("users").child(user_id).update({"group": group_id})
            set_new_token(group_id)
            return "JOINED GROUP"
        else:
            return "INVALID GROUP TOKEN!"
    except Exception as e:
        return "Unexpected error: " + str(e)


@app.route('/groups', methods=['DELETE'])
def creator_deletes_group():
    try:
        id_token = request.form['id_token']
        group_id = request.form['group_id']
        user_id = get_uid(id_token)

        if validate_user_group_creator(user_id, group_id):
            delete_group(group_id)
            return "GROUP DELETED"
        else:
            return "INVALID USER TOKEN OR USER NOT GROUP CREATOR!"
    except Exception as e:
        return "Unexpected error: " + str(e)


@app.route('/groups/<group_id>/members', methods=['DELETE'])
def leave_group(group_id):
    try:
        id_token = request.form['id_token']
        user_id = get_uid(id_token)

        if validate_user_in_group(user_id, group_id):
            database.child("groups").child(group_id).child("members").child(user_id).remove()
            database.child("users").child(user_id).child("group").remove()
            return "LEFT GROUP"
        else:
            return "INVALID USER TOKEN OR USER NOT IN GROUP!"
    except Exception as e:
        return "Unexpected error: " + str(e)



def delete_group(group_id):
    group_members = database.child("groups").child(group_id).child("members").get()
    for member in group_members.each():
        member_id = member.key()
        database.child("users").child(member_id).child("group").remove()
    database.child("groups").child(group_id).remove()



def get_uid(id_token):
    decoded_token = auth.verify_id_token(id_token)
    return decoded_token['uid']


def validate_user_in_group(user_id, group_id):
    group = database.child("users").child(user_id).child("group").get().val()
    if group == group_id:
        return True
    else:
        return False


def validate_user_group_creator(user_id, group_id):
    creator_id = database.child("groups").child(group_id).child("creator").get().val()
    if creator_id == user_id:
        return True
    else:
        return False


def set_new_token(group_id):
    token = str(uuid.uuid4())
    qr_string = str(group_id) + ":" + token
    database.child("groups").child(group_id).update({"token": qr_string})
    return qr_string


@app.route('/groups', methods=['GET'])
def delete_expired_groups():
    try:
        groups = database.child("groups")
        for group in groups.get().each():
            expiration_time = group.val()['expiration']
            timestamp = datetime.strptime(expiration_time, '%Y/%m/%d %H:%M:%S')
            if timestamp < datetime.utcnow():
                delete_group(group.key())
        return "EXPIRED GROUPS DELETED"
    except Exception as e:
        return "Unexpected error: " + str(e)


@app.errorhandler(500)
def server_error(e):
    logging.exception('An error occurred during a request.')
    return """
    An internal error occurred: <pre>{}</pre>
    See logs for full stacktrace.
    """.format(e), 500


@app.route('/updateDeviceToken', methods=['POST'])
def updateDeviceToken():
    try:
        args = request.form
    print(args)  # For debugging
    userToken = request.form['userToken']
    deviceToken = request.form['deviceToken']
    userID = get_uid(userToken)

    database.child("users").child(userID).child("deviceTokens").child(deviceToken).set("1");
    return "updateDeviceToken() worked in backend!"

    except Exception as e:
        return "Error in updateDeviceToken(): " + str(e)


'''
UPLOAD IMAGE

input: group-id, user-id, filename, maxQUality
output: group-id, owner, URLs, maxQuality, people 

example urls
http://127.0.0.1:8080/upload_image?userID=Seppo&groupID=someGroupID&filename=anImageUploadedFromAndroid.jpg&maxQuality=high


HTTP POST TO: http://127.0.0.1:8080/upload_image
with paremeters:
userID=<userID>&groupID=<groupID>&filename=<filename>&maxQuality=<low/full/high>
'''
@app.route('/upload_image', methods=['POST'])
def upload_image():
    try:
        # Get arguments
        args = request.form
        print(args)  # For debugging
        token = request.form['id_token']
        userID = get_uid(token)
        groupID = request.form['groupID']
        if validate_user_in_group(userID, groupID):
            filename = request.form['filename']
            maxQuality = request.form['maxQuality']

            urlpath = groupID + "/" + filename
            initialURL = storage.child(urlpath).get_url(0)

            """image_processing() function should generate lower quality pictures and upload them into STORAGE.
            returns URLs and if any people found in google-vision face detection
            """
            URLs, hasFaces = image_processing(initialURL, maxQuality,groupID, filename)

            '''Push to firebase
            '''
            data = {}
            data['userID'] = userID
            data['groupID'] = groupID
            data['maxQuality'] = maxQuality
            if (maxQuality == 'low'):
                data['lowURL'] = URLs[0]
            if (maxQuality == 'high'):
                data['lowURL'] = URLs[1]
                data['highURL'] = URLs[0]
            if (maxQuality == 'full'):
                data['lowURL'] = URLs[2]
                data['highURL'] = URLs[1]
                data['fullURL'] = URLs[0]

            data['hasFaces'] = hasFaces

            token = str(uuid.uuid4())
            database.child("groups").child(groupID).child("images").update({token: data})

            # send notification to all group users that new image has been uploaded
            notification_upload_image(data)

            user_name = database.child("users").child(userID).child("name").get().val()
            user_name = user_name.strip("_")
            return token + "_" + user_name + "_" + str(hasFaces) + "_"
        else:
            return "INVALID USER TOKEN OR USER NOT IN GROUP!"
    except Exception as e:
        return "Unexpected error: " + str(e)


def image_processing(initialURL, maxQuality,groupID, filename):
    URLs = []
    people = 0
    if (maxQuality == 'low'):
        URLs.append(initialURL)
    else:
        r = requests.get(initialURL,verify=False)
        pilImage = Image.open(BytesIO(r.content))
        #pilImage.mode = 'RGBA'

    if (maxQuality == 'high'):
        URLs.append(initialURL)
        URLs.append(img_to_low(pilImage, groupID, filename))
    if (maxQuality == 'full'):
        URLs.append(initialURL)
        URLs.append(img_to_high(pilImage, groupID, filename))
        URLs.append(img_to_low(pilImage, groupID, filename))

    if (check_for_faces(initialURL)):
        people = 1

    return URLs, people

'''resize-functions:
get image, resize, push to STORAGE /<group-id>/images/
return: url to pushed image
'''
def img_to_low(pilImage, groupID, filename):
    pilImage = pilImage.resize((640, 480))
    fname = addLowToFileName(filename)
    pilImage.save(fname, 'JPEG')
    fbpath = groupID + "/" + fname
    storage.child(fbpath).put(fname)
    os.remove(fname)

    return storage.child(fbpath).get_url(0)

def img_to_high(pilImage, groupID, filename):
    pilImage = pilImage.resize((1280, 960))
    fname = addHighToFileName(filename)
    pilImage.save(fname, 'JPEG')
    fbpath = groupID + "/" + fname
    storage.child(fbpath).put(fname)
    os.remove(fname)

    return storage.child(fbpath).get_url(0)

'''check for faces'''
def check_for_faces(url):
    client = vision.ImageAnnotatorClient()
    #url = "gs://mcc-fall-2017-g19.appspot.com/someGroupID/fullQualityWithFaces.jpg"
    #url = "https://auto.ndtvimg.com/car-images/medium/ferrari/gtc4lusso/ferrari-gtc4lusso.jpg?v=11"
    request = {
    'source': {'image_uri': url},
    }

    response = client.face_detection(request)

    if (len(response.face_annotations) > 0):
        return 1
    else:
        return 0

'''helper functions'''
def addLowToFileName(filename):
    split = filename.split(".")
    return split[0] + "Low." + split[1]
def addHighToFileName(filename):
    split = filename.split(".")
    return split[0] + "High." + split[1]


def notification_upload_image(data):
    #for each user in groupID

    all_users = database.child("groups").child(data['groupID']).child("members").get()
    registration_ids = []

    try:
        for user in all_users.each():
            print("found user key: ",user.key())
            print("found user val: ",user.val())
            
            #get device tokens for each user from firebase /users/<uid>/deviceTokens
            tempTokens = database.child("users").child(user.key()).child("deviceTokens").get()
            
            if (tempTokens is None):
                print("tempTokens = None!!!!!!!!")

            try: 
                for token in tempTokens.each():
                    print("found user token: ",token.key())
                    registration_ids.append(token.key())
            except Exception as e:
                print("Unexpected error in for token in tempTokens.each(): " + str(e)) 
    except Exception as e:
        print("no members found from groups/", data['groupID'],"members")
        print(str(e))

    #send data notification to registration_ids. 
    #add the following data: groupID, finalFileName
    
    timestamp = datetime.now().strftime('%d%B%Y%I:%M:%S%p')
    filename = str(data['userID']) + "_" + str(data['hasFaces']) + "_" + str(timestamp)

    data["filename"] = filename + ".jpg"
    photographer = database.child("users").child(data['userID']).child("name").get()
    data["photographer"] = photographer.val()


    for item in registration_ids:
        print("pushing to notification to following devices:", item)

    # message_body = "this is message body string. also data in this message!"
    # message_title = "noti"


    result = push_service.multiple_devices_data_message(registration_ids=registration_ids, data_message=data)

    #todo: with this function we can get valid tokens, 
    #i.e. we can clean up firebase from all of the non-valid ids.
    #valid_registration_ids = push_service.clean_registration_ids(registration_ids)



if __name__ == '__main__':
    # This is used when running locally. Gunicorn is used to run the
    # application on Google App Engine. See entrypoint in app.yaml.
    app.run(host='127.0.0.1', port=8080, debug=True)

# [END app]
