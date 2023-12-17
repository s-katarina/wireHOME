package projectnwt2023.backend.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexPattern {

    public static boolean isStringMatchingPattern(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);

        Matcher matcher = pattern.matcher(input);

        return matcher.matches();
    }

}
