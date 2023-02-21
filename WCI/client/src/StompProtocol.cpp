#include "../include/StompProtocol.h"

using std::string;

StompProtocol::StompProtocol() : username(), userFeed(), receiptsIdentifier(), receiptsCounter(), subsIdCounter(), receipt_mtx()
{
    receipt_mtx = new std::mutex();
}
// rule of 5:
// copy constructor:
StompProtocol::StompProtocol(StompProtocol &other) : username(other.username), userFeed(other.userFeed), receiptsIdentifier(other.receiptsIdentifier), receiptsCounter(other.receiptsCounter), subsIdCounter(other.subsIdCounter), receipt_mtx()
{
    receipt_mtx = (new std::mutex());
}

StompProtocol::~StompProtocol()
{
    if (receipt_mtx)
    {
        delete (receipt_mtx);
    }
    receipt_mtx = nullptr;
}

StompProtocol &StompProtocol::operator=(const StompProtocol &other)
{
    if (this != &other)
    {
        receipt_mtx = new std::mutex();
        this->username = other.username;
        this->userFeed = other.userFeed;
        this->receiptsIdentifier = other.receiptsIdentifier;
        this->receiptsCounter = other.receiptsCounter;
        this->subsIdCounter = other.subsIdCounter;
    }
    return *this;
}

// move constructor
StompProtocol::StompProtocol(StompProtocol &&other) : username(other.username), userFeed(other.userFeed), receiptsIdentifier(other.receiptsIdentifier), receiptsCounter(other.receiptsCounter), subsIdCounter(other.subsIdCounter), receipt_mtx(other.receipt_mtx)
{
    //this->receipt_mtx = other.receipt_mtx;
    other.receipt_mtx = nullptr;
}

// Move assignment operator
StompProtocol &StompProtocol::operator=(StompProtocol &&other)
{
    if (this != &other)
    {
        if (receipt_mtx)
        {
            delete receipt_mtx;
            receipt_mtx = nullptr;
        }

        receipt_mtx = other.receipt_mtx;
        other.receipt_mtx = nullptr;
        this->username = other.username;
        this->userFeed = other.userFeed;
        this->receiptsIdentifier = other.receiptsIdentifier;
        this->receiptsCounter = other.receiptsCounter;
        this->subsIdCounter = other.subsIdCounter;
    }

    return *this;
}

