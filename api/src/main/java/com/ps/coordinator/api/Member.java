package com.ps.coordinator.api;

import java.io.Serializable;

public class Member implements Serializable {

    private String name;
    private String node;
    private Type type;
    private String subtype;
    private String endpoint;
    private boolean isAvailable;

    public Member(String name, String node, Type type, String subtype, String endpoint) {
        this.name = name;
        this.node = node;
        this.type = type;
        this.subtype = subtype;
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

    public Type getType() {
        return type;
    }

    public Member setType(Type type) {
        this.type = type;
        return this;
    }

    public String subtype() {
        return subtype;
    }

    public Member setSubtype(String subtype) {
        this.subtype = subtype;
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