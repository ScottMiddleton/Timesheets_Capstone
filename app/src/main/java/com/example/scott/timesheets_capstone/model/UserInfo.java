package com.example.scott.timesheets_capstone.model;

public class UserInfo {

    private String email;
    private String name;
    private String password;

    public UserInfo(String e, String n, String p) {
        email = e;
        name = n;
        password = p;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
