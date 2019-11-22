package com.vinyl.model;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;

@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @NotNull(message = "First name can't be blank")
    @NotBlank(message = "First name can't be blank")
    private String firstName;

    @NotNull(message = "Last name can't be blank")
    @NotBlank(message = "Last name can't be blank")
    private String lastName;

    @Email(message = "Not email address format")
    @NotNull(message = "Email Address can't be blank")
    @NotBlank(message = "Email Address can't be blank")
    private String emailAddress;

    @NotNull(message = "Password can't be blank")
    @NotBlank(message = "Password can't be blank")
    private String password;

    @OneToOne(mappedBy="user")
    private Cart cart;

    @OneToMany(mappedBy="user")
    private Set<Order> order;

    @OneToOne
    @JoinColumn(name = "user_role_id", nullable = false, foreignKey=@ForeignKey(name = "Fk_user_user_role_id"))
    private UserRole userRole;

    public User() {
    }

    public User(String firstName, String lastName, String emailAddress, String password) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.emailAddress = emailAddress;
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Set<Order> getOrder() {
        return order;
    }

    public void setOrder(Set<Order> order) {
        this.order = order;
    }

    public UserRole getUserRole() {
        return userRole;
    }

    public void setUserRole(UserRole userRole) {
        this.userRole = userRole;
    }
}
