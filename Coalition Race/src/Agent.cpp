#include "Agent.h"
#include "Simulation.h"

Agent::Agent(int agentId, int partyId, SelectionPolicy *selectionPolicy) : mAgentId(agentId), mPartyId(partyId), mSelectionPolicy(selectionPolicy), coalitionId(partyId)
{
}

int Agent::getId() const
{
    return mAgentId;
}

int Agent::getPartyId() const
{
    return mPartyId;
}

void Agent::step(Simulation &sim)
{
    int selectedParty = mSelectionPolicy->select(sim, *this);
    if (selectedParty != -1)
    {
        sim.getParty(selectedParty).addOffer(mAgentId);
    }
}

// OURS:

// clone constructor
Agent::Agent(Agent &agent, int agentId, int partyId) : mAgentId(agentId), mPartyId(partyId), mSelectionPolicy(nullptr), coalitionId(agent.getCoalitionId())
{
    mSelectionPolicy = agent.mSelectionPolicy->clone();
}

// copy constructor
Agent::Agent(const Agent &other) : mAgentId(other.mAgentId), mPartyId(other.mPartyId), mSelectionPolicy(nullptr), coalitionId(other.coalitionId)
{
    mSelectionPolicy = other.mSelectionPolicy->clone();
}

Agent &Agent::operator=(const Agent &other)
{
    if (this != &other)
    {
        mSelectionPolicy = other.mSelectionPolicy->clone();
        mAgentId = other.mAgentId;
        mPartyId = other.mPartyId;
        coalitionId = other.coalitionId;
    }
    return *this;
}

Agent::~Agent()
{
    if (mSelectionPolicy)
    {
        delete (mSelectionPolicy);
    }
    mSelectionPolicy = nullptr;
}

// move constructor
Agent::Agent(Agent &&other) : mAgentId(other.mAgentId), mPartyId(other.mPartyId), mSelectionPolicy(nullptr), coalitionId(other.coalitionId)
{
    this->mSelectionPolicy = other.mSelectionPolicy;
    other.mSelectionPolicy = nullptr;
}

// Move assignment operator
Agent &Agent::operator=(Agent &&other)
{
    if (this != &other)
    {
        if (mSelectionPolicy)
        {
            delete mSelectionPolicy;
            mSelectionPolicy = nullptr;
        }

        mSelectionPolicy = other.mSelectionPolicy;
        other.mSelectionPolicy = nullptr;
        this->coalitionId = other.coalitionId;
        this->mAgentId = other.mAgentId;
        this->mPartyId = other.mPartyId;
    }

    return *this;
}

int Agent::getCoalitionId() const
{
    return coalitionId;
}