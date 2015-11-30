package com.ps.coordinator.api.utils;

public class Assert {

    public static void checkNull(Object object, String name) {
        if (object == null)
            throw new IllegalArgumentException(name + " cannot be null");
    }

    public static void checkNullOrEmpty(String object, String name) {
        if (object == null || object.isEmpty())
            throw new IllegalArgumentException(name + " cannot be null or empty");
    }

}
