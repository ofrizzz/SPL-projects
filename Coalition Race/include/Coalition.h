#pragma once

#include "Party.h"
#include <vector>

using std::vector;

class Coalition
{
private:
    vector<int> parties;
    int mandats;
    int coalitionId;

public:
    Coalition();
    Coalition(Party &leadingParty);
    ~Coalition();

    void joinCoalition(Party &par);
    int getMandats() const;
    int getCoalitionId() const;
    vector<int> getParties() const;
};