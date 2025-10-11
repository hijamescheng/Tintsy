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
#include <android/bitmap.h>

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

extern "C"
JNIEXPORT void JNICALL
Java_dev_hotfix_heros_tintsy_util_OpenCVUtil_applyFilterNative(
        JNIEnv* env,
        jobject /* this */,
        jobject bitmap,
        jint filterType
) {
    AndroidBitmapInfo info;
    void* pixels = nullptr;

    if (AndroidBitmap_getInfo(env, bitmap, &info) < 0) return;
    if (AndroidBitmap_lockPixels(env, bitmap, &pixels) < 0) return;

    cv::Mat mat(info.height, info.width, CV_8UC4, pixels);

    switch (filterType) {
        case 0: {
            cv::Mat gray;
            cv::cvtColor(mat, gray, cv::COLOR_RGBA2GRAY);
            cv::cvtColor(gray, mat, cv::COLOR_GRAY2RGBA);
            break;
        }
        case 1: cv::GaussianBlur(mat, mat, cv::Size(5,5), 0); break;
        case 2: cv::cvtColor(mat, mat, cv::COLOR_RGBA2BGR); break;
            // Add more filters here
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}