package splitter.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "borrower_id")
    private User borrower;

    @ManyToOne
    @JoinColumn(name = "lender_id")
    private User lender;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "transaction_date")
    private LocalDate date;

    public Transaction() {
    }

    public Transaction(User borrower, User lender, BigDecimal amount, LocalDate date) {
        this.borrower = borrower;
        this.lender = lender;
        this.amount = amount;
        this.date = date;
    }

    public User getBorrower() {
        return borrower;
    }

    public User getLender() {
        return lender;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", borrower=" + borrower +
                ", lender=" + lender +
                ", amount=" + amount +
                ", date=" + date +
                '}';
    }
}