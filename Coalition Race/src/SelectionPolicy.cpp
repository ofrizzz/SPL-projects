#include "SelectionPolicy.h"
#include "Simulation.h"
using std::distance;
using std::vector;

SelectionPolicy::~SelectionPolicy()
{
}

vector<Party> SelectionPolicy::filterOptions(Simulation &sim, Agent &agent)
{
   Graph graph = sim.getGraph();
   vector<Party> options;
   const vector<int> neighbors = graph.getNeighbors(agent.getPartyId());

   for (auto it = neighbors.begin(); it != neighbors.end(); ++it)
   {
      if (*it == 0)
      {
         continue;
      }
      int neighborPartyId = distance(neighbors.begin(), it);
      const Party neighborParty = sim.getParty(neighborPartyId);
      bool statusCheck = neighborParty.getState() == Waiting || neighborParty.getState() == CollectingOffers;
      bool notSameCoallitionCheck = true;

      for (int iAgentOffer : neighborParty.getPartyOffers())
      {
         notSameCoallitionCheck &= sim.getAgents()[iAgentOffer].getCoalitionId() != agent.getCoalitionId();
         if (!notSameCoallitionCheck)
         {
            break;
         }
      }

      if (statusCheck && notSameCoallitionCheck)
      {
         options.push_back(neighborParty);
      }
   }

   return options;
}

// MandatesSelectionPolicy:

MandatesSelectionPolicy::~MandatesSelectionPolicy()
{
}
int MandatesSelectionPolicy::select(Simulation &sim, Agent &agent)
{
   vector<Party> options = filterOptions(sim, agent);
   if (options.size() == 0)
   {
      // idle
      return -1;
   }

   int selected = 0;
   int maxMandats = options[selected].getMandates();
   for (auto it = options.begin(); it != options.end(); it++)
   {
      int index = distance(options.begin(), it);
      if (options[index].getMandates() > maxMandats)
      {
         selected = index;
         maxMandats = options[selected].getMandates();
      }
   }

   return options[selected].getId();
}

SelectionPolicy *MandatesSelectionPolicy::clone()
{
   return new MandatesSelectionPolicy();
}

// EdgeWeightSelectionPolicy:

EdgeWeightSelectionPolicy::~EdgeWeightSelectionPolicy()
{
}
int EdgeWeightSelectionPolicy::select(Simulation &sim, Agent &agent)
{
   vector<Party> options = filterOptions(sim, agent);
   if (options.size() == 0)
   {
      // idle
      return -1;
   }

   int selected = 0;
   int PartyId = agent.getPartyId();
   int maxWeight = sim.getGraph().getEdgeWeight(options[selected].getId(), PartyId);
   for (auto it = options.begin(); it != options.end(); it++)
   {
      int index = distance(options.begin(), it);
      int tempWeight = sim.getGraph().getEdgeWeight((*it).getId(), PartyId);
      if (tempWeight > maxWeight)
      {
         selected = index;
         maxWeight = tempWeight;
      }
   }
   return options[selected].getId();
}

SelectionPolicy *EdgeWeightSelectionPolicy::clone()
{
   return new EdgeWeightSelectionPolicy();
}
