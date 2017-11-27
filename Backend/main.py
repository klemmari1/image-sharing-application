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
from flask import Flask, redirect, render_template, request, url_for
# from google.cloud import vision

#CLOUD_STORAGE_BUCKET = os.environ.get('CLOUD_STORAGE_BUCKET')

firebaseConfig = {
  "apiKey": "apiKey",
  "authDomain": "mcc-fall-2017-g19.firebaseapp.com",
  "databaseURL": "https://mcc-fall-2017-g19.firebaseio.com/",
  "storageBucket": "gs://mcc-fall-2017-g19.appspot.com/"
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
    return redirect(url_for('create_group', user_id='testuserID', group_name='testgroupName'))

    # Testing: replace group_id with real id from db
    # Real implementation: Group id is passed from application
    #return redirect(url_for('delete_group', group_id='-KziglkrFvdzfQE3wHhQ'))

    #return redirect(url_for('join_group', user_id='testuser2ID', group_id='-KzihPzu45uBnue10kM9'))  # Similar as above


# TODO Change parameters into "methods=['GET'] ..." => request.args.get('param')
@app.route('/create_group/<user_id>/<group_name>')
def create_group(user_id, group_name):
    group_reference = database.child("groups").push({"name": group_name})
    group_key = group_reference["name"]
    database.child("groups").child(group_key).child("members").push({"user": user_id})
    database.child("users").child(user_id).update({"group": group_key})

    # TODO Response: groupID combined with token
    # token = uuid.uuid4()
    # qr_string = group_key + ":" + token

    return "GROUP ADDED"


@app.route('/join_group/<user_id>/<group_id>')
def join_group(user_id, group_id):
    database.child("groups").child(group_id).child("members").push({"user": user_id})
    database.child("users").child(user_id).update({"group": group_id})

    return "JOINED GROUP"


@app.route('/delete_group/<group_id>')
def delete_group(group_id):
    group_members = database.child("groups").child(group_id).child("members").get()
    for member in group_members.each():
        member_id = member.val()["user"]
        database.child("users").child(member_id).child("group").remove()
    database.child("groups").child(group_id).remove()

    return "GROUP DELETED"


@app.route('/get_token', methods=['GET'])
def get_token():
    token = None
    #get group token from firebase db
    #get group ID
    #Combine! (token = group_id:token)
    #(encrypt code)
    return token


@app.route('/verify_token', methods=['POST'])
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


if __name__ == '__main__':
    # This is used when running locally. Gunicorn is used to run the
    # application on Google App Engine. See entrypoint in app.yaml.
    app.run(host='127.0.0.1', port=8080, debug=True)

# [END app]
