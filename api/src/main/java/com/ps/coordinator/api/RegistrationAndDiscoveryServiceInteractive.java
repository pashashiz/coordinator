package com.ps.coordinator.api;

public interface RegistrationAndDiscoveryServiceInteractive extends RegistrationAndDiscoveryService {

    interface EventListener {

        void onMemberRegistered(Member member);

        void onMemberUnregistered(Member member);

        void onGroupCreated(Group group);

        void onGroupChanged(Group group);

        void onGroupRemoved(Group group);

    }

    String addEventListener(EventListener listener);

    void removeEventListener(String id);

}
