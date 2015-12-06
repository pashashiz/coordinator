package com.ps.coordinator;

import static org.junit.Assert.assertTrue;

public class Assert {

    public static  <T extends Exception> void assertException(String message, Class<T> exception, Runnable function) {
        boolean isAssertionTrue = false;
        try {
            function.run();
        } catch (Exception e) {
            isAssertionTrue = e.getClass().isAssignableFrom(exception);
        }
        assertTrue(message, isAssertionTrue);
    }

}
