#include <jni.h>
#include <opencv2/opencv.hpp>
#include <android/log.h>
#include <android/bitmap.h>
#include <random>
#include <iostream>

using namespace cv;

// --- Helper: ensure mat is BGR for processing ---
inline cv::Mat toBGR(cv::Mat& mat) {
    cv::Mat bgr;
    if(mat.channels() == 4) cv::cvtColor(mat, bgr, COLOR_RGBA2BGR);
    else bgr = mat;
    return bgr;
}

// --- Helper: convert BGR back to RGBA inplace ---
inline void toRGBA(cv::Mat& src, cv::Mat& dst) {
    if(dst.channels() == 4) cv::cvtColor(src, dst, COLOR_BGR2RGBA);
    else dst = src;
}

// --- Sepia filter (in-place) ---
void filter_sepia(cv::Mat& mat) {
    cv::Mat bgr = toBGR(mat);
    Mat kernel = (Mat_<float>(3,3) <<
                                   0.272, 0.534, 0.131,
            0.349, 0.686, 0.168,
            0.393, 0.769, 0.189);
    cv::transform(bgr, bgr, kernel);
    bgr.convertTo(bgr, CV_8UC3);
    toRGBA(bgr, mat);
}

// --- Vignette filter ---
void filter_vignette(cv::Mat& mat, double strength=0.5) {
    cv::Mat bgr = toBGR(mat);
    int rows = bgr.rows, cols = bgr.cols;
    cv::Mat kernelX(1, cols, CV_32F), kernelY(rows, 1, CV_32F);
    float cx = cols/2.f, cy = rows/2.f;
    float maxDist = std::sqrt(cx*cx + cy*cy);
    for(int x=0;x<cols;x++){
        float dx = (x-cx)/maxDist;
        kernelX.at<float>(0,x) = 1.0f - strength*(dx*dx);
    }
    for(int y=0;y<rows;y++){
        float dy = (y-cy)/maxDist;
        kernelY.at<float>(y,0) = 1.0f - strength*(dy*dy);
    }
    Mat mask = kernelY * kernelX;
    Mat mask3; cv::Mat channels[] = {mask, mask, mask}; merge(channels,3,mask3);
    Mat bgrf; bgr.convertTo(bgrf,CV_32F,1.0/255.0);
    Mat outf = bgrf.mul(mask3);
    outf.convertTo(bgr, CV_8U, 255.0);
    toRGBA(bgr, mat);
}

// --- Film grain filter ---
void filter_grain(cv::Mat& mat, double amount=0.03) {
    cv::Mat bgr = toBGR(mat);
    Mat noise(bgr.size(), CV_32FC3);
    std::mt19937 rng(12345);
    std::normal_distribution<float> dist(0.0f, (float)amount);
    for(int y=0;y<bgr.rows;y++){
        for(int x=0;x<bgr.cols;x++){
            Vec3f n;
            n[0] = dist(rng); n[1] = dist(rng); n[2] = dist(rng);
            noise.at<Vec3f>(y,x) = n;
        }
    }
    Mat bgrf; bgr.convertTo(bgrf,CV_32FC3,1.0/255.0);
    Mat outf = bgrf + noise;
    outf = cv::max(cv::min(outf,1.0f),0.0f);
    outf.convertTo(bgr, CV_8U, 255.0);
    toRGBA(bgr, mat);
}

// --- Warm filter (simple channel adjustment) ---
void filter_filmic(cv::Mat& mat) {
    cv::Mat bgr = toBGR(mat);
    cv::Mat lut(1, 256, CV_8UC1);
    uchar* p = lut.ptr();
    for(int i=0;i<256;i++){
        float x = i/255.0f;
        float y = pow(x,0.9f); // slightly lift shadows
        p[i] = cv::saturate_cast<uchar>(y*255);
    }
    cv::LUT(bgr, lut, bgr);
    toRGBA(bgr, mat);
    filter_vignette(mat, 0.35);
    filter_grain(mat, 0.04);
}

// --- Sepia + vignette + grain composite filter ---
void filter_sepia_composite(cv::Mat& mat) {
    filter_sepia(mat);
    filter_vignette(mat,0.45);
    filter_grain(mat,0.03);
}

extern "C"
JNIEXPORT void JNICALL
Java_dev_lab_crashless_tintsy_util_OpenCVUtil_applyFilterNative(
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
        case 1: cv::GaussianBlur(mat, mat, cv::Size(15,15), 5); break;
        case 2: cv::cvtColor(mat, mat, cv::COLOR_RGBA2BGR); break;
        case 3: filter_sepia_composite(mat); break;
        case 4: filter_vignette(mat, 0.8); break;
        case 5: filter_grain(mat, 0.1); break;
        case 6: filter_filmic(mat); break;
        // Add more filters here
    }

    AndroidBitmap_unlockPixels(env, bitmap);
}