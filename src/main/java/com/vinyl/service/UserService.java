package com.vinyl.service;

import com.vinyl.model.User;

public interface UserService {
    void save(User user);
    User findByEmailAddress(String emailAddress);
    User findByPassword(String password);
    void delete(User user);
}
