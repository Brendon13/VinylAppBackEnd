package com.vinyl.model;

import javax.persistence.*;

@Entity
@Table(name = "user_role")
public class UserRole {
    @Id
    @GeneratedValue
    private Long id;

    private String roles;

    @OneToOne(mappedBy="userRole")
    private User user;

    public UserRole(Long id, String roles) {
        this.id = id;
        this.roles = roles;
    }

    public UserRole(){

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRoles() {
        return roles;
    }

    public void setRoles(String roles) {
        this.roles = roles;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
