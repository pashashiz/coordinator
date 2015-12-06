package com.ps.coordinator.api;

import com.ps.coordinator.api.utils.Assert;

import java.io.Serializable;

public class Member implements Serializable {

    private String name;
    private String node;
    private String owner;
    private Type type;
    private String subtype;
    private String address;
    private boolean isAvailable;

    public Member() {}

    public Member(String name, String node, String owner, Type type, String subtype, String address) {
        setName(name).setNode(node).setOwner(owner).setType(type).setSubtype(subtype)
                .setAddress(address).setAvailable(true);
    }

    public Member(String name, String node, Type type, String subtype, String address) {
        this(name, node, null, type, subtype, address);
    }

    public Member(String name, Type type, String subtype) {
        this(name, "master", null, type, subtype, null);
    }

    public String getName() {
        return name;
    }

    public Member setName(String name) {
        Assert.notNullOrEmpty(name, "Member name");
        this.name = name;
        return this;
    }

    public String getNode() {
        return node;
    }

    public Member setNode(String node) {
        Assert.notNullOrEmpty(node, "Member node name");
        this.node = node;
        return this;
    }

    public String getOwner() {
        return owner;
    }

    public Member setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public Type getType() {
        return type;
    }

    public Member setType(Type type) {
        Assert.notNull(type, "Member type");
        this.type = type;
        return this;
    }

    public String getSubtype() {
        return subtype;
    }

    public Member setSubtype(String subtype) {
        Assert.notNullOrEmpty(subtype, "Member subtype");
        this.subtype = subtype;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Member setAddress(String address) {
        this.address = address;
        return this;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public Member setAvailable(boolean available) {
        isAvailable = available;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return name.equals(member.name) && node.equals(member.node);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + node.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Member{" +
                "name='" + name + '\'' +
                ", node='" + node + '\'' +
                ", owner='" + (owner != null ? owner : "no") + '\'' +
                ", type=" + type +
                ", getSubtype='" + subtype + '\'' +
                ", address='" + address + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}