package splitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import splitter.model.User;

import java.util.Optional;

@Repository
public interface UserRepository
        extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
}