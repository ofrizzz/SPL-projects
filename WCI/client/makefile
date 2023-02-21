CXX = g++
CXXFLAGS = -c -Wall -Weffc++ -g -std=c++11 -pthread
CPPFLAGS = -I ./include -MMD -MP
LDFLAGS := -lboost_system -lpthread

SRCS = $(wildcard ./src/*.cpp)
OBJS = $(patsubst ./src/%.cpp,./bin/%.o, $(SRCS))
DEPS := $(patsubst %.o,%.d, $(OBJS))

# default target
all: ./bin/StompWCIClient

# build StompClient
./bin/StompWCIClient: $(OBJS)
	@echo "Building ..."
	$(CXX) $(OBJS) -o $@ $(LDFLAGS)
	@echo "Finished building"

# build cpp files
./bin/%.o: ./src/%.cpp
	$(CXX) $(CXXFLAGS) $(CPPFLAGS) -c $< -o $@

# clean build files
clean:
	@rm -f ./bin/*

-include $(DEPS)