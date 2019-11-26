package com.vinyl.model;

import com.vinyl.validator.DoubleString;
import com.vinyl.validator.NumericString;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Set;


@Entity
@Table(name = "item")
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Name can't be blank")
    @NotBlank(message = "Name can't be blank")
    private String name;

    @NotNull(message = "Price can't be blank")
    @NotBlank(message = "Price can't be blank")
    @DoubleString(message = "Price should be numeric and positive")
    private Double price;

    @NotNull(message = "Description can't be blank")
    @NotBlank(message = "Description can't be blank")
    private String description;

    @NotNull(message = "Quantity can't be blank")
    @NotBlank(message = "Quantity can't be blank")
    @NumericString(message = "Quantity should be numeric and positive")
    private Long quantity;

    @OneToMany(mappedBy = "item")
    private Set<OrderItem> orderItem;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    public Set<OrderItem> getOrderItem() {
        return orderItem;
    }

    public void setOrderItem(Set<OrderItem> orderItem) {
        this.orderItem = orderItem;
    }
}

