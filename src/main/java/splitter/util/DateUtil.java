package splitter.util;

import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DateUtil {
    public static final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public static boolean isDate(String date) {
        Pattern pattern = Pattern.compile("\\s*\\d{4}\\.\\d{2}\\.\\d{2}\\s*");
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }
}