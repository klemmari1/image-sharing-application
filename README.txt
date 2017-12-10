Running the app from command line:

Clone the git repository:  
git clone git@version.aalto.fi:CS-E4100/mcc-2017-g19.git

Navigate to the project folder: cd mcc-2017-g19


Run build script: ./deploy.sh
	  - the script will prompt user for input once to deploy the app (app.yaml), 	
	      and once to deploy the cron job (cron.yaml), press ‘y’ on both times.	      


Manually install the produced APK from app/build/outputs/apk




Project files:
The “AlbumsView” folder contains all the files that we used to create the folder view. Every album has two TextView(name of the album + number of images) and two ImageView(folder image + image of the cloud). All the view is created with one custom adapter, and all the albums are added dynamically. 
Inside the “AlbumsView” is it possible to find another folder called “AlbumEach”. Inside this folder, we have all the views that are shown when the user clicks on one album. More precisely, we are creating the Grid RecyclerView. This is useful in order to dynamically create the views were to show all the images of the album. We have two possibilities to see the pictures: Sorted by people/no people or by authors of the picture. Furthermore, inside the “AlbumEach” is it possible to find the activities related to the private gallery.

Inside the folder “BackendAPI” is it possible to find the endpoints that we are using in order to query our backend (Google cloud engine)

The folder "BackgroundServices" shows the services that we are calling in our activities. For example, the file named "ImageSaveService.java" is responsible for saving the pictures taken by the camera. More precisely, once the user has taken pictures and decided to upload it, we call the service to check if there are sensible data. Based on that, the process goes on changing the size of the picture based on the settings of the user. Finally, it sends the picture on firebase and it calls the endpoint of the backend. There is also the “SyncImagesService” that takes care of downloading the images to the device from other users of the group.

Inside the folder “Connectivity” we have a java class with some functions that help to check if the phone is connected to internet or not, and also if through wifi or mobile data.

All the backend-related files are located in the “Backend” folder. “app.yaml” is used to configure Google App Engine’s settings, whereas the “cron.yaml” defines the background job that periodically deletes expired groups from Firebase.
File “main.py” contains the backend functionality of our project. It is implemented in Python with Flask framework that allows an easy deploying of a simple REST API. It handles all group related database write actions from group creation, joining and deleting to adding image entries to the firebase database. It also does some image processing to the uploaded files with google-vision API.
