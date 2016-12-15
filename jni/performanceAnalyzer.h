//
// Created by tangjp on 16-12-15.
//

#ifndef OPENCV_MARKERLESS_AR_MOBILE_PERFORMANCEANALYZER_H
#define OPENCV_MARKERLESS_AR_MOBILE_PERFORMANCEANALYZER_H

#include <sstream>
#include <iostream>
#include <ctime>
#include <android/log.h>
#include <vector>

class PerformanceAnalyzer {
public:
    static PerformanceAnalyzer* getInstance();

    void tag(const std::string& tag);
    void log();
    void count(const std::string& msg);
    void save(const std::string& msg);
    void commit();

private:
    timespec beg_, end_;
    std::string _tag;

    std::vector<std::string> _msg;
    std::ostringstream tmpstream;

    static PerformanceAnalyzer* m_instance;

    PerformanceAnalyzer(){};
    double elapsed();
};

#endif //OPENCV_MARKERLESS_AR_MOBILE_PERFORMANCEANALYZER_H
