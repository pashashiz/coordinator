package com.ps.coordinator.rest;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping(value = "/users")
public class UsersServiceRest {

    @RequestMapping(value = "/current")
    public Principal user(Principal user) {
        return user;
    }

}
