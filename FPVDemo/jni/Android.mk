LOCAL_PATH := $(call my-dir)

CVROOT := C:/OpenCV-android-sdk/sdk/native/jni

include $(CLEAR_VARS)
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=STATIC
include $(CVROOT)/OpenCV.mk


LOCAL_MODULE += KCF4
LOCAL_SRC_FILES +=  kcftracker.cpp fhog.cpp frame3.cpp
LOCAL_LDLIBS += -llog

include $(BUILD_SHARED_LIBRARY)