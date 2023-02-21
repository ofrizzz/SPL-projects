#include <stdlib.h>
#include "../include/ConnectionHandler.h"
#include "../include/StompProtocol.h"
#include <boost/algorithm/string.hpp>
#include <thread>

using namespace std;


std::vector<std::string> split(const std::string &str)
{
    std::vector<std::string> tokens;
    boost::split(tokens, str, boost::is_any_of(" "), boost::token_compress_on);
    return tokens;
}

int main(int argc, char *argv[])
{

    std::cout << "Starting STOMP client" << std::endl;

    StompProtocol *protocol = new StompProtocol();

    // ConnectionHandler connectionHandler(host, port);
    // std::thread th(&ConnectionHandler::run, &connectionHandler);
    ConnectionHandler *connectionHandler = nullptr;

    std::thread th;
    string username;

    const short bufsize = 1024;
    char buf[bufsize];

    while (1)
    {
        cin.getline(buf, bufsize);
        string line(buf);
        vector<std::string> tokens = split(line);
        string command = tokens[0];

        if (command == "login")
        {
            std::string host_port = tokens[1];
            string host = host_port.substr(0, host_port.find(":"));
            short port = atoi(host_port.substr(host.size() + 1).c_str());

            if (connectionHandler == nullptr || !connectionHandler->isConnectionAvailable())
            { // connection should be initialized...
                if (connectionHandler != nullptr)
                {
                    // connectionHandler->shouldTerminate = true;
                    if (th.joinable()) 
                    {
                        th.join();
                    }
                    delete connectionHandler;
                }
                protocol->clear(tokens[2]);
                connectionHandler = new ConnectionHandler(host, port, *protocol);
                th = std::thread(&ConnectionHandler::run, connectionHandler);
                while (!connectionHandler->isConnectionAvailable())
                {
                    // wait for connection to be available, consider connecting here
                }

                std::cout << "Connection is available" << std::endl;

                string frame = protocol->StompProtocol::proccessLoginCommand(tokens);
                bool success = connectionHandler->sendLine(frame);
                if (!success)
                {
                    std::cout << "”Could not connect to server\n"
                              << std::endl;
                }
            }
            else
            {
                cout << "The client is already logged in, log out before trying again" << endl;
            }
        }
        else if (command == "join")
        {
            if (connectionHandler != nullptr && connectionHandler->isConnectionAvailable())
            {
                string frame = protocol->StompProtocol::proccessJoinCommand(tokens);
                bool success = connectionHandler->sendLine(frame);

                if (!success)
                {
                    std::cout << "error occured in sending.\n"
                              << std::endl;
                }
            }
            else
            {
                std::cout << "server not connected.\n"
                          << std::endl;
            }
        }
        else if (command == "report")
        {
            if (connectionHandler != nullptr && connectionHandler->isConnectionAvailable())
            {
                std::vector<string> frames = protocol->StompProtocol::proccessReportCommand(tokens);
                for (long unsigned i = 0; i < frames.size(); i++)
                {
                    bool success = connectionHandler->sendLine(frames[i]);
                    if (!success)
                    {
                        std::cout << "error occured in sending.\n"
                                  << std::endl;
                        break;
                    }
                }
            }
            else
            {
                std::cout << "server not connected.\n"
                          << std::endl;
            }
        }
        else if (command == "exit")
        {
            if (connectionHandler != nullptr && connectionHandler->isConnectionAvailable())
            {
                string frame = protocol->StompProtocol::proccessExitCommand(tokens);
                bool success = connectionHandler->sendLine(frame);
                if (!success)
                {
                    std::cout << "error occured in sending.\n"
                              << std::endl;
                }
            }
            else
            {
                std::cout << "server not connected.\n"
                          << std::endl;
            }
        }
        else if (command == "summary")
        {
            protocol->proccessSummaryCommand(tokens);
        }
        else if (command == "logout")
        {
            if (connectionHandler != nullptr && connectionHandler->isConnectionAvailable())
            {
                string frame = protocol->proccessLogoutCommand(tokens);
                bool success = connectionHandler->sendLine(frame);
                if (!success)
                {
                    std::cout << "error occured in sending.\n"
                              << std::endl;
                }
            }
            else
            {
                std::cout << "server not connected.\n"
                          << std::endl;
            }
        }
        else
        {
            std::cout << "Invalid input.\n"
                      << std::endl;
        }
    }
    if (protocol)
    {
        delete protocol;
    }
    if (connectionHandler)
    {
        delete connectionHandler;
    }
    std::cout << "Exiting STOMP client" << std::endl;
    th.join();
    return 0;
}