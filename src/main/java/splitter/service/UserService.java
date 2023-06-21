package splitter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import splitter.model.User;
import splitter.repository.UserRepository;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;

import static splitter.util.ParseUtil.groupNameWithSignPattern;
import static splitter.util.ParseUtil.nameWithSignPattern;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final GroupService groupService;

    @Autowired
    public UserService(UserRepository userRepository, GroupService groupService) {
        this.userRepository = userRepository;
        this.groupService = groupService;
    }

    /**
     * Fetches a user by name or creates a new user if not found.
     *
     * @param name User name
     * @return User fetched or created
     */
    public User getOrCreateUserByName(String name) {
        return userRepository.findByName(name).orElseGet(() -> userRepository.save(new User(name)));
    }

    /**
     * Filters users and groups based on the input names.
     *
     * @param names List of user and group names
     * @return Set of filtered users
     */
    public Set<User> filterUsersAndGroups(List<String> names) {
        Set<User> includedUsers = new TreeSet<>();
        Set<User> excludedUsers = new TreeSet<>();

        for (String name : names) {
            processNameForIncludedAndExcludedUsers(name, includedUsers, excludedUsers, false);
        }
        includedUsers.removeAll(excludedUsers);
        return includedUsers;
    }

    /**
     * Filters users and groups to be removed based on the input names.
     *
     * @param names List of user and group names
     * @return Set of filtered users to be removed
     */
    public Set<User> filterUsersAndGroupsToRemove(List<String> names) {
        Set<User> includedUsers = new TreeSet<>();
        Set<User> excludedUsers = new TreeSet<>();

        for (String name : names) {
            processNameForIncludedAndExcludedUsers(name, includedUsers, excludedUsers, true);
        }
        excludedUsers.removeAll(includedUsers);
        return excludedUsers.isEmpty() ? includedUsers : excludedUsers;
    }

    /**
     * Processes the name for inclusion or exclusion based on the name sign.
     *
     * @param name Name to be processed
     * @param includedUsers Set of users to be included
     * @param excludedUsers Set of users to be excluded
     * @param isRemoveOperation Flag indicating if it is a remove operation
     */
    private void processNameForIncludedAndExcludedUsers(String name,
                                                        Set<User> includedUsers,
                                                        Set<User> excludedUsers,
                                                        boolean isRemoveOperation) {
        Matcher matcher = groupNameWithSignPattern.matcher(name);
        boolean shouldExclude = name.startsWith("-") || (isRemoveOperation && name.startsWith("+"));
        String strippedName = name.replaceAll("[-+]", "");

        if (matcher.matches()) {
            handleNameMatching(shouldExclude, strippedName, includedUsers, excludedUsers, true);
        } else if (nameWithSignPattern.matcher(name).matches()) {
            handleNameMatching(shouldExclude, strippedName, includedUsers, excludedUsers, false);
        }
    }

    /**
     * Handles the logic of adding users to the included or excluded sets.
     *
     * @param shouldExclude Flag indicating if the user should be excluded
     * @param strippedName Name without the sign
     * @param includedUsers Set of users to be included
     * @param excludedUsers Set of users to be excluded
     * @param isGroup Flag indicating if the name is a group name
     */
    private void handleNameMatching(boolean shouldExclude,
                                    String strippedName,
                                    Set<User> includedUsers,
                                    Set<User> excludedUsers,
                                    boolean isGroup) {
        if (shouldExclude) {
            if (isGroup) {
                excludedUsers.addAll(groupService.getUsersByGroupName(strippedName));
            } else {
                excludedUsers.add(getOrCreateUserByName(strippedName));
            }
        } else {
            if (isGroup) {
                includedUsers.addAll(groupService.getUsersByGroupName(strippedName));
            } else {
                includedUsers.add(getOrCreateUserByName(strippedName));
            }
        }
    }
}