#include "JoinPolicy.h"
#include "Coalition.h"
#include "Simulation.h"

JoinPolicy::~JoinPolicy()
{
}

// MandatesJoinPolicy:

MandatesJoinPolicy::~MandatesJoinPolicy()
{
}

int MandatesJoinPolicy::join(const Simulation &s, const vector<int> &offers)
{
    int maxMandats = 0;
    int maxMandatsAgent;
    for (int ofr : offers)
    {
        Agent ag = s.getAgents().at(ofr);

        const Coalition *current = s.getCoalition(ag.getCoalitionId());

        if (current->getMandats() > maxMandats)
        {
            maxMandats = current->getMandats();
            maxMandatsAgent = ofr;
        }
    }

    return maxMandatsAgent;
}

JoinPolicy *MandatesJoinPolicy::clone()
{
    return new MandatesJoinPolicy();
}

// LastOfferJoinPolicy:

LastOfferJoinPolicy::~LastOfferJoinPolicy()
{
}

int LastOfferJoinPolicy::join(const Simulation &s, const vector<int> &offers)
{
    return offers.back();
}

JoinPolicy *LastOfferJoinPolicy::clone()
{
    return new LastOfferJoinPolicy();
}