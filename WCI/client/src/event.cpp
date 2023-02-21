#include "../include/event.h"
#include "../include/json.hpp"
#include <iostream>
#include <fstream>
#include <string>
#include <map>
#include <vector>
#include <sstream>
using json = nlohmann::json;

Event::Event(std::string team_a_name, std::string team_b_name, std::string name, int time,
             std::map<std::string, std::string> game_updates, std::map<std::string, std::string> team_a_updates,
             std::map<std::string, std::string> team_b_updates, std::string discription)
    : team_a_name(team_a_name), team_b_name(team_b_name), name(name),
      time(time), game_updates(game_updates), team_a_updates(team_a_updates),
      team_b_updates(team_b_updates), description(discription)
{
}

// copy constructor:
Event::Event(const Event &other) : team_a_name(other.team_a_name), team_b_name(other.team_b_name), name(other.name),
                             time(other.time), game_updates(other.game_updates), team_a_updates(other.team_a_updates),
                             team_b_updates(other.team_b_updates), description(other.description)
{

}

Event::~Event()
{
}

const std::string &Event::get_team_a_name() const
{
    return this->team_a_name;
}

const std::string &Event::get_team_b_name() const
{
    return this->team_b_name;
}

const std::string &Event::get_name() const
{
    return this->name;
}

int Event::get_time() const
{
    return this->time;
}

const std::map<std::string, std::string> &Event::get_game_updates() const
{
    return this->game_updates;
}

const std::map<std::string, std::string> &Event::get_team_a_updates() const
{
    return this->team_a_updates;
}

const std::map<std::string, std::string> &Event::get_team_b_updates() const
{
    return this->team_b_updates;
}

const std::string &Event::get_discription() const
{
    return this->description;
}

std::string Event::substringUntilNewlineNotTabbed(std::string text) {
    std::string substring = "";
    for (long unsigned i = 0; i < text.size(); i++) {
        if (text[i] == '\n') {
            if (i > 0 && text[i + 1] != '\t') {
                return substring;
            } else {
                substring += text[i];
            }
        } else {
            substring += text[i];
        }
    }
    return substring;
}

 std::map<std::string,std::string> Event::rawEventToMap(std::string text){
    string target = "description:\n";
    if(text.find(target) != std::string::npos){
        text.replace(text.find(target), target.size(), "description:");
    }

    std::map<std::string,std::string> map;
    long unsigned index = 0;
    for(long unsigned i=0;index<text.size();i++){
        string curr = substringUntilNewlineNotTabbed(text.substr(index));

        string key = curr.substr(0,curr.find(":"));
        key.erase(remove(key.begin(), key.end(), '\n'), key.end());
        string value = curr.substr(curr.find(":")+1);

        if(value.size()>0){
            map.insert(std::pair<std::string,std::string>(key,value));
        }

        index += curr.size() +1;
    }

    return map;
}
 std::map<std::string,std::string> Event::rawUpdatesToMap(std::string text){
    text.erase(remove(text.begin(), text.end(), '\t'), text.end());
    return rawEventToMap(text);
}
Event::Event(const std::string &frame_body) : team_a_name(""), team_b_name(""), name(""), time(0), game_updates(), team_a_updates(), team_b_updates(), description("")
{
    std::map <std::string,std::string> map = rawEventToMap(frame_body);
    this->team_a_name = map["team a"];
    this->team_b_name = map["team b"];
    this->name = map["event name"];
    this->time = std::stoi(map["time"]);
    this->description = map["description"];
    this->team_a_updates = rawUpdatesToMap(map["team a updates"]);
    this->team_b_updates = rawUpdatesToMap(map["team b updates"]);
    this->game_updates = rawUpdatesToMap(map["game updates"]);
}

names_and_events parseEventsFile(std::string json_path)
{
    std::ifstream f(json_path);
    if (!f.good()) {
        std::cout << "File does not exist." << std::endl;
    }
    json data = json::parse(f);

    std::string team_a_name = data["team a"];
    std::string team_b_name = data["team b"];

    // run over all the events and convert them to Event objects
    std::vector<Event> events;
    for (auto &event : data["events"])
    {
        std::string name = event["event name"];
        int time = event["time"];
        std::string description = event["description"];
        std::map<std::string, std::string> game_updates;
        std::map<std::string, std::string> team_a_updates;
        std::map<std::string, std::string> team_b_updates;
        for (auto &update : event["general game updates"].items())
        {
            if (update.value().is_string())
                game_updates[update.key()] = update.value();
            else
                game_updates[update.key()] = update.value().dump();
        }

        for (auto &update : event["team a updates"].items())
        {
            if (update.value().is_string())
                team_a_updates[update.key()] = update.value();
            else
                team_a_updates[update.key()] = update.value().dump();
        }

        for (auto &update : event["team b updates"].items())
        {
            if (update.value().is_string())
                team_b_updates[update.key()] = update.value();
            else
                team_b_updates[update.key()] = update.value().dump();
        }
        
        events.push_back(Event(team_a_name, team_b_name, name, time, game_updates, team_a_updates, team_b_updates, description));
    }
    names_and_events events_and_names{team_a_name, team_b_name, events};

    return events_and_names;
}