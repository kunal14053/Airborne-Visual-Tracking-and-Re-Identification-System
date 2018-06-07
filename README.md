# Airborne-Visual-Tracking-and-Re-Identification-System

1. FPVDemo contain the code and all the libraries/dependencies that are required to run the Android application

2. For Person Detection, you need to run the api1.py provided in the PersonDetect folder, keep this api and the model and prototxt file(present in the same folder) in one directory. You need flask to run that python script and need to change the IP address in that file according to your machine/server

3. For Person Reidentification you need to download and set up C3D library(https://github.com/facebook/C3D/tree/master/C3D-v1.0) and have to use our model(c3d_drone_finetune_whole_iter_1000) provided in the Re-id folder. After setting up the C3D from the link above place the api2(in Re-id folder) along with our model in C3d/examples/c3d_feature_extraction. For feature extraction from frames you need to run the python script api2.py, need flask and change the IP address in that file according to your machine/server

After successfully running all the python script you can use the application. But note that the android application is made and tested usinf DJI Phantom-4 (drone), so you will require that. 