std::string StompProtocol::proccessLoginCommand(std::vector<std::string> properties)
{ // propertis[1] = "host:port", [2] = "username" , [3] = "password"
    this->username = properties[2];
    int colonIdx = properties[1].find(':');
    string host = properties[1].substr(colonIdx + 1, properties[1].size());
    // string port = properties[1].substr(0, colonIdx);
    int receipt = receiptsIdGenerator();
    vector<string> commandTokens{"LOGIN", properties[1]}; //[1]=game_name, [2]=subsId

    addReceipt(receipt, commandTokens);

    return "CONNECT\naccept-version:1.2\nhost:stomp.cs.bgu.ac.il\nlogin:" + properties[2] + "\npasscode:" + properties[3] + "\n\n";
}
std::string StompProtocol::proccessJoinCommand(std::vector<std::string> properties)
{ // properties[1] = "game_name"
    int receipt = receiptsIdGenerator();
    int subsId = subsIdGenerator();
    vector<string> commandTokens{"SUBSCRIBE", properties[1], std::to_string(subsId)}; //[1]=game_name, [2]=subsId
    addReceipt(receipt, commandTokens);
    return "SUBSCRIBE\ndestination:" + properties[1] + "\nid:" + std::to_string(subsId) + "\nreceipt:" + std::to_string(receipt) + "\n\n";
}
std::vector<std::string> StompProtocol::proccessReportCommand(std::vector<std::string> properties)
{ // properties[1] = file
    std::ifstream file(properties[1]);
    if (!file.good())
    {
        std::cerr << "File does NOT exists!" << std::endl;
        file.close();
        return vector<string>();
    }
    file.close();

    names_and_events eventFromFile = parseEventsFile(properties[1]);
    string game = eventFromFile.team_a_name + '_' + eventFromFile.team_b_name;
    
    //check if subscribed to game
    if(userFeed.topicToId.find(game) == userFeed.topicToId.end()){
        std::cerr << "You are not subscribed to this game!" << std::endl;
        return vector<string>();
    }
    
    std::vector<string> frames;
    frames.reserve(eventFromFile.events.size());

    string frameHead = "SEND\ndestination:" + game + "\n\nuser: " + this->username + "\nteam a: " + eventFromFile.team_a_name + "\nteam b: " + eventFromFile.team_b_name + "\n";
    for (Event event : eventFromFile.events)
    {
        string gameupdates = "event name:" + event.get_name() + "\ntime: " + std::to_string(event.get_time()) + "\n";

        gameupdates += "general game updates:";

        for (pair<string, string> update : event.get_game_updates())
        {
            gameupdates += "\n\t" + update.first + ":" + update.second;
        }
        gameupdates += "\nteam a updates:";
        for (pair<string, string> update : event.get_team_a_updates())
        {
            gameupdates += "\n\t" + update.first + ":" + update.second;
        }
        gameupdates += "\nteam b updates:";

        for (pair<string, string> update : event.get_team_b_updates())
        {
            gameupdates += "\n\t" + update.first + ":" + update.second;
        }
        gameupdates += "\ndescription:\n" + event.get_discription() + "\n";

        frames.push_back(frameHead + gameupdates);
    }
    return frames;
}
std::string StompProtocol::proccessExitCommand(std::vector<std::string> properties)
{ // properties[1] = "game_name"
    int id = this->userFeed.topicToId[properties[1]];
    int receipt = receiptsIdGenerator();
    vector<string> commandTokens{"UNSUBSCRIBE", std::to_string(id), properties[1]}; //[1]=subsId , [2]=game_name
    addReceipt(receipt, commandTokens);
    return "UNSUBSCRIBE\nid:" + std::to_string(id) + "\nreceipt:" + std::to_string(receipt) + "\n\n";
}
std::string StompProtocol::proccessLogoutCommand(std::vector<std::string> properties)
{ // properties[1] = null
    int receipt = receiptsIdGenerator();
    vector<string> commandTokens{"DISCONNECT"};
    addReceipt(receipt, commandTokens);
    return "DISCONNECT\nreceipt:" + std::to_string(receipt) + "\n\n";
}
std::string StompProtocol::proccessSummaryCommand(std::vector<std::string> properties)
{ // properties[1] = "game_name", [2] = "user to summary", [3] = file
    string topic = properties[1];
    string user = properties[2];
     if(userFeed.topicToId.find(topic) == userFeed.topicToId.end()){
        std::cerr << "You are not subscribed to this game!" << std::endl;
        return "";
    }

    list<Event> events = this->userFeed.topicToUsers[topic][user];

    if (events.empty())
    {
        std::cout << "events is empty" << std::endl;
        return "";
    }

    // get first element in events
    string summary = events.front().get_team_a_name() + " vs " + events.front().get_team_b_name() + "\nGame stats:\nGeneral stats:\n";

    std::map<string, string> general_updates;
    std::map<string, string> team_a_updates;
    std::map<string, string> team_b_updates;

    string events_content = "Game event reports:\n";

    string stats = "";
    for (Event event : events)
    {
        for (std::pair<string, string> update : event.get_game_updates())
        {
            general_updates[update.first] = update.second;
        }
        for (std::pair<string, string> update : event.get_team_a_updates())
        {
            team_a_updates[update.first] = update.second;
        }
        for (std::pair<string, string> update : event.get_team_b_updates())
        {
            team_b_updates[update.first] = update.second;
        }

        events_content += std::to_string(event.get_time()) + " - " + event.get_name() + ":\n\n" + event.get_discription() + "\n\n";
    }

    for (std::pair<string, string> update : general_updates)
    {
        stats += update.first + ": " + update.second + "\n";
    }
    stats += "\n" + events.front().get_team_a_name() + " stats:\n";
    for (std::pair<string, string> update : team_a_updates)
    {
        stats += update.first + ": " + update.second + "\n";
    }

    stats += "\n" + events.front().get_team_b_name() + " stats:\n";
    for (std::pair<string, string> update : team_b_updates)
    {
        stats += update.first + ": " + update.second + "\n";
    }

    stats += "\n";

    summary += stats + events_content;

    std::ofstream out_file(properties[3], std::ios::out | std::ios::trunc);
    if (!out_file.is_open())
    {
        std::cerr << "Error opening file" << std::endl;
        return "";
    }

    out_file << summary << std::endl;
    out_file.close();

    std::cout<< "File written successfully " <<std::endl;

    return "";
}
bool StompProtocol::proccessFrame(std::string frame)
{
    vector<string> lines = splitFrame(frame);
    string command = lines[0];

    if (command == "CONNECTED")
    {
        std::cout << "Login successful" << std::endl;
    }
    else if (command == "RECEIPT")
    {
        string receiptId = lines[1].substr(lines[1].find(":") + 1);

        int id = std::stoi(receiptId);

        receipt_mtx->lock();
        vector<string> commandTokens = receiptsIdentifier[id];
        receipt_mtx->unlock();

        if (commandTokens.size() == 0)
        {
            std::cerr << "ERROR! RECEIPT command received id:" << std::to_string(id) << " - but did not find command!" << std::endl;
            return false;
        }

        if (commandTokens[0] == "SUBSCRIBE")
        {
            int subsId = std::stoi(commandTokens[2]);
            this->userFeed.topicToId[commandTokens[1]] = subsId;
            this->userFeed.idToTopic[subsId] = commandTokens[1];

            std::cout << "Joined channel " << commandTokens[1] << std::endl;
        }
        else if (commandTokens[0] == "UNSUBSCRIBE")
        {
            int subsId = stoi(commandTokens[1]);
            this->userFeed.topicToId.erase(this->userFeed.idToTopic[subsId]);
            this->userFeed.idToTopic.erase(subsId);

            std::cout << "Exited channel " << commandTokens[2] << std::endl;
        }
        else if (commandTokens[0] == "DISCONNECT")
        {
            std::cout << "Logout successful" << std::endl;
            return true;
        }

        receipt_mtx->lock();
        receiptsIdentifier.erase(id);
        receipt_mtx->unlock();
    }
    else if (command == "MESSAGE")
    {
        string header = frame.substr(0, frame.find("\n\n"));
        string body = frame.substr(header.size() + 2);
        vector<string> header_lines = splitFrame(header);
        vector<string> body_lines = splitFrame(body);

        string user = body_lines[0].substr(body_lines[0].find(":") + 2);
        string event_raw = body.substr(body.find("\n") + 1);
        Event event = Event(event_raw);

        string target = "destination:";
        string topic = frame.substr(frame.find(target) + target.size());
        topic = topic.substr(0, topic.find("\n"));
        // string topic = frame.substr(frame.find(target) + target.size(), frame.find("\n") - frame.find(target) - target.size());
        this->userFeed.topicToUsers[topic][user].push_back(event);

        std::cout << "New message received from topic: " << topic << std::endl;
    }
    else if (command == "ERROR")
    {
        string target = "message:";
        string message = frame.substr(frame.find(target) + target.size(), frame.find("\n") - frame.find(target) - target.size());
        std::cout << message << std::endl;

        return true;
    }
    else
    {
        std::cout << "Error: unknown command" << std::endl;

        return true;
    }
    return false;
}

void StompProtocol::clear(std::string newUserName)
{
    username = newUserName;
    userFeed = Feed();
    receiptsIdentifier = map<int, std::vector<std::string>>();
}

void StompProtocol::addReceipt(int id, std::vector<std::string> content)
{
    receipt_mtx->lock();
    receiptsIdentifier[id] = content;
    receipt_mtx->unlock();
}

int StompProtocol::receiptsIdGenerator()
{
    receiptsCounter++;
    return receiptsCounter;
}

int StompProtocol::subsIdGenerator()
{
    subsIdCounter++;
    return subsIdCounter;
}

vector<string> StompProtocol::splitFrame(string frame)
{
    std::vector<std::string> parts;
    boost::split(parts, frame, boost::is_any_of("\n"));
    return parts;
}
