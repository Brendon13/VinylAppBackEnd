package com.vinyl.model;

import java.io.Serializable;

public class JwtAddVinylRequest implements Serializable {

    private static final long serialVersionUID = 5926468583005150707L;

    private String username;
    private String password;

    private String name;
    private Double cost;
    private Long stock;
    private String description;

    public JwtAddVinylRequest()
    {

    }

    public JwtAddVinylRequest(String username, String password, String name, Double cost, Long stock, String description) {
        this.setUsername(username);
        this.setPassword(password);
        this.setName(name);
        this.setCost(cost);
        this.setStock(stock);
        this.setDescription(description);
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getCost() {
        return cost;
    }

    public void setCost(Double cost) {
        this.cost = cost;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
