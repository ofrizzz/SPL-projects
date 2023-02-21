package bgu.spl.net.impl.stomp;

import java.util.concurrent.ConcurrentHashMap;

public class LiveFeedUser {
    final String username;
    final String password;
    boolean loggedIn = false;

    final ConcurrentHashMap<Integer, String> subscriptions = new ConcurrentHashMap<>();

    public LiveFeedUser(String username, String password) {
        this.password = password;
        this.username = username;
    }

    public boolean checkPassword(String password) {
        return this.password.equals(password);
    }

    public void addSubscription(int subscriptionId, String channelName) {
        subscriptions.put(subscriptionId, channelName);
    }

    public String removeSubscription(int subscriptionId) {
        return subscriptions.remove(subscriptionId);
    }

    public void setLoggedOut() {
        loggedIn = false;
    }

}