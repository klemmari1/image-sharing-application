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
from flask import Flask, redirect, render_template, request
from google.cloud import vision

#CLOUD_STORAGE_BUCKET = os.environ.get('CLOUD_STORAGE_BUCKET')

app = Flask(__name__)


#Implement listeners

@app.route('/')
def homepage():
   return("Hello world!")


@app.route('/create_group', methods=['GET'])
def create_group():
    user_id = request.args.get('user')
    #create group

    #get group token


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

    if(token_valid == True)
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
