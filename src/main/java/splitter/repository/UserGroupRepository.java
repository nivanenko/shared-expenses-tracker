package splitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import splitter.model.Group;
import splitter.model.UserGroup;

import java.util.List;

@Repository
public interface UserGroupRepository
        extends JpaRepository<UserGroup, Long> {
    List<UserGroup> findAllByGroup(Group group);
}
