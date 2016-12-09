//
// Created by tangjp on 16-12-9.
//

#ifndef OPENCV_MARKERLESS_AR_MOBILE_PERFORMANCEANALYZER_H
#define OPENCV_MARKERLESS_AR_MOBILE_PERFORMANCEANALYZER_H

#include <iostream>
#include <ctime>

class PerformanceAnalyzer {
public:
    PerformanceAnalyzer() { clock_gettime(CLOCK_REALTIME, &beg_); }

    double elapsed() {
        clock_gettime(CLOCK_REALTIME, &end_);
        return (end_.tv_sec - beg_.tv_sec +
                (end_.tv_nsec - beg_.tv_nsec) / 1000000000.) * 1000;
    }

    void reset() { clock_gettime(CLOCK_REALTIME, &beg_); }

private:
    timespec beg_, end_;
};

#endif //OPENCV_MARKERLESS_AR_MOBILE_PERFORMANCEANALYZER_H
