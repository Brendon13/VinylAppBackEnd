package com.vinyl.repository;

import com.vinyl.model.User;
        import org.springframework.data.jpa.repository.JpaRepository;
        import org.springframework.data.repository.CrudRepository;
        import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {
        User findByEmailAddress(String emailAddress);
        User findByPassword(String password);
}
