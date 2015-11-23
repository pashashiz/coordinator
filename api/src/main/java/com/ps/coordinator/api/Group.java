package com.ps.coordinator.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Group implements Serializable {

    private String name;
    private String type;
    private String endpoint;
    private Map<String, Member> members;

    public Group(String name, String type, String endpoint) {
        this.name = name;
        this.type = type;
        this.endpoint = endpoint;
    }

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public String getType() {
        return type;
    }

    public Group setType(String type) {
        this.type = type;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Group setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public Map<String, Member> getMembers() {
        if (members == null)
            members = new HashMap<>();
        return members;
    }

    public boolean isAvailable() {
        for (Member member : members.values())
            if (member.isAvailable()) return true;
        return false;
    }

}
