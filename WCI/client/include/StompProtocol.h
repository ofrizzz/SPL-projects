#pragma once
#include <string>
#include <vector>
#include "../include/event.h"
#include "../include/Feed.h"
#include <fstream>
#include <map>
#include <mutex>
#include <boost/algorithm/string.hpp>

class StompProtocol
{
private:
    std::string username = "";
    Feed userFeed;
    map<int, std::vector<std::string> > receiptsIdentifier;
    int receiptsCounter = 0;
    int subsIdCounter = 0;
    std::mutex *receipt_mtx;

public:
    StompProtocol();
    // rule of 5:
    StompProtocol(StompProtocol &);
    StompProtocol &operator=(const StompProtocol &other);
    ~StompProtocol();
    StompProtocol(StompProtocol &&other);
    StompProtocol &operator=(StompProtocol &&other);

    std::string proccessLoginCommand(std::vector<std::string>); 
    std::string proccessJoinCommand(std::vector<std::string>);
    std::vector<std::string> proccessReportCommand(std::vector<std::string>); 
    std::string proccessExitCommand(std::vector<std::string>);
    std::string proccessLogoutCommand(std::vector<std::string>);
    std::string proccessSummaryCommand(std::vector<std::string>); 

    void addReceipt(int id, std::vector<std::string>);

    // returns true if client should close connection, false otherwise
    bool proccessFrame(std::string);

    void clear(std::string);

    int receiptsIdGenerator();

    int subsIdGenerator();

    static vector<string> splitFrame(string frame);
};