package com.example.application.data.service;

import com.example.application.data.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import javax.validation.constraints.Email;

public interface UserRepository extends JpaRepository<User, Integer> {

}