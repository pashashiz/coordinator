package com.ps.coordinator.hz;

import com.ps.coordinator.api.Group;
import com.ps.coordinator.api.Member;
import com.ps.coordinator.api.RegistrationAndDiscoveryServiceInteractive;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ListenerTracker {

    private RegistrationAndDiscoveryServiceInteractive.EventListener listener;
    private BlockingQueue<Member> memberRegistered = new LinkedBlockingQueue<>();
    private BlockingQueue<Member> memberUnregistered = new LinkedBlockingQueue<>();
    private BlockingQueue<Member> memberAvailable = new LinkedBlockingQueue<>();
    private BlockingQueue<Member> memberUnavailable = new LinkedBlockingQueue<>();
    private BlockingQueue<Group> groupCreated = new LinkedBlockingQueue<>();
    private BlockingQueue<Group> groupRebalanced = new LinkedBlockingQueue<>();
    private BlockingQueue<Group> groupAvailable = new LinkedBlockingQueue<>();
    private BlockingQueue<Group> groupUnavailable = new LinkedBlockingQueue<>();
    private BlockingQueue<Group> groupRemoved = new LinkedBlockingQueue<>();

    public ListenerTracker() {
        listener = new RegistrationAndDiscoveryServiceInteractive.EventListener() {
            @Override
            public void onMemberRegistered(Member member) {
                memberRegistered.add(member);
            }
            @Override
            public void onMemberUnregistered(Member member) {
                memberUnregistered.add(member);
            }
            @Override
            public void onMemberAvailable(Member member) {
                memberAvailable.add(member);
            }
            @Override
            public void onMemberUnavailable(Member member) {
                memberUnavailable.add(member);
            }
            @Override
            public void onGroupCreated(Group group) {
                groupCreated.add(group);
            }
            @Override
            public void onGroupRebalanced(Group group) {
                groupRebalanced.add(group);
            }
            @Override
            public void onGroupAvailable(Group group) {
                groupAvailable.add(group);
            }
            @Override
            public void onGroupUnavailable(Group group) {
                groupUnavailable.add(group);
            }
            @Override
            public void onGroupRemoved(Group group) {
                groupRemoved.add(group);
            }
        };
    }

    public RegistrationAndDiscoveryServiceInteractive.EventListener getListener() {
        return listener;
    }

    public Member takeMemberRegistered() throws InterruptedException {
        return memberRegistered.take();
    }

    public Member takeMemberUnregistered() throws InterruptedException {
        return memberUnregistered.take();
    }

    public Member takeMemberAvailable() throws InterruptedException {
        return memberAvailable.take();
    }

    public Member takeMemberUnavailable() throws InterruptedException {
        return memberUnavailable.take();
    }

    public Group takeGroupCreated() throws InterruptedException {
        return groupCreated.take();
    }

    public Group takeGroupRebalanced() throws InterruptedException {
        return groupRebalanced.take();
    }

    public Group takeGroupAvailable() throws InterruptedException {
        return groupAvailable.take();
    }

    public Group takeGroupUnavailable() throws InterruptedException {
        return groupUnavailable.take();
    }

    public Group takeGroupRemoved() throws InterruptedException {
        return groupRemoved.take();
    }

}
