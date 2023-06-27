package splitter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import splitter.model.Group;
import splitter.model.User;
import splitter.model.UserGroup;
import splitter.repository.GroupRepository;
import splitter.repository.UserGroupRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class GroupService {
    private final GroupRepository groupRepository;
    private final UserGroupRepository userGroupRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository,
                        UserGroupRepository userGroupRepository) {
        this.groupRepository = groupRepository;
        this.userGroupRepository = userGroupRepository;
    }

    /**
     * Adds a set of users to a group.
     *
     * @param group the group to add the users to.
     * @param users the set of users to add to the group.
     * @return the number of users added to the group.
     */
    @Transactional
    public int addUsersToGroup(Group group, Set<User> users) {
        return users.stream()
                .map(user -> createUserGroup(group, user))
                .map(userGroupRepository::save)
                .toArray().length;
    }

    /**
     * Removes a set of users from a group.
     *
     * @param group the group to remove the users from.
     * @param users the set of users to remove from the group.
     * @return the number of users removed from the group.
     */
    @Transactional
    public int removeUsersFromGroup(Group group, Set<User> users) {
        return groupRepository.removeUsersFromGroup(group, users);
    }

    public Set<User> getUsersByGroupName(String name) {
        return groupRepository.findUsersByGroupName(name);
    }

    public Set<User> getUsers(Group group) {
        return groupRepository.findUsers(group);
    }

    public Group getOrCreateGroupByName(String name) {
        return groupRepository.findByName(name)
                .orElseGet(() -> groupRepository.save(new Group(name)));
    }

    public Group createGroupByName(String name) {
        if (groupExists(name)) deleteByName(name);
        return groupRepository.save(new Group(name));
    }

    public Group getGroupByName(String name) {
        Optional<Group> optionalGroup = groupRepository.findByName(name);
        return optionalGroup.orElse(null);
    }

    @Transactional
    public void deleteByName(String name) {
        List<Group> groups = groupRepository.findAllByName(name);
        for (Group group : groups) {
            List<UserGroup> userGroups = userGroupRepository.findAllByGroup(group);
            userGroupRepository.deleteAll(userGroups);  // This deletes user-group associations
        }
        groupRepository.deleteAll(groups);  // Then delete the groups
    }

    public boolean groupExists(String name) {
        return groupRepository.existsByName(name);
    }

    /**
     * Creates a new UserGroup instance with a specified group and user.
     *
     * @param group the group to set.
     * @param user  the user to set.
     * @return the newly created UserGroup instance.
     */
    private UserGroup createUserGroup(Group group, User user) {
        UserGroup userGroup = new UserGroup();
        userGroup.setGroup(group);
        userGroup.setUser(user);
        return userGroup;
    }
}