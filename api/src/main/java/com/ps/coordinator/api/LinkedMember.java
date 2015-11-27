package com.ps.coordinator.api;

import java.io.Serializable;

public class LinkedMember implements Serializable {

    private boolean isAvailable;

    public LinkedMember(boolean isAvailable) {
        this.isAvailable = isAvailable;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public LinkedMember setAvailable(boolean available) {
        isAvailable = available;
        return this;
    }

}