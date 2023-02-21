package bgu.spl.net.srv;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsImpl<T> implements Connections<T> {
    HashMap<Integer,ConnectionHandler<T>> connections = new HashMap<Integer,ConnectionHandler<T>>();//concurrent?
    int connectionId = 0;
    ConcurrentHashMap<String, ConcurrentHashMap<Integer, Integer>> channels = new ConcurrentHashMap<String, ConcurrentHashMap<Integer,Integer>>(); //chanel -> {connectionId:subscriptionId}

    @Override
    public int connect(ConnectionHandler<T> handler) {
        connections.put(connectionId, handler);
        return connectionId++;
    }

    @Override
    public boolean subscribe(int connectionId, String channel, Integer subscriptionId) {
        if (!channels.containsKey(channel)) {
            channels.put(channel, new ConcurrentHashMap<Integer, Integer>());
        }

        if (!channels.get(channel).containsKey(connectionId)) {
            channels.get(channel).put(connectionId, subscriptionId);
            return true;
        }

        return false;
    }

    @Override
    public boolean send(int connectionId, T msg) {
        if (connections.get(connectionId) != null) {
            connections.get(connectionId).send(msg);
            return true;
        }
        return false;
    }

    @Override
    public void send(String channel, T msg) { 
        if (channels.containsKey(channel)) {
            channels.get(channel).forEach((k, v) -> send(k,msg));
        }
    }


    public boolean isSubscribed(int connectionId, String channel) {
        return channels.containsKey(channel) && channels.get(channel).containsKey(connectionId);
    }

    @Override
    public boolean unsubscribe(int connectionId, String channel) {
        if (channels.containsKey(channel) && channels.get(channel).containsKey(connectionId)) {
            channels.get(channel).remove(connectionId);

            if (channels.get(channel).isEmpty()) {
                channels.remove(channel);
            }
            return true;
        }
        return false;
    }

    @Override
    public void disconnect(int connectionId) {
        connections.remove(connectionId);
        channels.forEach((k, v) -> v.remove(connectionId));
    }

    @Override
    public int getConnectionId(ConnectionHandler handler) {
        for (int i = 0; i < connections.size(); i++) {
            if (connections.get(i) == handler) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public ConcurrentHashMap<Integer, Integer> getChannelSubscriptions(String channel) {
        return channels.get(channel);
    }

}