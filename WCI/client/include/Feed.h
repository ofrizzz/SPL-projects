#pragma once
#include <string>
#include <map>
#include <list>
#include "../include/event.h"

using namespace std;
using std::map;
using std::string;

class Feed
{
private:
public:
    map<int, string> idToTopic;
    map<string, int> topicToId;
    map<string, map<string, list<Event> > > topicToUsers;
    Feed();
    ~Feed();
};
