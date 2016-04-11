#ifndef ComputerVision_jni
#define ComputerVision_jni

#include <jni.h>
#include <string>
#include <sstream>
#include <vector>
#include <math.h>
#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/contrib/detection_based_tracker.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>

#define LOG_TAG "ComputerVision_jni"
#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define LOGE(...) ((void)__android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__))

/* DEBUG MODE FLAG */
#define DEBUG 0

using namespace std;
using namespace cv;

#ifdef __cplusplus
extern "C" {
#endif

/**
*\fn JNIEXPORT long JNICALL Java_com_example_circles_MainActivity_process (JNIEnv *, jclass, jlong, jlong);
*\brief Function called by MainActivity (Java) to process the next frame from the camera
*\brief The returned long value is the number of detected colors (aka number of circles)
*/
JNIEXPORT float JNICALL Java_com_example_circles_MainActivity_process(JNIEnv * jenv, jobject obj, jlong, jlong);

#ifdef __cplusplus
}
#endif


/**
*\fn void find_circles(jlong addrGray, jlong addrRgba, vector<Vec3f>* circles);
*\param addrGray : jlong, gray image's native address
*\param addRgba : jlong, RGBA image's native address
*\param circles :  vector<Vec3f>*, container for results
*\brief Perform a circle detection using Hough Transform
*/
void find_circles(jlong addrGray, jlong addrRgba, /*vector<Point>* approx*/vector<Vec3f>* circles);

/**
*\fn void drawCircles(jlong addrImage, vector<Vec3f>* circles);
*\param addImage : jlong, native address of the image we want to draw circles on
*\param circles :  vector<Vec3f>*, collection of circles we want to draw
*\param colors : circles' colors
*\brief Draws circles on the specified image.
*/
void drawCircles(jlong addrImage, vector<Vec3f>* circles);

/**
*\fn void computeOrders(jlong addrImage, vector<Vec3f>* circles);
*\param addImage : jlong, native address of the image we want to draw circles on
*\param circles :  vector<Vec3f>*, collection of circles
*\param colors : circles' colors
*\brief Print on screen orders to correct device location
*/
float computeOrders(jlong addImage, /*vector<Point>* approx*/vector<Vec3f>* circles);


#endif
