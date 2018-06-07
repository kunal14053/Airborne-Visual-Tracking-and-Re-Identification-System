#include <jni.h>
#include <iostream>
#include <string.h>
#include <algorithm>
#include "opencv2/opencv.hpp"
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/core/core.hpp"
#include "opencv2/highgui/highgui.hpp"
#include "kcftracker.hpp"

using namespace cv;
using namespace std;

extern "C" {

int counter = 0;

KCFTracker tracker(true, true, true, true);

Rect result;

JNIEXPORT jstring
JNICALL Java_com_dji_FPVDemo_MainActivity_KCFTracker2
        (JNIEnv *env, jclass inter, jlong addrmat, jfloat x,jfloat y,jfloat width,jfloat height) {
    Mat &frame = *(Mat *) addrmat;

    if ( counter == 0 ) {
        tracker.init(Rect((float) x, (float) y, (float) width, (float) height), frame);
        rectangle( frame, Point(result.x, result.y), Point(result.x + result.width, result.y + result.height), Scalar(255, 0, 0),1, 8 );
        counter = 1;
        return (*env).NewStringUTF("YES");
    }
    else
    {
        result = tracker.update(frame);
        rectangle( frame, Point(result.x, result.y), Point(result.x + result.width, result.y + result.height), Scalar(255, 0, 0),1, 8 );
        char buf1[64];
        char buf2[64];
        char buf3[64];
        char buf4[64];
        char buff[2048];
        sprintf(buf1, "%f@", (float)result.x);
        sprintf(buf2, "%f@", (float)result.y);
        sprintf(buf3, "%f@", (float)result.width);
        sprintf(buf4, "%f", (float)result.height);
        strcat(buff,buf1);
        strcat(buff,buf2);
        strcat(buff,buf3);
        strcat(buff,buf4);
        return (*env).NewStringUTF(buff);

    }

}

}


