#pragma once

#include <vector>
#include "Graph.h"
#include "SelectionPolicy.h"

class Simulation;
class SelectionPolicy;

class Agent
{
public:
    Agent(int agentId, int partyId, SelectionPolicy *selectionPolicy);

    int getPartyId() const;
    int getId() const;
    void step(Simulation &);

    Agent(Agent &agent, int agentId, int partyId);
    Agent(const Agent &other);
    Agent &operator=(const Agent &other);
    Agent(Agent &&agent);
    Agent &operator=(Agent &&other);
    virtual ~Agent();
    int getCoalitionId() const;

private:
    int mAgentId;
    int mPartyId;
    SelectionPolicy *mSelectionPolicy;
    int coalitionId;
};
