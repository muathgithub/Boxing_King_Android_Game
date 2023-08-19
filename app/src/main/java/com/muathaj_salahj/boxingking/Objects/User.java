package com.muathaj_salahj.boxingking.Objects;

import java.util.ArrayList;

public class User {

    // declaring the needed attributes
    private String name;
    private String email;
    private float score;
    private ArrayList<String> follows;

    // empty constructor for firebase APIs
    public User(){

    }

    // constructor for creating/holding users objects
    public User(String name, String email, float score, ArrayList<String> follows) {
        this.name = name;
        this.email = email;
        this.score = score;
        this.follows = follows == null ? new ArrayList<String>() : follows;
    }

    // getters for normal user and for firebase APIs use
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public float getScore() {
        return score;
    }

    public ArrayList<String> getFollows() {
        return follows;
    }
}
