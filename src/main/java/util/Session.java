package util;

import Entities.User;

public class Session {
    private static Session instance;
    private User loggedInUser;

    private Session() {}

    public static Session getInstance() {
        if (instance == null) {
            instance = new Session();
        }
        return instance;
    }

    public void setUser(User user) {
        this.loggedInUser = user;
    }

    public User getUser() {
        return loggedInUser;
    }

    public void clear() {
        loggedInUser = null;
    }

    public boolean isLoggedIn() {
        return loggedInUser != null;
    }
}


