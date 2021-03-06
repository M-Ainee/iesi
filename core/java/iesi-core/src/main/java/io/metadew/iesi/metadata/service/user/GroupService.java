package io.metadew.iesi.metadata.service.user;

import io.metadew.iesi.metadata.configuration.user.GroupConfiguration;
import io.metadew.iesi.metadata.definition.user.*;

import java.util.List;
import java.util.Optional;

public class GroupService {

    private static GroupService INSTANCE;

    public synchronized static GroupService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new GroupService();
        }
        return INSTANCE;
    }

    private GroupService() {
    }

    public boolean exists(GroupKey groupKey) {
        return GroupConfiguration.getInstance().exists(groupKey);
    }

    public boolean exists(String groupName) {
        return GroupConfiguration.getInstance().exists(groupName);
    }

    public List<Group> getAll() {
        return GroupConfiguration.getInstance().getAll();
    }

    public void addGroup(Group group) {
        GroupConfiguration.getInstance().insert(group);
    }

    public Optional<Group> get(GroupKey groupKey) {
        return GroupConfiguration.getInstance().get(groupKey);
    }

    public Optional<Group> get(String groupName) {
        return GroupConfiguration.getInstance().get(groupName);
    }

    public void update(Group group) {
        GroupConfiguration.getInstance().update(group);
    }

    public void delete(GroupKey groupKey) {
        GroupConfiguration.getInstance().delete(groupKey);
    }

    public void delete(String groupName) {
        GroupConfiguration.getInstance().delete(groupName);
    }

    public List<Authority> getAuthorities(GroupKey groupKey) {
        return GroupConfiguration.getInstance().getAuthorities(groupKey);
    }

    public List<Authority> getAuthorities(String groupName) {
        return GroupConfiguration.getInstance().getAuthorities(groupName);
    }

    public List<User> getUsers(GroupKey groupKey) {
        return GroupConfiguration.getInstance().getUsers(groupKey);
    }

    public List<User> getUsers(String groupName) {
        return GroupConfiguration.getInstance().getUsers(groupName);
    }

    public void addAuthority(GroupKey groupKey, AuthorityKey authorityKey) {
        GroupConfiguration.getInstance().addAuthority(groupKey, authorityKey);
    }

    public void addAuthority(String groupName, String authority) {
        GroupConfiguration.getInstance().addAuthority(groupName, authority);
    }

    public void removeAuthority(GroupKey groupKey, AuthorityKey authorityKey) {
        GroupConfiguration.getInstance().removeAuthority(groupKey, authorityKey);
    }

    public void removeAuthority(String groupName, String authority) {
        GroupConfiguration.getInstance().removeAuthority(groupName, authority);
    }

    public void addUser(GroupKey groupKey, UserKey userKey) {
        GroupConfiguration.getInstance().addUser(groupKey, userKey);
    }

    public void addUser(String groupName, String username) {
        GroupConfiguration.getInstance().addUser(groupName, username);
    }

    public void removeUser(GroupKey groupKey, UserKey userKey) {
        GroupConfiguration.getInstance().removeUser(groupKey, userKey);
    }

    public void removeUser(String groupName, String username) {
        GroupConfiguration.getInstance().removeUser(groupName, username);
    }

}
