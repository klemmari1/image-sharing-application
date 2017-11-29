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
from PIL import Image
from io import BytesIO
from google.cloud import vision
import os

#CLOUD_STORAGE_BUCKET = os.environ.get('CLOUD_STORAGE_BUCKET')

serviceAcc = {
          "type": "service_account",
          "project_id": "mcc-fall-2017-g19",
          "private_key_id": "key",
          "private_key": "-----BEGIN PRIVATE KEY-----\nkey/\nIMYDVHVxNgNWsaRW1tYqQlU7Kx/4wdQ+1VhJ1PJe2UoG3QpbZoktB1ctBLHkVDRL\niuuhkDagDB5Xy2tIzTaqvH1Uqh4dX2AGfOIeaWA85BhzyRvwv2Dh9p+iQHA9EFEK\n6/Vxy7mf6Yvb7QlRyVskhSJmFTBm/JqWZ/KeEraFaGiasfJKG3uBJXQlmVmGH8Ec\n4PeDK3EOM/7ncz/lyzZDR8yU+ap8zkGnGEhekbhrtdMnEROErySxTfsrNq/BG2D8\nGqv7UTvXDkFvY9zlOzVNu2+BEASYzdv1nenfsGoFyroET8SoCR5TJd7YHShbNGIB\n+2MzOfevAgMBAAECggEADD9BoOkb+qBnUDLULeeTjJaGqBAJAyxHpxbj+Bt3bPlK\nvQQAELom8Bc9coZGdidfmaSGqq2Ykz1lM0uaHUfj8INboeTbTgF1ND2tcLuZXaO8\nuq7W3zQFehzu6XUIaaBraP8BaxUH5LB5PxVFX6/XAcaNO7xbvvnP7fHObdwyC4St\nOkou2u7CLdAHzi75rFaNEmgdk9vKo55u5xFrodjNH5tXppPWhS77v7v4dTi3EM+0\nj4f5xYBgVcRyC3kYIfYmKaGq5yp0YjP7wsBsIEvWutQn/OeGfXbCTeb5WS+Jl2/l\nGPhUHnRM+cuiBq2j3wvYEEDueNuReswR4SsVgz9h7QKBgQDlj7yYAA6HXwevUxN5\nC2YESFDYO92a36TH4vPrmmZJdYg8dR2Eg0aJ9BoBNXVKSUrWaL7Nl8aWfJS43PSk\nKZu4ZhfWwH9STjCp+g99rODUuKTrIx0FEXvAb7kl4OdRIZAUrGfrC6xO2bwDAlxe\n34pXoVTWl89wYcPnyKCeIBJJCwKBgQC3kwIQscuqq5PtIjH/22vKQBMviaZDFBZL\n2YnHCVN1iwxIecPInVt8w66g91cjLEDrt4AQEoo/Ey9J4Tf1fc0WbTC9xZckgAy/\nfhVJGR+N5JElB1am+0vj5UiadKXmP0FkAh/lN7vbE83CbamVVUgfZQwjw/jsuvUK\nVafzISlabQKBgQCs2ReF07Uc1L7ykjj9UUnVO6Yzyo/Hh1GJeCd1ZOJTuX2FGCHL\nnxTD1tqlwly4PItu+ZuBLiDHOrK4pxZFbVbk92pHttWnYVxe//weAsefJBB5RA0b\nvdhSQ01Dah6CBiV4i4ALiNSK4oMgOOzYOrTt2noIwnHdCp/5rCTUKw3ZlQKBgDgi\nM9d3BphBrxIsCq36IpPN1BANP1Hzqn23H3JFX8fppO/kjAGkXH1iONvvxi1zMsbh\nbb98a9mtvCATRlFDDpt0+BqPeRAoN722iDu5+vQgmGGCRPT6ktI1ImZYlQI7BXvX\nSnmE+WScQabacajAUzWGaJfnLQ72fEeUua6WzVZhAoGBAKhV50ZuBjYfNoehVtcw\nT23zHaup21s1leDtLKc7lAcEfz916w1kmu2aR0ICJTl0TABwTxO1M4sCeQPeEedG\nN+YCmhs+d3dxM7wYntN9p5XHmr/NTIcI3OPQAKE0U7M/sE59lOj4r2kfLTQ8ceOn\nyVcSlVTNCGNQmaBhD8B+PTHi\n-----END PRIVATE KEY-----\n",
          "client_email": "email",
          "client_id": "key",
          "auth_uri": "https://accounts.google.com/o/oauth2/auth",
          "token_uri": "https://accounts.google.com/o/oauth2/token",
          "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
          "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-pzipw%40mcc-fall-2017-g19.iam.gserviceaccount.com"
        }

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
    try:
        user_id = request.form['user_id']
        user_name = database.child("users").child(user_id).child("name").get().val()

        group_name = request.form['group_name']
        group_reference = database.child("groups").push({"name": group_name})
        group_key = group_reference["name"]
        database.child("groups").child(group_key).child("members").update({user_id: user_name})
        database.child("groups").child(group_key).update({"creator": user_id})

        database.child("users").child(user_id).update({"group": group_key})

        token = str(get_token())
        qr_string = str(group_key) + ":" + str(token)
        database.child("groups").child(group_key).update({"token": qr_string})
        return qr_string
    except:
        return "ERROR"


