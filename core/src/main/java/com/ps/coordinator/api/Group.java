package com.ps.coordinator.api;

import java.util.List;

public interface Group {

    String getName();

    Group setName(String type);

    String getType();

    Group setType(String type);

    String getEndpoint();

    Member setEndpoint(String endpoint);

    List<Member> getMembers();

    boolean isAvailable();

}
