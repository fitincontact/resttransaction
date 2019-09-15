package com.resttransfer.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class User {

    @JsonIgnore
    private long userId ;

    @JsonProperty(required = true)
    private String userName;

    // for com.fasterxml
    public User() { }

    public User(String userName) {
        this.userName = userName;
    }

    public User(long userId, String userName) {
        this.userId = userId;
        this.userName = userName;
    }

    public long getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (userId != user.userId) return false;
        return userName.equals(user.userName);

    }

    @Override
    public int hashCode() {
        int result = (int) (userId ^ (userId >>> 32));
        result = 31 * result + userName.hashCode();
        return result;
    }

    @Override
    public String toString() {
        StringBuffer strBuffer = new StringBuffer("");
        strBuffer.append("User{userId=");
        strBuffer.append(userId);
        strBuffer.append(", userName='");
        strBuffer.append(userName);
        strBuffer.append("}");
        return strBuffer.toString();
    }
}
