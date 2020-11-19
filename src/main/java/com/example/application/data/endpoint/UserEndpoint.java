package com.example.application.data.endpoint;

import com.example.application.data.CrudEndpoint;
import com.example.application.data.entity.User;
import com.example.application.data.service.UserService;
import com.vaadin.flow.server.connect.Endpoint;

import org.springframework.beans.factory.annotation.Autowired;

@Endpoint
public class UserEndpoint extends CrudEndpoint<User, Integer> {

    private UserService service;

    public UserEndpoint(@Autowired UserService service) {
        this.service = service;
    }

    @Override
    protected UserService getService() {
        return service;
    }

}
