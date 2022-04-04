package com.geekbrains.cloud.server.service;

public class UserNameService {

    public static int userId = 0;
    public static int MAX_USER_CONNECTED = 5;

    public String getUserName() {
        if (userId <= MAX_USER_CONNECTED) {

            userId++;
            return "user" + userId;
        } else {
            return "user_unconnected";
        }
    }
}
