package com.example.application.data.service;

import com.example.application.data.dto.UserDTO;
import com.example.application.data.entity.User;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.vaadin.artur.helpers.CrudService;

import java.util.Optional;
import java.util.stream.Stream;

@Service
@Transactional
public class UserService extends CrudService<User, Integer> {

    private final ModelMapper modelMapper = new ModelMapper();

    private UserRepository repository;
    private PasswordEncoder encoder;

    public UserService(@Autowired UserRepository repository, @Autowired PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public User update(UserDTO dto) {
        User entity;
        if (dto.getId() != null) {
            entity = get(dto.getId()).orElseThrow(() -> new IllegalArgumentException("The user with ID blabla does not exist"));
        } else {
            entity = new User();
        }
        modelMapper.map(dto, entity);

        if (!dto.getNewPassword().isEmpty()) {
            // If a new password is provided, encode and use that
            entity.setPasswordHash(encoder.encode(dto.getNewPassword()));
            System.out.println("Setting hash to " + entity.getPasswordHash());
        } else if (entity.getId() == null) {
            // New entity without a password
            throw new IllegalArgumentException("A new user must have a password");
        }
        return super.update(entity);
    }

    public Optional<UserDTO> getDto(Integer id) {
        return super.get(id).map(value -> modelMapper.map(value, UserDTO.class));
    }

    @Override
    protected UserRepository getRepository() {
        return repository;
    }

    public Stream<UserDTO> findUsers() {
        return repository.findAll().stream()
                .map(user -> modelMapper.map(user, UserDTO.class));
    }
}
