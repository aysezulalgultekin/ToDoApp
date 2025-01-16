package com.example.todoappdemo;

public class User {
    private String userId;
    private String email;
    private String name;
    private int level;

    public User(String userId, String email, String name, int level) {
        this.userId = userId;
        this.email = email;
        this.name = name;
        this.level = level;
    }
    public User(String userId, String email) {
        this.userId = userId;
        this.email = email;
    }
    public User() {}
    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
}



