package splitter.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import splitter.model.Debt;
import splitter.model.Transaction;
import splitter.model.User;
import splitter.repository.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;

    @Autowired
    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Retrieves debts for a set of users on a given date.
     *
     * @param users the users to get the debts for.
     * @param date  the date of the debts.
     * @return a list of debts for the users on the given date.
     */
    @Transactional
    public List<Debt> getDebtsForUsers(Set<User> users, LocalDate date) {
        return calculateDebts(date, users);
    }

    /**
     * Creates a new transaction.
     *
     * @param transaction the transaction to create.
     * @return the created transaction.
     */
    public Transaction createTransaction(Transaction transaction) {
        return transactionRepository.save(transaction);
    }

    /**
     * Deletes transactions before a given date.
     *
     * @param date the date to delete transactions before.
     * @return the number of transactions deleted.
     */
    @Transactional
    public int deleteTransactionsBeforeDate(LocalDate date) {
        return transactionRepository.deleteByDateBefore(date);
    }

    /**
     * Calculates the debts for a set of users on a given date.
     *
     * @param date  the date of the debts.
     * @param users the users to calculate the debts for.
     * @return a list of debts for the users on the given date.
     */
    private List<Debt> calculateDebts(LocalDate date, Set<User> users) {
        List<Transaction> transactions =
                users != null && !users.isEmpty() ?
                        transactionRepository.findAllByDateAndBeforeForUsers(date, users) :
                        transactionRepository.findAllByDateAndBefore(date);
        Map<String, Debt> debtMap = calculateDebtMap(transactions);
        return extractPositiveDebts(debtMap);
    }

    /**
     * Calculates a debt map from a list of transactions.
     *
     * @param transactions the transactions to calculate the debt map from.
     * @return a map of debts.
     */
    private Map<String, Debt> calculateDebtMap(List<Transaction> transactions) {
        Map<String, Debt> debtMap = new HashMap<>();
        for (Transaction transaction : transactions) {
            updateDebtsForTransaction(transaction, debtMap);
        }
        return debtMap;
    }

    /**
     * Updates the debts for a transaction.
     *
     * @param transaction the transaction to update the debts for.
     * @param debtMap     the map of debts to update.
     */
    private void updateDebtsForTransaction(Transaction transaction, Map<String, Debt> debtMap) {
        User borrower = transaction.getBorrower();
        User lender = transaction.getLender();
        BigDecimal amount = transaction.getAmount();

        String borrowerKey = borrower.getId() + "-" + lender.getId();
        Debt borrowerDebt = debtMap.getOrDefault(borrowerKey, new Debt(borrower, lender, BigDecimal.ZERO));
        borrowerDebt.setAmount(borrowerDebt.getAmount().add(amount));
        debtMap.put(borrowerKey, borrowerDebt);

        String lenderKey = lender.getId() + "-" + borrower.getId();
        Debt lenderDebt = debtMap.getOrDefault(lenderKey, new Debt(lender, borrower, BigDecimal.ZERO));
        lenderDebt.setAmount(lenderDebt.getAmount().subtract(amount));
        debtMap.put(lenderKey, lenderDebt);
    }

    /**
     * Extracts the positive debts from a debt map.
     *
     * @param debtMap the debt map to extract the positive debts from.
     * @return a list of positive debts.
     */
    private List<Debt> extractPositiveDebts(Map<String, Debt> debtMap) {
        return debtMap.values().stream()
                .filter(debt -> debt.getAmount().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
    }
}