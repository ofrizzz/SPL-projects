#pragma once
#include <string>
#include <vector>

#include "JoinPolicy.h"

using std::string;
using std::vector;

class JoinPolicy;
class Simulation;

enum State
{
    Waiting,
    CollectingOffers,
    Joined
};

class Party
{
public:
    Party(int id, string name, int mandates, JoinPolicy *);
    State getState() const;
    void setState(State state);
    int getMandates() const;
    void step(Simulation &s);
    const string &getName() const;
    // OURS:
    const vector<int> &getPartyOffers() const;
    int getId() const;
    void addOffer(int agentId);
    void onPartyJoined();
    virtual ~Party();
    Party(const Party &other);
    Party &operator=(const Party &other);

    Party(Party &&other);
    Party &operator=(Party &&other);

private:
    int mId;
    string mName;
    int mMandates;
    JoinPolicy *mJoinPolicy;
    State mState;
    // changes below:

    vector<int> offers;
    int timer;
};
