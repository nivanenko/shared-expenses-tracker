package splitter.util.model;

public enum Commands {
    BALANCE_PERFECT("balancePerfect"),
    BALANCE("balance"),
    BORROW("borrow"),
    CASH_BACK("cashBack"),
    EXIT("exit"),
    HELP("help"),
    GROUP_ADD("group add"),
    GROUP_CREATE("group create"),
    GROUP_SHOW("group show"),
    GROUP_REMOVE("group remove"),
    PURCHASE("purchase"),
    REPAY("repay"),
    SECRET_SANTA("secretSanta"),
    WRITE_OFF("writeOff");

    private final String value;

    Commands(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}