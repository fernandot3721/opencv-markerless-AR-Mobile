LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

include /opt/OpenCV-2.4.10-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_MODULE    := mixed_sample
LOCAL_SRC_FILES := jni_part.cpp
LOCAL_LDLIBS += -L$(LOCAL_PATH)/lib -llog -ldl -lnonfree

include $(BUILD_SHARED_LIBRARY)
