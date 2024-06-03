# System-Programming-projects
four projects done as part of my CS studies in BGU.
The projects are:
1. Coalition Race.
2. Set Card Game.
3. World Cup Informer.
4. BGU Mart.

All made in collaboration with Or Virt- @orbitoly.

# 1. Coalition Race
A program that simulates the coalition race- given an election outcome with connections "rank" between every pair of parties - the program will find the best coalition that can be performed.

# practice goals:
The objective of this project is to design an object-oriented system and gain
implementation experience in C++ while using classes, standard data structures, and unique
C++ properties.

# 2. Set Card Game
This program implements a version of the well known card game "Set" (https://en.wikipedia.org/wiki/Set_(card_game))
This version of the game supports multi-player and playing against a computer player/s.
Here is an example of how the game looks like:
![image](https://user-images.githubusercontent.com/117899740/220136643-588bf39b-0752-45e2-bb75-0031a7561caa.png)
![image](https://user-images.githubusercontent.com/117899740/220136677-b60c2a3d-2b60-4521-a38e-cf9fe4034cbc.png)

# practice goals:
This project, written in Java, was mostly about practicing concurrency in multi-threaded enviorment.
Here every player is represented as a thread, performing commands taken from the keyboard (for human player) or a seperate thread generating random keyboard presses. In addition there is the timer thread and dealer thread which is responsible for managing the table, players' score, cards, etc.
One of our goals was to make the flow of the game as fast as we could, that means, as much active threads at any time as we can, while keeping the shared resources thread safe.
For this cause we used multiplte synchronization mechanisms such as: Read-Write lock, atomic variables, sleep/interrupt and Java's monitors.

# 3. World Cup Informer
In this project we Implemented a forum about the 2022 Qatar world cup.
Users of this forum can create an acount and log in from any computer, subscribe to different games to get reports from other users, and report whatever they want about games they are subscribed to.

![image](https://user-images.githubusercontent.com/117899740/220344416-e95fbbf2-fd4f-4274-b077-f74af60f0386.png)

![image](https://user-images.githubusercontent.com/117899740/220344479-0ffbc5f4-4878-49cc-8df2-dc54b9e5657b.png)

# practice goals:
The WCI forum was mainly about practicing inter-proccess communication, or more basiclly, implementing a sever-client program.
Based on a TCP protocol, the server has 2 diiferent types of work (design patterns) - one is as a thread per client server, and the other one is as a Reactor based server.
The application layer protocol we used is Simple Text Oriented Messaging Protocol (STOMP - stomp.github.io) and the programming languages we used are- Java for the server code and C++ for the client code.
Much of our focus when designing the server was to implement it Modular, that so most changes in the application will not cause changes in the implemtation of the basic server, and also using a different messaging protocol should cause minimal change.

# 4. BGU Mart
A program that manages supermarket chains. The software supports managing a large number of employees and the buying/selling of products.
It also manages the inventory and thus contact various suppliers.
Sells and deliveries of products are also being registered and logged.

# practice goals:
With creating this program we wanted to learn and practice managing a database using SQL with sqlite3 libary in python.
Again much of our work has been about making this code modular, and we made that happen using the persistance layer design pattern.
This pattern is ment for separating the logics of the program from the actions performed on the db, and is done by creating a data type that represents a record in a certain table, and a generic data type for a table, with methods such as insert, find, delete and update.
In this way we could perform actions on the data base without writing a SQL query for each action, and make the code more clean, modular and readable.
