package bgu.spl.net.srv;

import java.util.concurrent.ConcurrentHashMap;

public interface Connections<T> {
    int getConnectionId(ConnectionHandler<T> handler);

    int connect(ConnectionHandler<T> handler);

    boolean subscribe(int connectionId, String channel, Integer subscriptionId);

    boolean isSubscribed(int connectionId, String channel);

    ConcurrentHashMap<Integer,Integer> getChannelSubscriptions(String channel);
   
    boolean send(int connectionId, T msg);

    void send(String channel, T msg);

    boolean unsubscribe(int connectionId, String channel);

    void disconnect(int connectionId);
}
