package com.ps.coordinator.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Group implements Serializable {

    private String name;
    private Type type;
    private String subtype;
    private String endpoint;
    private Map<String, LinkedMember> members;

    public Group(String name, Type type, String subtype, String endpoint) {
        this.name = name;
        this.subtype = subtype;
        this.endpoint = endpoint;
    }

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        this.name = name;
        return this;
    }

    public Type getType() {
        return type;
    }

    public Group setType(Type type) {
        this.type = type;
        return this;
    }

    public String getSubtype() {
        return subtype;
    }

    public Group setSubtype(String subtype) {
        this.subtype = subtype;
        return this;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public Group setEndpoint(String endpoint) {
        this.endpoint = endpoint;
        return this;
    }

    public Map<String, LinkedMember> getMembers() {
        if (members == null)
            members = new HashMap<>();
        return members;
    }

    public boolean isAvailable() {
        for (LinkedMember member : members.values())
            if (member.isAvailable()) return true;
        return false;
    }

    public static Member createBy(Group group, String node) {
        LinkedMember linkedMember = group.getMembers().get(node);
        if (linkedMember == null)
            throw new IllegalArgumentException("No such node [" + node + "] in the group [" + group + "]");
        return new Member(group.getName(), node, group.getType(), group.getSubtype(), group.getEndpoint())
                .setAvailable(linkedMember.isAvailable());
    }

    public static Group createBy(Member member) {
        return new Group(member.getName(), member.getType(), member.subtype(), member.getEndpoint());
    }

}
