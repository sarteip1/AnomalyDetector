package uam.anomalydetector.common.helper;

public class RegexParser {

    public static String getRegexForPath(String path) {
        String regex = path.replaceAll("/", "\\\\/");
        regex = regex.replaceAll("\\{.*\\}", "(.*)");
        return regex;
    }

}
