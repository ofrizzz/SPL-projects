#include "Coalition.h"

Coalition::Coalition() : parties(), mandats(), coalitionId(){};

Coalition::Coalition(Party &leadingParty) : parties(), mandats(leadingParty.getMandates()), coalitionId(leadingParty.Party::getId())
{
    parties.push_back(leadingParty.Party::getId());
}

Coalition::~Coalition()
{
}

int Coalition::getMandats() const
{
    return mandats;
}

int Coalition::getCoalitionId() const
{
    return coalitionId;
}

vector<int> Coalition::getParties() const
{
    return parties;
}

void Coalition::joinCoalition(Party &par)
{

    parties.push_back(par.getId());
    mandats += par.getMandates();
}