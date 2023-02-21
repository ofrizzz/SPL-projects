# System-Programming-course-projects
four projects done as part of my CS studies in BGU.
The projects are:
1. Coalition Race.
2. Set Card Game.
3. World Cup Informer.
4. BGU Mart.

# 1. Coalition Race
A program that simulates the coalition race- given an election outcome with connections "rank" between every pair of parties - the program will find the best coalition that can be performed.

# practice goals:
The objective of this project is to design an object-oriented system and gain
implementation experience in C++ while using classes, standard data structures, and unique
C++ properties.

# 2. Set Card Game
This program implements a version of the well known card game "Set" (https://en.wikipedia.org/wiki/Set_(card_game))
This version of the game supports multi-player and playing against a computer player/s.
here is an example of how the game looks like:
![image](https://user-images.githubusercontent.com/117899740/220136643-588bf39b-0752-45e2-bb75-0031a7561caa.png)
![image](https://user-images.githubusercontent.com/117899740/220136677-b60c2a3d-2b60-4521-a38e-cf9fe4034cbc.png)

# practice goals:
This project, written in Java, was mostly about practicing concurrency in multi-threaded enviorment.
Here every player is represented as a thread, performing commands taken from the keyboard (for human player) or a seperate thread generating random keyboard presses. In addition there is the timer thread and dealer thread which is responsible for managing the table, players' score, cards, etc.
One of our goals was to make the flow of the game as fast as we could, that means, as much active threads at any time as we can, while keeping the shared resources thread safe.
For this cause we used multiplte synchronization mechanisms such as: Read-Write lock, atomic variables, sleep/interrupt and Java's monitors.

# 3. World Cup Informer
In this project 
