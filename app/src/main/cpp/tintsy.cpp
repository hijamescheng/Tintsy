// Write C++ code here.
//
// Do not forget to dynamically load the C++ library into your application.
//
// For instance,
//
// In MainActivity.java:
//    static {
//       System.loadLibrary("tintsy");
//    }
//
// Or, in MainActivity.kt:
//    companion object {
//      init {
//         System.loadLibrary("tintsy")
//      }
//    }
#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>

using namespace cv;

extern "C"
JNIEXPORT void JNICALL
Java_dev_hotfix_heros_tintsy_util_OpenCVUtil_applyGrayScaleFilter(
        JNIEnv *env,
        jobject /* this */,
        jstring inputPath,
        jstring outputPath
) {
    const char *input = env->GetStringUTFChars(inputPath, nullptr);
    const char *output = env->GetStringUTFChars(outputPath, nullptr);

// Load the image
    Mat image = imread(input);
    if (image.empty()) {
        env->ReleaseStringUTFChars(inputPath, input);
        env->ReleaseStringUTFChars(outputPath, output);
        return;
    }

// Convert to grayscale
    Mat grayImage;
    cvtColor(image, grayImage, COLOR_BGR2GRAY);

// Save the processed image
    imwrite(output, grayImage);

// Clean up JNI references
    env->ReleaseStringUTFChars(inputPath, input);
    env->ReleaseStringUTFChars(outputPath, output);
}