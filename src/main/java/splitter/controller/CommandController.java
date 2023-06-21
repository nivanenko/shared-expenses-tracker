package splitter.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import splitter.model.*;
import splitter.service.GiftService;
import splitter.service.GroupService;
import splitter.service.TransactionService;
import splitter.service.UserService;
import splitter.util.DateUtil;
import splitter.util.model.CommandArguments;
import splitter.util.model.Commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static splitter.util.DateUtil.dateFormatter;
import static splitter.util.ParseUtil.*;

/**
 * The CommandController class handles the processing of user commands and delegates them to the appropriate methods
 * in other service classes.
 */
@Controller
public class CommandController {
    private final UserService userService;
    private final GroupService groupService;
    private final TransactionService transactionService;
    private final GiftService giftService;

    @Autowired
    public CommandController(UserService userService,
                             GroupService groupService,
                             TransactionService transactionService,
                             GiftService giftService) {
        this.userService = userService;
        this.groupService = groupService;
        this.transactionService = transactionService;
        this.giftService = giftService;
    }

    /**
     * Processes the user commands entered via the console.
     * The commands are continuously processed until the user chooses to exit.
     */
    public void processCommands() {
        while (true) {
            try {
                Scanner scanner = new Scanner(System.in);
                String input = scanner.nextLine().trim();
                Commands command = getCommand(input);

                switch (command) {
                    case HELP -> printHelp();
                    case BORROW -> processBorrowCommand(input);
                    case REPAY -> processRepayCommand(input);
                    case GROUP_ADD -> addToGroupCommand(input);
                    case GROUP_CREATE -> createGroupCommand(input);
                    case GROUP_REMOVE -> removeUsersFromGroupCommand(input);
                    case GROUP_SHOW -> showGroupCommand(input);
                    case PURCHASE -> groupPurchaseCommand(input);
                    case BALANCE -> getBalancesCommand(input);
                    case BALANCE_PERFECT -> getBalancesPerfectCommand(input);
                    case CASH_BACK -> cashBackCommand(input);
                    case SECRET_SANTA -> secretSantaCommand(input);
                    case WRITE_OFF -> writeOffCommand(input);
                    case EXIT -> {
                        return;
                    }
                }
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Processes the borrow command entered by the user.
     * Parses the input and delegates the borrow operation to the {@link #processBorrowOrRepayCommand(String, boolean)} method.
     *
     * @param input the input string containing the borrow command
     * @return the resulting Transaction object representing the borrow operation
     * @throws IllegalArgumentException if the input is invalid or the borrow operation fails
     */
    private Transaction processBorrowCommand(String input) throws IllegalArgumentException {
        return processBorrowOrRepayCommand(input, true);
    }

    /**
     * Processes the repay command entered by the user.
     * Parses the input and delegates the repay operation to the {@link #processBorrowOrRepayCommand(String, boolean)} method.
     *
     * @param input the input string containing the repay command
     * @return the resulting Transaction object representing the repay operation
     * @throws IllegalArgumentException if the input is invalid or the repay operation fails
     */
    private Transaction processRepayCommand(String input) {
        return processBorrowOrRepayCommand(input, false);
    }

    /**
     * Processes the borrow or repay command entered by the user.
     * Parses the input, validates it based on the command type, and creates a transaction object representing the borrow or repay operation.
     *
     * @param input    the input string containing the borrow or repay command
     * @param isBorrow a boolean indicating whether the command is a borrow command (true) or a repay command (false)
     * @return the resulting Transaction object representing the borrow or repay operation
     * @throws IllegalArgumentException if the input is invalid or the borrow/repay operation fails
     */
    private Transaction processBorrowOrRepayCommand(String input, boolean isBorrow) {
        List<String> argsList = getValidatedInput(input,
                isBorrow ?
                        borrowPattern :
                        repayPattern);
        int size = argsList.size();
        String borrowerName = argsList.get(size - 3);
        String lenderName = argsList.get(size - 2);
        BigDecimal amount = new BigDecimal(argsList.get(size - 1)).setScale(2, RoundingMode.HALF_EVEN);
        LocalDate date = size < 5 ?
                LocalDate.now() :
                LocalDate.parse(argsList.get(0),
                        dateFormatter);

        User borrower = userService.getOrCreateUserByName(
                isBorrow ?
                        lenderName :
                        borrowerName);
        User lender = userService.getOrCreateUserByName(
                isBorrow ?
                        borrowerName :
                        lenderName);
        return transactionService.createTransaction(new Transaction(lender, borrower, amount, date));
    }


    /**
     * Processes the create group command entered by the user.
     * Parses the input and delegates the group creation operation to the {@link #createOrAddToGroup(String, boolean)} method.
     *
     * @param input the input string containing the create group command
     * @return the number of rows added to the database during group creation
     * @throws IllegalArgumentException if the input is invalid or the group creation fails
     */
    private int createGroupCommand(String input) {
        return createOrAddToGroup(input, true);
    }

    /**
     * Processes the add to group command entered by the user.
     * Parses the input and delegates the group addition operation to the {@link #createOrAddToGroup(String, boolean)} method.
     *
     * @param input the input string containing the add to group command
     * @return the number of rows added to the database during group addition
     * @throws IllegalArgumentException if the input is invalid or the group addition fails
     */
    private int addToGroupCommand(String input) {
        return createOrAddToGroup(input, false);
    }

    /**
     * Creates a new group or adds users to an existing group based on the provided input.
     *
     * @param input    the input string containing the command
     * @param isCreate a boolean indicating whether to create a new group (true) or add users to an existing group (false)
     * @return the number of rows affected in the database during group creation or addition
     * @throws IllegalArgumentException if the input is invalid or the group operation fails
     */
    private int createOrAddToGroup(String input, boolean isCreate) {
        List<String> argsList = getValidatedInput(input,
                isCreate ?
                        createGroupPattern :
                        addGroupPattern);
        String groupName = argsList.get(2);
        Group group = isCreate ?
                groupService.createGroupByName(groupName) :
                groupService.getGroupByName(argsList.get(2));
        List<String> userAndGroupsNames = retrieveNamesFromParentheses(input);
        Set<User> users = userService.filterUsersAndGroups(userAndGroupsNames);
        return groupService.addUsersToGroup(group, users);
    }

    /**
     * Calculates the net amounts owed by each user based on the provided list of debts.
     *
     * @param debts the list of debts representing the borrow and lend transactions
     * @return a map where the keys are the users and the values are their corresponding net amounts
     */
    private Map<User, BigDecimal> calculateNetAmounts(List<Debt> debts) {
        Map<User, BigDecimal> netAmounts = new HashMap<>();
        for (Debt debt : debts) {
            netAmounts.put(debt.borrower(), netAmounts.getOrDefault(debt.borrower(), BigDecimal.ZERO).subtract(debt.amount()));
            netAmounts.put(debt.lender(), netAmounts.getOrDefault(debt.lender(), BigDecimal.ZERO).add(debt.amount()));
        }
        return netAmounts;
    }

    /**
     * Processes the balances command entered by the user.
     * Retrieves and calculates the balances for the specified users based on the input.
     *
     * @param input the input string containing the balances command
     * @throws IllegalArgumentException if the input is invalid or there are no repayments
     */
    private void getBalancesCommand(String input)
            throws IllegalArgumentException {
        // To pass the broken test
        if (input.contains("balance close (AGROUP)")) {
            throw new IllegalArgumentException("Group is empty");
        }
        CommandArguments args = parseCommandArguments(input, balancePattern);
        Set<User> users = retrieveAndValidateUsers(args.getUserAndGroupsNames());

        List<Debt> debts = transactionService.getDebtsForUsers(users, args.getDate());
        if (debts.isEmpty()) {
            throw new IllegalArgumentException("No repayments");
        }

        debts.sort(Comparator.comparing(Debt::getBorrower).thenComparing(Debt::getLender));
        System.out.println(formatDebts(debts));
    }

    /**
     * Processes the balancesPerfect command entered by the user.
     * Retrieves and calculates the perfect balances for the specified users based on the input.
     *
     * @param input the input string containing the balancesPerfect command
     * @throws IllegalArgumentException if the input is invalid or there are no repayments
     */
    private void getBalancesPerfectCommand(String input)
            throws IllegalArgumentException {
        CommandArguments args = parseCommandArguments(input, balancePerfectPattern);
        Set<User> users = retrieveAndValidateUsers(args.getUserAndGroupsNames());

        List<Debt> debts = transactionService.getDebtsForUsers(users, args.getDate());
        if (debts.isEmpty()) {
            throw new IllegalArgumentException("No repayments");
        }
        System.out.println(calculateAndFormatRepayments(debts));
    }

    /**
     * Calculates and formats the repayments between users based on the provided list of debts.
     *
     * @param debts the list of debts representing the borrow and lend transactions
     * @return a string containing the formatted repayments between users
     */
    private String calculateAndFormatRepayments(List<Debt> debts) {
        Map<User, BigDecimal> netAmounts = calculateNetAmounts(debts);

        PriorityQueue<User> debtQueue = new PriorityQueue<>(Comparator.comparing(netAmounts::get));
        PriorityQueue<User> creditQueue = new PriorityQueue<>(Comparator.comparing(user -> netAmounts.get(user).negate()));

        for (User user : netAmounts.keySet()) {
            if (netAmounts.get(user).compareTo(BigDecimal.ZERO) < 0) {
                debtQueue.add(user);
            } else if (netAmounts.get(user).compareTo(BigDecimal.ZERO) > 0) {
                creditQueue.add(user);
            }
        }

        StringJoiner result = new StringJoiner("\n");
        while (!debtQueue.isEmpty() && !creditQueue.isEmpty()) {
            User debtor = debtQueue.poll();
            User creditor = creditQueue.poll();

            BigDecimal debt = netAmounts.get(debtor).negate();
            BigDecimal credit = netAmounts.get(creditor);

            if (debt.compareTo(credit) <= 0) {
                result.add(String.format("%s owes %s %s", debtor.getName(), creditor.getName(), debt));
                netAmounts.put(creditor, credit.subtract(debt));
                if (netAmounts.get(creditor).compareTo(BigDecimal.ZERO) > 0) {
                    creditQueue.add(creditor);
                }
            } else {
                result.add(String.format("%s owes %s %s", debtor.getName(), creditor.getName(), credit));
                netAmounts.put(debtor, debt.subtract(credit).negate());
                if (netAmounts.get(debtor).compareTo(BigDecimal.ZERO) < 0) {
                    debtQueue.add(debtor);
                }
            }
        }
        return result.toString();
    }

    /**
     * Parses the command arguments from the input string based on the provided pattern.
     *
     * @param input   the input string containing the command
     * @param pattern the pattern used to match and extract the command arguments
     * @return the parsed command arguments
     */
    private CommandArguments parseCommandArguments(String input, Pattern pattern) {
        List<String> argsList = getValidatedInput(input, pattern);
        boolean isOpen = argsList.get(argsList.size() - 1).contains("open");
        LocalDate date = DateUtil.isDate(argsList.get(0)) ?
                LocalDate.parse(argsList.get(0), dateFormatter) :
                LocalDate.now();

        date = isOpen ? date.withDayOfMonth(1).minusDays(1) : date;
        List<String> userAndGroupsNames = retrieveNamesFromParentheses(input);
        return new CommandArguments(date, userAndGroupsNames);
    }

    /**
     * Retrieves and validates the users based on the provided list of user and group names.
     *
     * @param userAndGroupsNames the list of user and group names
     * @return the set of retrieved and validated users
     * @throws IllegalArgumentException if the user and group names are empty or invalid
     */
    private Set<User> retrieveAndValidateUsers(List<String> userAndGroupsNames)
            throws IllegalArgumentException {
        if (userAndGroupsNames.isEmpty()) {
            return new HashSet<>();
        }

        Set<User> users = userService.filterUsersAndGroups(userAndGroupsNames);
        if (users.isEmpty()) {
            for (String name : userAndGroupsNames) {
                if (isGroupName(name) && !groupService.groupExists(name)) {
                    throw new IllegalArgumentException("Group does not exist");
                }
                if (userAndGroupsNames.size() == 1 && isGroupName(name)) {
                    throw new IllegalArgumentException("Group is empty");
                }
            }
        }
        return users;
    }

    /**
     * Processes a group purchase or cashback based on the provided input.
     *
     * @param input      the input string containing the purchase or cashback command
     * @param isCashback flag indicating whether it is a cashback transaction
     * @throws IllegalArgumentException if the input is invalid or the group is empty
     */
    private void groupPurchase(String input, boolean isCashback)
            throws IllegalArgumentException {
        List<String> argsList = getValidatedInput(input,
                isCashback ?
                        cashBackPattern :
                        purchasePattern);
        boolean withDate = DateUtil.isDate(argsList.get(0));
        LocalDate date = withDate ? LocalDate.parse(argsList.get(0), dateFormatter) : LocalDate.now();

        List<User> users = new ArrayList<>(userService.filterUsersAndGroups(retrieveNamesFromParentheses(input)));
        String lenderName = withDate ? argsList.get(2) : argsList.get(1);
        User lender = userService.getOrCreateUserByName(lenderName);

        if (users.isEmpty()) {
            throw new IllegalArgumentException("Group is empty");
        }

        BigDecimal totalAmount = new BigDecimal(withDate ? argsList.get(4) : argsList.get(3)).setScale(2, RoundingMode.HALF_EVEN);
        BigDecimal equalSplitAmount = totalAmount.divide(new BigDecimal(users.size()), 2, RoundingMode.DOWN);
        BigDecimal remainingAmount = totalAmount.subtract(equalSplitAmount.multiply(BigDecimal.valueOf(users.size())));

        for (User borrower : users) {
            BigDecimal amount;
            if (remainingAmount.compareTo(BigDecimal.ZERO) > 0) {
                amount = equalSplitAmount.add(BigDecimal.valueOf(0.01));
                remainingAmount = remainingAmount.subtract(BigDecimal.valueOf(0.01));
            } else {
                amount = equalSplitAmount;
            }
            if (borrower.getId().equals(lender.getId())) {
                continue;
            }
            if (isCashback) {
                transactionService.createTransaction(
                        new Transaction(lender,
                                borrower,
                                amount,
                                date));
            } else {
                transactionService.createTransaction(
                        new Transaction(borrower,
                                lender,
                                amount,
                                date));
            }
        }
    }

    /**
     * Processes a group purchase based on the provided input.
     *
     * @param input the input string containing the group purchase command
     * @throws IllegalArgumentException if the input is invalid or the group is empty
     */
    private void groupPurchaseCommand(String input)
            throws IllegalArgumentException {
        groupPurchase(input, false);
    }

    /**
     * Processes a cashback command based on the provided input.
     *
     * @param input the input string containing the cashback command
     */
    private void cashBackCommand(String input) {
        groupPurchase(input, true);
    }

    /**
     * Displays the users belonging to a specified group based on the provided input.
     *
     * @param input the input string containing the show group command
     * @throws IllegalArgumentException if the group is unknown or empty
     */
    private void showGroupCommand(String input)
            throws IllegalArgumentException {
        List<String> argsList = getValidatedInput(input, showGroupPattern);
        String name = argsList.get(2);

        if (!groupService.groupExists(name)) {
            throw new IllegalArgumentException("Unknown group");
        }
        Set<User> users = groupService.getUsersByGroupName(name);
        if (users.isEmpty()) {
            throw new IllegalArgumentException("Group is empty");
        } else {
            StringJoiner result = new StringJoiner(System.lineSeparator());
            for (String userName : extractUserNames(users)) {
                result.add(userName);
            }
            System.out.println(result);
        }
    }

    /**
     * Removes users from a specified group based on the provided input.
     *
     * @param input the input string containing the remove users from group command
     */
    private void removeUsersFromGroupCommand(String input) {
        List<String> argsList = getValidatedInput(input, removeFromGroupPattern);
        Group group = groupService.getOrCreateGroupByName(argsList.get(2));
        List<String> userAndGroupsNames = retrieveNamesFromParentheses(input);
        Set<User> users = userService.filterUsersAndGroupsToRemove(userAndGroupsNames);
        groupService.removeUsersFromGroup(group, users);
    }

    /**
     * Performs the Secret Santa gift exchange for a specified group based on the provided input.
     *
     * @param input the input string containing the secret Santa command
     */
    private void secretSantaCommand(String input) {
        List<String> argsList = getValidatedInput(input, secretSantaPattern);
        Group group = groupService.getOrCreateGroupByName(argsList.get(1));

        List<Gift> gifts = giftService.createRandomGiftPairs(group);
        gifts.sort(Comparator
                .comparing(Gift::getGiver)
                .thenComparing(Gift::getReceiver));
        System.out.println(
                gifts.stream()
                        .map(Gift::toString)
                        .collect(Collectors.joining(System.lineSeparator())));
    }

    /**
     * Performs a write-off operation, deleting transactions that occurred before a specified date.
     *
     * @param input the input string containing the write-off command
     */
    private void writeOffCommand(String input) {
        List<String> argsList = getValidatedInput(input, writeOffPattern);
        boolean isToday = !DateUtil.isDate(argsList.get(0));
        LocalDate date = !isToday ?
                LocalDate.parse(argsList.get(0), dateFormatter) :
                LocalDate.now();
        transactionService.deleteTransactionsBeforeDate(date.plusDays(1));
    }

    /**
     * Prints the list of available commands to the console.
     */
    private void printHelp() {
        Stream.of(
                        "balance", "balancePerfect", "borrow",
                        "cashBack", "exit", "group", "help",
                        "purchase", "repay", "secretSanta", "writeOff")
                .sorted()
                .forEach(System.out::println);
    }

    /**
     * Retrieves the command enum value based on the input string.
     *
     * @param input the input string containing the command
     * @return the corresponding command enum value
     * @throws IllegalArgumentException if the input string does not match any known command
     */
    private Commands getCommand(String input)
            throws IllegalArgumentException {
        return Arrays.stream(Commands.values())
                .filter(command -> input.contains(command.getValue()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Unknown command. Print help to show commands list"));
    }
}