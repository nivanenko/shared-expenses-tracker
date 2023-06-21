package splitter.util.model;

import java.time.LocalDate;
import java.util.List;

public class CommandArguments {
    private LocalDate date;
    private List<String> userAndGroupsNames;

    public CommandArguments(LocalDate date, List<String> userAndGroupsNames) {
        this.date = date;
        this.userAndGroupsNames = userAndGroupsNames;
    }

    public LocalDate getDate() {
        return date;
    }


    public List<String> getUserAndGroupsNames() {
        return userAndGroupsNames;
    }

}

