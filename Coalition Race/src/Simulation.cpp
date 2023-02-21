#include "Simulation.h"

Simulation::Simulation(Graph graph, vector<Agent> agents) : mGraph(graph), mAgents(agents), coalitions()
{
    for (auto &agent : mAgents)
    {
        coalitions.push_back(Coalition(getParty(agent.getPartyId())));
    }
}

void Simulation::step()
{
    for (int i = 0; i < mGraph.getNumVertices(); i++)
    {
        mGraph.getParty(i).step(*this);
    }
    for (auto &agent : mAgents)
    {
        agent.step(*this);
    }
}

bool Simulation::shouldTerminate() const
{
    const bool allPartiesJoined = static_cast<int>(getAgents().size()) == getGraph().getNumVertices();

    bool biggerthan60coali = false;
    for (auto &coali : coalitions)
    {
        biggerthan60coali |= coali.getMandats() > 60;
    }

    return allPartiesJoined || biggerthan60coali;
}

const Graph &Simulation::getGraph() const
{
    return mGraph;
}

const vector<Agent> &Simulation::getAgents() const
{
    return mAgents;
}

const Party &Simulation::getParty(int partyId) const
{
    return mGraph.getParty(partyId);
}

/// This method returns a "coalition" vector, where each element is a vector of party IDs in the coalition.
/// At the simulation initialization - the result will be [[agent0.partyId], [agent1.partyId], ...]
const vector<vector<int>> Simulation::getPartiesByCoalitions() const
{
    vector<vector<int>> partiesByCoalitions;
    for (auto &coal : coalitions)
    {
        partiesByCoalitions.push_back(coal.getParties());
    }

    return partiesByCoalitions;
}

// OUR FUNCTIONS:
void Simulation::onPartyJoined(int partyId, int agentId)
{
    int coalId = mAgents.at(agentId).getCoalitionId();

    Coalition *coal;
    for (auto &c : coalitions)
    {
        if (c.getCoalitionId() == coalId)
        {
            coal = &c;
            break;
        }
    }

    coal->joinCoalition(getParty(partyId));

    Agent clone(mAgents.at(agentId), mAgents.size(), partyId);
    mAgents.push_back(clone);
}

const vector<Coalition> &Simulation::getCoalitions()
{
    return coalitions;
}

Party &Simulation::getParty(int partyId)
{
    return mGraph.getParty(partyId);
}

const Coalition *Simulation::getCoalition(int coalitionId) const
{
    for (auto &coali : coalitions)
    {
        if (coali.getCoalitionId() == coalitionId)
        {
            return &coali;
        }
    }

    return nullptr;
}
