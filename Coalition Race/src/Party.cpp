#include "Party.h"
#include "Simulation.h"

Party::Party(int id, string name, int mandates, JoinPolicy *jp) : mId(id), mName(name), mMandates(mandates), mJoinPolicy(jp), mState(Waiting), offers(), timer(0)
{
}

State Party::getState() const
{
    return mState;
}

void Party::setState(State state)
{
    mState = state;
}

int Party::getMandates() const
{
    return mMandates;
}

const string &Party::getName() const
{
    return mName;
}

void Party::step(Simulation &s)
{
    if (mState != Joined)
    {
        if (timer == 2)
        {
            int agentId = mJoinPolicy->join(s, offers);
            mState = Joined;
            s.onPartyJoined(mId, agentId);
        }
        else if (!offers.empty())
        {
            timer++;
        }
    }
}

int Party::getId() const
{
    return mId;
}

const vector<int> &Party::getPartyOffers() const
{
    return offers;
}

void Party::addOffer(int agentId)
{
    offers.push_back(agentId);
    mState = CollectingOffers;
}

Party::~Party()
{
    offers.clear();

    if (mJoinPolicy != nullptr)
    {
        delete mJoinPolicy;
    }
    mJoinPolicy = nullptr;
}

// copy constructor
Party::Party(const Party &other) : mId(other.mId), mName(other.mName), mMandates(other.mMandates), mJoinPolicy(nullptr), mState(other.mState), offers(other.offers), timer(other.timer)
{
    this->mJoinPolicy = other.mJoinPolicy->clone();
}

Party &Party::operator=(const Party &other)
{
    if (this != &other)
    {
        this->mJoinPolicy = other.mJoinPolicy->clone();
        mId = other.mId;
        mName = other.mName;
        mMandates = other.mMandates;
        mState = other.mState;
        offers = other.offers;
        timer = other.timer;
    }

    return *this;
}

// move constructor
Party::Party(Party &&other) : mId(other.mId), mName(other.mName), mMandates(other.mMandates), mJoinPolicy(nullptr), mState(other.mState), offers(other.offers), timer(other.timer)
{
    this->mJoinPolicy = other.mJoinPolicy;
    other.mJoinPolicy = nullptr;
}

// move assignment operator
Party &Party::operator=(Party &&other)
{
    if (this != &other)
    {
        if (mJoinPolicy)
        {
            delete mJoinPolicy;
            mJoinPolicy = nullptr;
        }

        mJoinPolicy = other.mJoinPolicy;
        other.mJoinPolicy = nullptr;
        this->mId = other.mId;
        this->mMandates = other.mMandates;
        this->mName = other.mName;
        this->mState = other.mState;
        this->offers = other.offers;
    }

    return *this;
}