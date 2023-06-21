package splitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import splitter.model.Transaction;
import splitter.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    int deleteByDateBefore(LocalDate date);

    @Query("SELECT t " +
            "FROM Transaction t " +
            "WHERE t.date <= :date")
    List<Transaction> findAllByDateAndBefore(@Param("date") LocalDate date);

    @Query("SELECT t " +
            "FROM Transaction t " +
            "WHERE t.date <= :date " +
            "AND (t.borrower IN :users)")
    List<Transaction> findAllByDateAndBeforeForUsers(@Param("date") LocalDate date, @Param("users") Set<User> users);
}