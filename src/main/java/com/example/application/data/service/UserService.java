package com.example.application.data.service;

import com.example.application.data.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.artur.helpers.CrudService;

@Service
@Transactional
public class UserService extends CrudService<User, Integer> {

    private UserRepository repository;
    private PasswordEncoder encoder;

    public UserService(@Autowired UserRepository repository, @Autowired PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Override
    public User update(User entity) {
        if (!entity.getNewPassword().isEmpty()) {
            // If a new password is provided, encode and use that
            entity.setPasswordHash(encoder.encode(entity.getNewPassword()));
            System.out.println("Setting hash to " + entity.getPasswordHash());
        } else if (entity.getId() != null) {
            // If updating an existing entity without a new password, use the existing one
            entity.setPasswordHash(repository.getOne(entity.getId()).getPasswordHash());
            System.out.println("Leaving hash as " + entity.getPasswordHash());
        } else {
            // New entity without a password
            throw new IllegalArgumentException("A new user must have a password");
        }
        entity.setNewPassword("");
        return super.update(entity);
    }

    @Override
    protected UserRepository getRepository() {
        return repository;
    }

}
