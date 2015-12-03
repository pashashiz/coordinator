package com.ps.coordinator.api;

public interface RegistrationAndDiscoveryServiceInteractive extends RegistrationAndDiscoveryService {

    interface EventListener {

        void onMemberRegistered(Member member);

        void onMemberUnregistered(Member member);

        void onMemberAvailable(Member member);

        void onMemberUnavailable(Member member);

        void onGroupCreated(Group group);

        void onGroupRebalanced(Group group);

        void onGroupAvailable(Group group);

        void onGroupUnavailable(Group group);

        void onGroupRemoved(Group group);

    }

    String addEventListener(EventListener listener);

    void removeEventListener(String id);

}
