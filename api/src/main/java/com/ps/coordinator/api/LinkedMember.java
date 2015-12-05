package com.ps.coordinator.api;

import java.io.Serializable;

public class LinkedMember implements Serializable {

    private boolean isAvailable;
    private String owner;

    public LinkedMember(String owner, boolean isAvailable) {
        this.owner = owner;
        this.isAvailable = isAvailable;
    }

    public String getOwner() {
        return owner;
    }

    public LinkedMember setOwner(String owner) {
        this.owner = owner;
        return this;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public LinkedMember setAvailable(boolean available) {
        isAvailable = available;
        return this;
    }

    @Override
    public String toString() {
        return "Member{" +
                "owner='" + (owner != null ? owner : "no") + '\'' +
                ", isAvailable=" + isAvailable +
                '}';
    }
}