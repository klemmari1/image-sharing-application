# Copyright 2017 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# [START app]
import pyrebase
import uuid
import logging
from flask import Flask, redirect, render_template, request, url_for
from google.cloud import vision
import json
import requests
from PIL import Image, ImageTk
from io import BytesIO
from google.cloud import vision
import os
# from google.cloud import vision

#CLOUD_STORAGE_BUCKET = os.environ.get('CLOUD_STORAGE_BUCKET')

firebaseConfig = {
          "apiKey": "AIzaSyBNrgNc9VQ0FuEJKYMDsocJbMnPSctix3M",
          "authDomain": "mcc_2017_g19.firebaseapp.com",
          "databaseURL": "https://mcc-fall-2017-g19.firebaseio.com/",
          "storageBucket": "mcc-fall-2017-g19.appspot.com",
          "serviceAccount": "mcc-fall-2017-g19-firebase-adminsdk-pzipw-d092116b07.json"
        }

firebase = pyrebase.initialize_app(firebaseConfig)
#auth = firebase.auth()
database = firebase.database()
storage = firebase.storage()

# For testing purposes
# database.child("users").child('testuserID').set({"name": "testusername"})
# database.child("users").child('testuser2ID').set({"name": "testuser2name"})

app = Flask(__name__)


#Implement listeners
@app.route('/')
def homepage():
    return "Homepage"
    # return redirect(url_for('create_group', user_id='testuserID', group_name='testgroupName'))

    # Testing: replace group_id with real id from db
    # Real implementation: Group id is passed from application
    #return redirect(url_for('delete_group', group_id='-KziglkrFvdzfQE3wHhQ'))

    #return redirect(url_for('join_group', user_id='testuser2ID', group_id='-KzihPzu45uBnue10kM9'))  # Similar as above


# TODO Change parameters into "methods=['GET'] ..." => request.args.get('param')
@app.route('/groups', methods=['POST'])
def create_group():
    user_id = request.form['user_id']
    group_name = request.form['group_name']
    group_reference = database.child("groups").push({"name": group_name})
    group_key = group_reference["name"]
    database.child("groups").child(group_key).child("members").push({"user": user_id})
    database.child("users").child(user_id).update({"group": group_key})

    # TODO Response: groupID combined with token
    token = str(get_token())
    # qr_string = group_key + ":" + token
    return "GROUP ADDED"


@app.route('/groups/<group_id>/members', methods=['POST'])
def add_member(group_id):
    user_id = request.form['user_id']
    database.child("groups").child(group_id).child("members").push({"user": user_id})
    database.child("users").child(user_id).update({"group": group_id})

    return "JOINED GROUP"


@app.route('/groups', methods=['DELETE'])
def delete_group():
    group_id = request.form['group_id']
    group_members = database.child("groups").child(group_id).child("members").get()
    for member in group_members.each():
        member_id = member.val()["user"]
        database.child("users").child(member_id).child("group").remove()
    database.child("groups").child(group_id).remove()

    return "GROUP DELETED"


def get_token():
    token = None
    #get group token from firebase db
    #get group ID
    #Combine! (token = group_id:token)
    #(encrypt code)
    token = uuid.uuid4()
    return token


def verify_token():
    #(Decrypt code)
    #split the group_id and one-use-token
    token_valid = False

    #verify token based on the group_id

    if(token_valid == True):
        newtoken = uuid.uuid4()
        #put to firebase to group of group_id




@app.errorhandler(500)
def server_error(e):
    logging.exception('An error occurred during a request.')
    return """
    An internal error occurred: <pre>{}</pre>
    See logs for full stacktrace.
    """.format(e), 500



'''
UPLOAD IMAGE

input: group-id, user-id, filename, maxQUality
output: group-id, owner, URLs, maxQuality, people 

example urls
http://127.0.0.1:8080/upload_image?owner=Seppo&groupID=someGroupID&filename=anImageUploadedFromAndroid.jpg&maxQuality=high
http://127.0.0.1:8080/upload_image?owner=Seppo&groupID=someGroupID&filename=4kImage.jpg&maxQuality=full
'''
@app.route('/upload_image', methods=['GET'])
def upload_image():
    
    #get arguments
    args = request.args
    print (args) # For debugging

    owner = args.get('userID')
    groupID = args.get('groupID')
    filename = args.get('filename')
    maxQuality = args.get('maxQuality')

    urlpath = groupID + "/" + filename
    initialURL = storage.child(urlpath).get_url(0)


    
    """image_processing() function should generate lower quality pictures and upload them into STORAGE.
    returns URLs and if any people found in google-vision face detection
    """
    URLs, people = image_processing(initialURL, maxQuality,groupID, filename)

    '''Push to firebase
    '''
    data = {}
    data['owner'] = owner
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
    data['people'] = people

    database.child("groups").child(groupID).child("images").push(data)

    print("upload_image() ok")
    return "upload_image() ok" # this will be returned to android if we'll end up using 'GET' I think.


def image_processing(initialURL, maxQuality,groupID, filename):
    URLs = []
    people = 0
    if (maxQuality == 'low'):
        URLs.append(initialURL)
    else:
        r = requests.get(initialURL)
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
    pilImage.save(fname,'JPEG')
    fbpath = groupID + "/" + fname
    storage.child(fbpath).put(fname)
    os.remove(fname)

    return storage.child(fbpath).get_url(0)

def img_to_high(pilImage, groupID, filename):
    pilImage = pilImage.resize((1280, 960))
    fname = addHighToFileName(filename)
    pilImage.save(fname,'JPEG')
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
    return split[0]+ "Low." +split[1]
def addHighToFileName(filename):
    split = filename.split(".")
    return split[0]+ "High." +split[1]


if __name__ == '__main__':
    # This is used when running locally. Gunicorn is used to run the
    # application on Google App Engine. See entrypoint in app.yaml.
    app.run(host='127.0.0.1', port=8080, debug=True)

# [END app]
