#pragma once

#include <vector>
#include "Coalition.h"
#include "Graph.h"
#include "Agent.h"

using std::string;
using std::vector;

class Simulation
{
public:
    Simulation(Graph g, vector<Agent> agents);

    void step();
    bool shouldTerminate() const;
    const Graph &getGraph() const;
    const vector<Agent> &getAgents() const;
    const Party &getParty(int partyId) const;
    const vector<vector<int>> getPartiesByCoalitions() const;

    // OURS:
    void onPartyJoined(int partyId, int agentId);
    const vector<Coalition> &getCoalitions();
    Party &getParty(int partyId);
    const Coalition *getCoalition(int coalitionId) const;

private:
    Graph mGraph;
    vector<Agent> mAgents;
    vector<Coalition> coalitions;
};
