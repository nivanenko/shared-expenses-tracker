package splitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import splitter.model.Group;
import splitter.model.User;
import splitter.model.UserGroup;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByName(String groupName);
    List<Group> findAllByName(String groupName);

    @Modifying
    @Query("DELETE FROM UserGroup ug " +
            "WHERE ug.user IN :users " +
            "AND ug.group = :group")
    int removeUsersFromGroup(@Param("group") Group group, @Param("users") Set<User> users);

    @Query("SELECT u FROM User u " +
            "JOIN UserGroup ug ON u.id = ug.user.id " +
            "JOIN Group g ON ug.group.id = g.id " +
            "WHERE g.name = :groupName")
    Set<User> findUsersByGroupName(@Param("groupName") String groupName);

    @Query("SELECT ug.user " +
            "FROM UserGroup ug " +
            "JOIN ug.group g " +
            "WHERE g = :group")
    Set<User> findUsers(@Param("group") Group group);

    boolean existsByName(String name);
}
