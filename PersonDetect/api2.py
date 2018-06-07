import os
import base64
import json
import pickle as joblib
import numpy as np
import json
import datetime
import cv2
from flask import Flask,render_template,request,jsonify,json


app= Flask(__name__)

APP_ROOT= os.path.dirname(os.path.abspath(__file__))

def find_positions(fimage,confi):
	print "START"
	net = cv2.dnn.readNetFromCaffe('./MobileNetSSD_deploy.prototxt.txt', './MobileNetSSD_deploy.caffemodel')
	image = cv2.imread(fimage)
	(h, w) = image.shape[:2]
	blob = cv2.dnn.blobFromImage(cv2.resize(image, (300, 300)), 0.007843,(300, 300), 127.5)
	net.setInput(blob)
	detections = net.forward()
	print "END"
	positions_x=[]
	positions_y=[]
	positions_w=[]
	positions_h=[]

	for i in np.arange(0, detections.shape[2]):
		confidence = detections[0, 0, i, 2]	
		if confidence > confi:

			idx = int(detections[0, 0, i, 1])
			box = detections[0, 0, i, 3:7] * np.array([w, h, w, h])
			(startX, startY, endX, endY) = box.astype("int")
 
			if(idx==15):
			
				if(startY<0):
					startY=0
				if(startX<0):
					startX=0	
				if(endX>image.shape[1]):
					endX=image.shape[1]
				if(endY>image.shape[0]):
					endY=image.shape[0]

				positions_x.append(float(startX))
				positions_y.append(float(startY))
				positions_w.append(float(endX-startX))
				positions_h.append(float(endY-startY))

	return positions_x,positions_y,positions_w,positions_h			



@app.route('/detect', methods=['GET','POST'])
def android_tool():
	
	content = request.get_json(silent=True)
	image=content.get('image')
	
	with open("test.jpg", "wb") as fh:
		fh.write(base64.b64decode(image))
		
	
	x,y,w,h=find_positions('./test.jpg',0.5)
	
	print (x,y,w,h)
	data={'x_array':x,'y_array':y,'w_array':w ,'h_array':h}

	
	return  jsonify(**data)
	

@app.route('/check', methods=['GET','POST'])
def android_tool1():
	
	content = request.get_json(silent=True)
	image=content.get('image')
	a=content.get('x')
	b=content.get('y')
	c=content.get('w')
	d=content.get('h')
	c=c+a
	d=d+b
        print "GOT THE IMAGE"
	with open("test.jpg", "wb") as fh:
		fh.write(base64.b64decode(image))
	img=cv2.imread('./test.jpg')
	cv2.imwrite("./crop_test.jpg",img[int(b):int(d),int(a):int(c)])	
	
	x,y,w,h=find_positions('./crop_test.jpg',0.5)
	check=0;	
	if(len(x)>0):
		check=1;
		
	print (x,y,w,h)

	data={'result':check}

	return  jsonify(**data)



@app.route('/test', methods=['GET','POST'])
def android_tool2():
	#print (request.data)
	x,y,w,h=find_positions('./test.jpg',0.9)
	image = cv2.imread('./test.jpg')
	print (x,y,w,h)
	
	#for i in range(len(x)):
	#	cv2.imwrite( "./crop"+str(i)+".jpg",image[y[i]:h[i],x[i]:w[i]] );	
 
	data={'x_array':x,'y_array':y,'w_array':w ,'h_array':h}
	
	return  jsonify(**data)
		

#@app.route('/save_data', methods=['GET','POST'])
#def android_tool_helper():
#	content = request.get_json(silent=True)
#	image_array=content.get('Image')
#	name_array=content.get('Name')
#        now = datetime.datetime.now()
#	filehandler = open(str(now)+'-name.obj', 'w') 
#        joblib.dump(name_array, filehandler)
#        filehandler = open(str(now)+'-image.obj', 'w') 
#        joblib.dump(image_array, filehandler)
#	return "Success";	


@app.route("/test0")
def test():
	return "Wroking"



if __name__=="__main__":
	app.run(host='192.168.28.124 ',port=8000, debug=True)
