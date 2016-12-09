//
// Created by tangjp on 16-12-9.
//

#ifndef OPENCV_MARKERLESS_AR_MOBILE_PERFORMANCEANALYZER_H
#define OPENCV_MARKERLESS_AR_MOBILE_PERFORMANCEANALYZER_H

#include <iostream>
#include <ctime>
#include <android/log.h>

class PerformanceAnalyzer {
public:
    PerformanceAnalyzer(const std::string& tag) {
        clock_gettime(CLOCK_REALTIME, &beg_);
        _tag = tag;
    }

    double elapsed() {
        clock_gettime(CLOCK_REALTIME, &end_);
        return (end_.tv_sec - beg_.tv_sec +
                (end_.tv_nsec - beg_.tv_nsec) / 1000000000.) * 1000;
    }

    void reset() { clock_gettime(CLOCK_REALTIME, &beg_); }

    void log() {
        reset();
    }

    void count(const std::string& msg) {
        __android_log_print(ANDROID_LOG_ERROR, _tag.c_str(),
                            "[%s COSTs]: %f", msg.c_str(), elapsed());
    }

private:
    timespec beg_, end_;
    std::string _tag;
};

#endif //OPENCV_MARKERLESS_AR_MOBILE_PERFORMANCEANALYZER_H
