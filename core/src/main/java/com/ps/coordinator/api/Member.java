package com.ps.coordinator.api;

public interface Member {

    String getName();

    Member setName(String type);

    String getNode();

    Member setNode(String node);

    String getType();

    Member setType(String type);

    String getEndpoint();

    Member setEndpoint(String endpoint);

    boolean isAvailable();

}