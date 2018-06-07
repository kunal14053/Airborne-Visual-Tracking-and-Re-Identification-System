import os
from flask import Flask,render_template,request,jsonify
import cv2
import json
import pickle as joblib
import subprocess
import subprocess
import collections
import array
import numpy as np

app= Flask(__name__)

APP_ROOT= os.path.dirname(os.path.abspath(__file__))

save_index=-1;

test_index=-1;

def read_binary_blob(filename):
    
	read_status = 1
	blob = collections.namedtuple('Blob', ['size', 'data'])

	f = open(filename, 'rb')
	s = array.array("i")
	s.fromfile(f, 5)

	if len(s) == 5 :
		m = s[0]*s[1]*s[2]*s[3]*s[4]

        
		data_aux = array.array("f")
		data_aux.fromfile(f, m)
		data = np.array(data_aux.tolist())

		if len(data) != m:
			read_status = 0;

	else:
		read_status = 0;

   
	if not read_status:
		s = []
		blob_data = []
		b = blob(s, blob_data)
		return s, b, read_status

 
	blob_data = np.zeros((s[0], s[1], s[2], s[3], s[4]), np.float32)
	off = 0
	image_size = s[3]*s[4]
	for n in range(0, s[0]):
		for c in range(0, s[1]):
			for l in range(0, s[2]):
                
				tmp = data[np.array(range(off, off+image_size))];
				blob_data[n][c][l][:][:] = tmp.reshape(s[3], -1);
				off = off+image_size;


	b = blob(s, blob_data)
	f.close()    
	return  blob_data



def runcode(total_person):
	
	file1=open("/home/kunal14053/C3D/examples/c3d_feature_extraction/prototxt/input_list_frm.txt","w")
	for i in range(total_person):
		file1.write("input/frm/Person"+str(i+1)+"/ 1 0\n")
	file1.close()

	file2=open("/home/kunal14053/C3D/examples/c3d_feature_extraction/prototxt/output_list_prefix.txt","w")
	for i in range(total_person):
		file2.write("output/c3d/Person"+str(i+1)+"/000001\n")
	file2.close()	
	
	print "HELLOOOOOOOOOOOOOOOOOOOOOOOOOOOOO1"
	id = subprocess.Popen(['sh','/home/kunal14053/C3D/examples/c3d_feature_extraction/c3d_sport1m_feature_extraction_frm.sh'])
	id.wait()
	print "HELLOOOOOOOOOOOOOOOOOOOOOOOOOOOOO2"
	final_array=[]
	
	for i in range(total_person):
		output=read_binary_blob("/home/kunal14053/C3D/examples/c3d_feature_extraction/output/c3d/Person"+str(i+1)+"/000001.fc7-1")
		features=[]
		for i in range(0,4096):
			features.append(output[0][i][0][0][0]);

		arr=np.array(features,order='F')
		final_array.append(arr)	

	filename='./compair'

	matrix=joblib.load(open(filename,'rb'))
	
	min_mat=[]
	index_mat=[]
	for fec in final_array:
		min_fec=[]
		for x in matrix:
			#print np.linalg.norm(fec-x)
			min_fec.append(np.linalg.norm(fec-x))
		min_mat.append(min(min_fec))
		index_mat.append(min_fec.index(min(min_fec)))
	

	feature_index=min_mat.index(min(min_mat))
	
	return feature_index,index_mat[feature_index]
		



def save_image_and_crop(data,name,count):
	with open(name+str(count)+".jpg", "wb") as fh:
		fh.write(data.decode('base64'))
	img = cv2.imread('./'+name+str(count)+'.jpg')
	lst1=name.split('@')
	for x,i in zip(lst1,range(len(lst1))):
		person=x.split(':')
		person=[int(float(j)) for j in person]
		crop_img=img[person[1]:person[1]+person[3], person[0]:person[0]+person[2]]
		if count>9:
			cv2.imwrite("/home/kunal14053/C3D/examples/c3d_feature_extraction/input/frm/Person"+str(i+1)+"/0000"+str(count)+".jpg",crop_img)
		else:
			cv2.imwrite("/home/kunal14053/C3D/examples/c3d_feature_extraction/input/frm/Person"+str(i+1)+"/00000"+str(count)+".jpg",crop_img)	
	
	return len(lst1)



@app.route('/images', methods=['GET','POST'])
def android_tool():
	#print request.data
		
	content = request.get_json(silent=True)
	image_array=content.get('file')
	name_array=content.get('name')
	count=1;
	number=0;
	for i,j in zip(image_array,name_array):
		number=save_image_and_crop(i,j,count)
		count=count+1
	
	a,b= runcode(number)
	data={"id":a, "index":b}
	global save_index;
	save_index=int(b);
	return jsonify(**data)


@app.route('/images_check', methods=['GET','POST'])
def android_tool_helper():
	#print request.data
	content = request.get_json(silent=True)
	image_array=content.get('file')
	name_array=content.get('name')
	count=1;
	number=0;
	for i,j in zip(image_array,name_array):
		number=save_image_and_crop(i,j,count)
		count=count+1
	print("counter: ",count-1);
	a,b= runcode(number)
	data={"id":1}
	return jsonify(**data)


def save_image_and_crop1(name,count):
	
	img = cv2.imread('./'+name+'.jpg')
	lst1=name.split('-')
	for x,i in zip(lst1,range(len(lst1))):
		person=x.split(':')
		person=[int(float(j)) for j in person]
		crop_img=img[person[1]:person[1]+person[3], person[0]:person[0]+person[2]]
		cv2.imwrite("/home/kunal14053/C3D/examples/c3d_feature_extraction/input/frm/Person"+str(i+1)+"/00000"+str(count)+".jpg",crop_img)
	
	return len(lst1)



@app.route('/test1', methods=['GET'])
def test_app():
	name_array=['159.000000:232.000000:150.000000:337.000000-462.000000:139.000000:130.000000:291.000000-308.000000:248.000000:79.000000:179.000000-294.000000:255.000000:83.000000:188.000000-298.000000:246.000000:87.000000:197.000000','159.000000:232.000000:150.000000:337.000000-462.000000:140.000000:130.000000:291.000000-308.000000:248.000000:79.000000:179.000000-296.000000:260.000000:79.000000:179.000000-298.000000:245.000000:87.000000:197.000000','159.000000:233.000000:150.000000:337.000000-461.000000:140.000000:130.000000:291.000000-308.000000:248.000000:79.000000:179.000000-294.000000:255.000000:83.000000:188.000000-298.000000:246.000000:87.000000:197.000000']	
	count=1;
	number=0;
	for i in name_array:
		number=save_image_and_crop1(i,count)
		count=count+1
	
	a,b= runcode(number)
	data={"id":a, "index":b}
	return jsonify(**data)


@app.route('/getIndex', methods=['GET'])
def sendindex_app():
	data={"index":save_index}
	return jsonify(**data)

@app.route('/message', methods=['GET','POST'])
def add_message():
    print request.form['image']	
    with open(request.form['name']+".jpg", "wb") as fh:
    	fh.write(request.form['image'].decode('base64'))
     
    return "Success"


@app.route('/gettestIndex', methods=['GET'])
def sendtestindex_app():
	data={"index":test_index}
	return jsonify(**data)

@app.route("/test")
def test():
	global test_index;
	test_index=10;
	return "Wroking"

if __name__=="__main__":
	app.run(host='192.168.1.187 ',port=8000, debug=True)
