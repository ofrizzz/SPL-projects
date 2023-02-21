#pragma once
#include <vector>

using std::vector;
class Simulation;

class JoinPolicy
{
public:
    virtual int join(const Simulation& s,const vector<int> &offers) = 0;
    virtual JoinPolicy* clone() = 0;
    virtual ~JoinPolicy();
};

class MandatesJoinPolicy : public JoinPolicy
{
public:
    virtual int join(const Simulation& s,const vector<int> &offers);
    virtual JoinPolicy* clone();
    virtual ~MandatesJoinPolicy();
};

class LastOfferJoinPolicy : public JoinPolicy
{
public:
    virtual int join(const Simulation& s,const vector<int> &offers);
    virtual JoinPolicy* clone();
    virtual ~LastOfferJoinPolicy();
};