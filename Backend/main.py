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

app = Flask(__name__)

#Implement listeners
@app.route('/')
def homepage():
    #return redirect(url_for('create_group', user_id='testuserID'))
    return redirect(url_for('delete_group', group_id='-Kzi83kCssOg_AZ3AR5K'))


@app.route('/create_group/<user_id>')
def create_group(user_id):
    group_id = "testgroupID"  # From request parameter

    groupData = {"name": group_id, "members": user_id}
    database.child("groups").push(groupData)

    userGroupData = {"group": group_id}
    database.child("users").child(user_id).set(userGroupData)

    return "GROUP ADDED"

    # Response: groupID
    # Should never fail, since group creation can only initiate if user is not in group?


@app.route('/delete_group/<group_id>')
def delete_group(group_id):
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
