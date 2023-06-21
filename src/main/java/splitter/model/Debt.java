package splitter.model;

import java.math.BigDecimal;
import java.util.Objects;

public final class Debt {
    private final User borrower;
    private final User lender;
    private BigDecimal amount;

    public Debt(User borrower, User lender, BigDecimal amount) {
        this.borrower = borrower;
        this.lender = lender;
        this.amount = amount;
    }

    public User borrower() {
        return borrower;
    }

    public User lender() {
        return lender;
    }

    public BigDecimal amount() {
        return amount;
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

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Debt) obj;
        return Objects.equals(this.borrower, that.borrower) &&
                Objects.equals(this.lender, that.lender) &&
                Objects.equals(this.amount, that.amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(borrower, lender, amount);
    }

    @Override
    public String toString() {
        return "Debt[" +
                "borrower=" + borrower + ", " +
                "lender=" + lender + ", " +
                "amount=" + amount + ']';
    }
}