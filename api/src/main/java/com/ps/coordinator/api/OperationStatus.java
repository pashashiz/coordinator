package com.ps.coordinator.api;

public class OperationStatus {

    public enum Status {SUCCESSFUL, WARNING, ERROR}

    private Status status;
    private int code;
    private String message;

    public OperationStatus(Status status, int code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public static OperationStatus createSuccessful() {
        return new OperationStatus(Status.SUCCESSFUL, 0, null);
    }

    public static OperationStatus createWarning(int code, String message) {
        return new OperationStatus(Status.WARNING, code, message);
    }

    public static OperationStatus createError(int code, String message) {
        return new OperationStatus(Status.ERROR, code, message);
    }

    public Status getStatus() {
        return status;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
