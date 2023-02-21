package bgu.spl.net.impl.stomp;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LiveFeed {
    ConcurrentHashMap<String, LiveFeedUser> users = new ConcurrentHashMap<>();
    ConcurrentHashMap<String, ConcurrentLinkedQueue<LiveFeedUser>> channels = new ConcurrentHashMap<>();

    public LiveFeed() {
    }

    public LiveFeedUser createUser(String username, String password) {
        return users.putIfAbsent(username, new LiveFeedUser(username, password)); 
    }

    public LoginAttemptResponseType login(String username, String password) {
        LiveFeedUser user = users.get(username);
        LoginAttemptResponseType response;
        if (user == null) {
            createUser(username, password);
            user = users.get(username);
            user.loggedIn = true;

            response = LoginAttemptResponseType.SUCCESS_LOGIN_USER_CREATED;
        } else {
            if (user.loggedIn) {
                response = LoginAttemptResponseType.ERR_USER_ALREADY_CONNECTED;
            } else if (user.checkPassword(password)) {
                user.loggedIn = true;
                response = LoginAttemptResponseType.SUCCESS_LOGIN_USER_EXISTS;
            } else {
                response = LoginAttemptResponseType.ERR_WRONG_PASSWORD;
            }
        }
        return response;
    }

    public LiveFeedUser getUser(String username) {
        return users.get(username);
    }

    public boolean isLoggedIn(String username) {
        LiveFeedUser user = users.get(username);
        if (user == null) {
            return false;
        }
        return user.loggedIn;
    }
}
