#include <jni.h>
#include <iostream>
#include <string.h>
#include <list>
#include <algorithm>
#include "opencv2/opencv.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "kcftracker.hpp"

using namespace cv;
using namespace std;

extern "C" {


vector<KCFTracker> track;


Rect result;

JNIEXPORT jstring
JNICALL Java_com_dji_FPVDemo_LostActivity_KCFTracker3
        (JNIEnv *env, jclass inter, jlong addrmat, jfloatArray x_arr,jfloatArray y_arr,jfloatArray w_arr,jfloatArray h_arr,jint counter) {
    Mat &frame = *(Mat *) addrmat;


    if ( counter == 0 )
    {

        jsize len = (*env).GetArrayLength(x_arr);
        jfloat *x = (*env).GetFloatArrayElements(x_arr, 0);
        jfloat *y = (*env).GetFloatArrayElements(y_arr, 0);
        jfloat *w = (*env).GetFloatArrayElements(w_arr, 0);
        jfloat *h = (*env).GetFloatArrayElements(h_arr, 0);

            for (int i=0; i<5 && i<len; i++)  //limited the number of people to 5
            {
                track.push_back(new KCFTracker(true, true, true, true));
                int lenu=track.size()-1;
                track[lenu].init( Rect(x[i],y[i],w[i],h[i]), frame ) ;
            }

            if(len>0) {
                //counter=1;
                return (*env).NewStringUTF("YES");
            }
            else{
                //counter=0;
                return (*env).NewStringUTF("NO");
            }
    }
    else
    {

        char full_buff[2048]="";

        for(int i=0;i<track.size();i++) {
            result = track[i].update(frame);
            rectangle(frame, Point(result.x, result.y),
                      Point(result.x + result.width, result.y + result.height), Scalar(255, 0, 0),
                      1, 8);
            char buf1[64]= "";
            char buf2[64]= "";
            char buf3[64]= "";
            char buf4[64]= "";
            char buff[2048]= "";

            sprintf(buf1, "%f:", (float)result.x);
            sprintf(buf2, "%f:", (float)result.y);
            sprintf(buf3, "%f:", (float)result.width);
            sprintf(buf4, "%f@", (float)result.height);

            strcat(buff,buf1);
            strcat(buff,buf2);
            strcat(buff,buf3);
            strcat(buff,buf4);

            strcat(full_buff,buff);

        }

        full_buff[strlen(full_buff) - 1] = '\0';
        return (*env).NewStringUTF(full_buff);

    }

}

}


