package com.ps.coordinator.api;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.ps.coordinator.api.utils.Assert.*;

public class Group implements Serializable {

    private String name;
    private Type type;
    private String subtype;
    private String address;
    private Map<String, LinkedMember> members;

    public Group(String name, Type type, String subtype, String address) {
        setName(name).setType(type).setSubtype(subtype).setAddress(address);
    }

    public String getName() {
        return name;
    }

    public Group setName(String name) {
        checkNullOrEmpty(name, "Member name");
        this.name = name;
        return this;
    }

    public Type getType() {
        return type;
    }

    public Group setType(Type type) {
        checkNull(type, "Member type");
        this.type = type;
        return this;
    }

    public String getSubtype() {
        return subtype;
    }

    public Group setSubtype(String subtype) {
        checkNullOrEmpty(subtype, "Member subtype");
        this.subtype = subtype;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Group setAddress(String address) {
        this.address = address;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Group group = (Group) o;
        return name.equals(group.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return "Group{" +
                "name='" + name + '\'' +
                ", type=" + type +
                ", getSubtype='" + subtype + '\'' +
                ", address='" + address + '\'' +
                ", members=" + members +
                '}';
    }

    public static Member createBy(Group group, String node) {
        LinkedMember linkedMember = group.getMembers().get(node);
        if (linkedMember == null)
            throw new IllegalArgumentException("No such node [" + node + "] in the group [" + group + "]");
        return new Member(group.getName(), node, group.getType(), group.getSubtype(), group.getAddress())
                .setAvailable(linkedMember.isAvailable());
    }

    public static Group createBy(Member member) {
        return new Group(member.getName(), member.getType(), member.getSubtype(), member.getAddress());
    }

}