@app.route('/groups', methods=['GET'])
def get_groups():
    try:
        groups = database.child("groups").get().val()
        return str(dict(groups))
    except:
        return "ERROR"


@app.route('/groups/<group_id>', methods=['GET'])
def get_group_info(group_id):
    try:
        group = database.child("groups").child(group_id).get().val()
        return str(dict(group))
    except:
        return "ERROR"


@app.route('/groups/<group_id>/members', methods=['GET'])
def get_members(group_id):
    try:
        members = database.child("groups").child(group_id).child("members").get().val()
        return str(dict(members))
    except:
        return "ERROR"


@app.route('/groups/<group_id>/members', methods=['POST'])
def add_member(group_id):
    try:
        user_id = request.form['user_id']
        user_name = database.child("users").child(user_id).child("name").get().val()
        database.child("groups").child(group_id).child("members").update({user_id: user_name})
        database.child("users").child(user_id).update({"group": group_id})

        return "JOINED GROUP"
    except:
        return "ERROR"


@app.route('/groups', methods=['DELETE'])
def delete_group():
    try:
        group_id = request.form['group_id']
        group_members = database.child("groups").child(group_id).child("members").get()
        for member in group_members.each():
            member_id = member.val()["user"]
            database.child("users").child(member_id).child("group").remove()
        database.child("groups").child(group_id).remove()

        return "GROUP DELETED"
    except:
        return "ERROR"


@app.route('/users/<user_id>/group', methods=['GET'])
def get_user_group(user_id):
    try:
        group_id = database.child("users").child(user_id).child("group").get().val()
        return get_group_info(group_id)
    except:
        return "ERROR"


@app.route('/users/<user_id>/token', methods=['GET'])
def get_group_token(user_id):
    try:
        group_id = database.child("users").child(user_id).child("group").get().val()
        group_token = database.child("groups").child(group_id).child("token").get().val()
        return group_token
    except:
        return "ERROR"


def get_token():
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
@app.route('/upload_image', methods=['POST'])
def upload_image():
    
    # Get arguments
    args = request.args
    print(args)  # For debugging

    owner = request.form['userID']
    groupID = request.form['groupID']
    filename = request.form['filename']
    maxQuality = request.form['maxQuality']

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
    return "upload_image() ok"  # this will be returned to android if we'll end up using 'GET' I think.


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


if __name__ == '__main__':
    # This is used when running locally. Gunicorn is used to run the
    # application on Google App Engine. See entrypoint in app.yaml.
    app.run(host='127.0.0.1', port=8080, debug=True)

# [END app]
