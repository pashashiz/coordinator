package com.ps.coordinator.rest;

import com.ps.coordinator.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

import static org.springframework.web.bind.annotation.RequestMethod.*;

@RestController
@RequestMapping("/registrationAndDiscovery")
public class RegistrationAndDiscoveryServiceRest implements RegistrationAndDiscoveryService {

    @Autowired RegistrationAndDiscoveryServiceInteractive service;

    @RequestMapping(path = "", method = POST)
    @Override public void register(@RequestBody Member member) {
        service.register(member);
    }

    @RequestMapping(path = "/{name}", method = DELETE)
    @Override public void unregister(@PathVariable String name, @RequestParam(required = false) String node) {
        if (node != null && !node.isEmpty())
            service.unregister(name, node);
        else
            unregister(name);
    }

    @Override public void unregister(String name) {
        service.unregister(name);
    }

    @RequestMapping(path = "/{name}/setUnavailable", method = PATCH)
    @Override public void setUnavailable(@PathVariable String name, @RequestParam String node) {
        service.setUnavailable(name, node);
    }

    @RequestMapping(path = "/{name}", method = GET)
    @Override public Group find(@PathVariable String name) {
        return service.find(name);
    }

    @RequestMapping(path = "/search/findByType", method = GET)
    @Override public Set<Group> findAll(@RequestParam Type type) {
        return service.findAll(type);
    }

    @RequestMapping(path = "/search/findByTypeAndSubtype", method = GET)
    @Override public Set<Group> findAll(@RequestParam Type type, @RequestParam String subtype) {
        return service.findAll(type, subtype);
    }
}
