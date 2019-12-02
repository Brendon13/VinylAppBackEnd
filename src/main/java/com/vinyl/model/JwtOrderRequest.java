package com.vinyl.model;

import java.io.Serializable;

public class JwtOrderRequest implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;

    private String username;
    private String password;

    private Long status;

    //need default constructor for JSON Parsing
    public JwtOrderRequest()
    {

    }

    public JwtOrderRequest(Long status, String username, String password) {
        this.setStatus(status);
        this.setUsername(username);
        this.setPassword(password);
    }


    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }
}
