package com.vinyl.model;

import java.io.Serializable;

public class JwtVinylRequest implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;

    private String username;
    private String password;
    private Long quantity;

    public JwtVinylRequest()
    {

    }

    public JwtVinylRequest(String username, String password, Long status) {
        this.setUsername(username);
        this.setPassword(password);
        this.setQuantity(status);
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

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
}
