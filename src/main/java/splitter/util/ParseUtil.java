package splitter.util;

import org.springframework.stereotype.Component;
import splitter.model.Debt;
import splitter.model.User;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class ParseUtil {
    public static final Pattern balancePattern = Pattern.compile(
            "(\\s*\\d{4}\\.\\d{2}\\.\\d{2})?\\s*balance\\s?(open|close)?\\s*(\\([-+]?\\w+(,\\s*\\w+)*(,\\s[-+]?\\w+)*\\))?\\s*");
    public static final Pattern balancePerfectPattern = Pattern.compile(
            "(\\s*\\d{4}\\.\\d{2}\\.\\d{2})?\\s*balancePerfect\\s*(open|close)?\\s*");
    public static final Pattern borrowPattern = Pattern.compile(
            "(\\s*\\d{4}\\.\\d{2}\\.\\d{2})?\\s*borrow+\\s+[\\w+]+\\s+[\\w+]+\\s+\\d+\\.?\\d{0,2}\\s*");
    public static final Pattern addGroupPattern = Pattern.compile(
            "\\s*group\\s+add\\s+[A-Z]+\\s+\\([-+]?\\w+(,\\s*\\w+)*(,\\s[-+]?\\w+)*\\)\\s*");
    public static final Pattern createGroupPattern = Pattern.compile(
            "\\s*group\\s+create\\s+[A-Z]+\\s+\\([-+]?\\w+(,\\s*\\w+)*(,\\s[-+]?\\w+)*\\)\\s*");
    public static final Pattern removeFromGroupPattern = Pattern.compile(
            "\\s*group\\s+remove\\s+[A-Z]+\\s+\\([-+]?\\w+(,\\s*\\w+)*(,\\s[-+]?\\w+)*\\)\\s*");
    public static final Pattern showGroupPattern = Pattern.compile(
            "\\s*group\\s+show\\s+[A-Z]+\\s*");
    public static final Pattern purchasePattern = Pattern.compile(
            "(\\s*\\d{4}\\.\\d{2}\\.\\d{2})?\\s*purchase\\s+[\\w+]+\\s+[\\w+]+\\s+\\d+(\\.\\d{2})?\\s+\\([-+]?\\w+(,\\s*\\w+)*(,\\s[-+]?\\w+)*\\)\\s*");
    public static final Pattern repayPattern = Pattern.compile(
            "(\\s*\\d{4}\\.\\d{2}\\.\\d{2})?\\s*repay+\\s+[\\w+]+\\s+[\\w+]+\\s+\\d+\\.?\\d{0,2}\\s*");
    public static final Pattern secretSantaPattern = Pattern.compile(
            "secretSanta\\s+[A-Z]+\\s*");
    public static final Pattern cashBackPattern = Pattern.compile(
            "(\\s*\\d{4}\\.\\d{2}\\.\\d{2})?\\s*cashBack\\s+[\\w+]+\\s+[\\w+]+\\s+\\d+(\\.\\d{2})?\\s+\\([-+]?\\w+(,\\s*\\w+)*(,\\s[-+]?\\w+)*\\)\\s*");
    public static final Pattern writeOffPattern = Pattern.compile(
            "(\\s*\\d{4}\\.\\d{2}\\.\\d{2})?\\s*writeOff\\s*");
    public static final Pattern groupNameWithSignPattern = Pattern.compile("[-+]?[A-Z]+");
    public static final Pattern nameWithSignPattern = Pattern.compile("[-+]?\\w+");

    public static List<String> getValidatedInput(String input, Pattern pattern) throws IllegalArgumentException {
        Matcher matcher = pattern.matcher(input);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Illegal command arguments");
        }
        return Arrays.stream(input.split("\\s+")).filter(s -> !s.isEmpty()).collect(Collectors.toList());
    }

    public static List<String> retrieveNamesFromParentheses(String input) {
        Pattern pattern = Pattern.compile("\\((.*?)\\)");
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            String[] names = matcher.group(1).split(",");
            List<String> trimmedNames = new ArrayList<>(names.length);
            for (String name : names) {
                trimmedNames.add(name.trim());
            }
            return trimmedNames;
        } else {
            return List.of();
        }
    }

    public static boolean isGroupName(String name) {
        Matcher matcher = groupNameWithSignPattern.matcher(name);
        return matcher.matches();
    }

    public static Set<String> extractUserNames(Set<User> users) {
        return users.stream()
                .map(User::getName)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    public static String formatDebts(List<Debt> debts) {
        StringJoiner result = new StringJoiner("\n");
        for (Debt debt : debts) {
            result.add(String.format("%s owes %s %s", debt.borrower().getName(), debt.lender().getName(), debt.amount()));
        }
        return result.toString();
    }
}