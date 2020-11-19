package com.example.application.data.generator;

import com.vaadin.flow.spring.annotation.SpringComponent;

import com.example.application.data.service.UserRepository;
import com.example.application.data.entity.User;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.vaadin.artur.exampledata.DataType;
import org.vaadin.artur.exampledata.ExampleDataGenerator;

@SpringComponent
public class DataGenerator {

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository) {
        return args -> {
            Logger logger = LoggerFactory.getLogger(getClass());
            if (userRepository.count() != 0L) {
                logger.info("Using existing database");
                return;
            }
            int seed = 123;

            logger.info("Generating demo data");

            logger.info("... generating 100 User entities...");
            ExampleDataGenerator<User> userRepositoryGenerator = new ExampleDataGenerator<>(User.class);
            userRepositoryGenerator.setData(User::setId, DataType.ID);
            userRepositoryGenerator.setData(User::setProfilePicture, DataType.PROFILE_PICTURE_URL);
            userRepositoryGenerator.setData(User::setEmail, DataType.EMAIL);
            userRepositoryGenerator.setData(User::setPassword, DataType.TWO_WORDS);
            userRepository.saveAll(userRepositoryGenerator.create(100, seed));



            logger.info("Generated demo data");
        };
    }

}