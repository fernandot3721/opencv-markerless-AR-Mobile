//
// Created by tangjp on 16-12-15.
//

#include "performanceAnalyzer.h"
PerformanceAnalyzer* PerformanceAnalyzer::m_instance = NULL;

PerformanceAnalyzer* PerformanceAnalyzer::getInstance() {
    if (m_instance == NULL) {
        m_instance = new PerformanceAnalyzer();
    }
    return m_instance;
}

void PerformanceAnalyzer::tag(const std::string& tag) {
    _msg.clear();
    _tag = tag;
}

void PerformanceAnalyzer::log() {
    clock_gettime(CLOCK_REALTIME, &beg_);
}

void PerformanceAnalyzer::count(const std::string& msg) {
    tmpstream.clear();
    tmpstream.str("");
    tmpstream << "[" << msg << " COSTs]: " << elapsed();
    _msg.push_back(tmpstream.str());
}

void PerformanceAnalyzer::save(const std::string& msg) {
    _msg.push_back(msg);
}

void PerformanceAnalyzer::commit() {
    int count = _msg.size();
    __android_log_print(ANDROID_LOG_ERROR, _tag.c_str(), "===============");
    for (int i = 0; i < count;i++)
    {
        __android_log_print(ANDROID_LOG_ERROR, _tag.c_str(), "%s", _msg.at(i).c_str());
    }
    __android_log_print(ANDROID_LOG_ERROR, _tag.c_str(), " ");
}

double PerformanceAnalyzer::elapsed() {
    clock_gettime(CLOCK_REALTIME, &end_);
    return (end_.tv_sec - beg_.tv_sec +
            (end_.tv_nsec - beg_.tv_nsec) / 1000000000.) * 1000;
}