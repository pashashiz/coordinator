package com.ps.coordinator.api;

import java.io.Serializable;

public class Member implements Serializable {

    private String name;
    private String node;
    private String type;
    private String endpoint;
    private boolean isAvailable;

    public Member(String name, String node, String type, String endpoint) {
        this.name = name;
        this.node = node;
        this.type = type;
        this.endpoint = endpoint;
        isAvailable = true;
    }

    public String getName() {
        return name;
    }

    public Member setName(String name) {
        this.name = name;
        return this;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getType() {
        return type;
    }

    public Member setType(String type) {
        this.type = type;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Member setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public Member setAvailable(boolean available) {
        isAvailable = available;
        return this;
    }

}