#pragma once

#include <vector>
#include "Party.h"

using std::vector;

class Simulation;
class Agent;

class SelectionPolicy
{
public:
    virtual int select(Simulation & sim, Agent & agent) = 0;
    virtual ~SelectionPolicy();
    virtual SelectionPolicy* clone() = 0;

protected:
    vector<Party> filterOptions(Simulation & sim, Agent & agent);
};

class MandatesSelectionPolicy : public SelectionPolicy
{
public:
    virtual int select(Simulation & sim, Agent & agent);
    virtual ~MandatesSelectionPolicy();
    virtual SelectionPolicy* clone();

};

class EdgeWeightSelectionPolicy : public SelectionPolicy
{
public:
    virtual int select(Simulation & sim, Agent & agent);
    virtual ~EdgeWeightSelectionPolicy();
    virtual SelectionPolicy* clone();
};