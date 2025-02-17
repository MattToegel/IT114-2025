package M3;

import java.time.LocalDateTime;

public class BaseClass {
    public enum Color {
        BLACK("\033[0;30m"),
        RED("\033[0;31m"),
        GREEN("\033[0;32m"),
        YELLOW("\033[0;33m"),
        BLUE("\033[0;34m"),
        PURPLE("\033[0;35m"),
        CYAN("\033[0;36m"),
        WHITE("\033[0;37m");

        private final String code;

        Color(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    public static final String RESET = "\033[0m";

    public static String colorize(String text, Color color) {
        return color.getCode() + text + RESET;
    }

    public static void printHeader(String ucid, int problem, String description) {
        LocalDateTime currentDT = LocalDateTime.now();
        System.out.println(
                colorize(String.format("Running Problem %d for [%s] [%s] \n %s", problem, ucid, currentDT, description),
                        Color.PURPLE));

    }

    public static void printFooter(String ucid, int problem) {
        LocalDateTime currentDT = LocalDateTime.now();
        System.out.println(
                colorize(String.format("Completed Problem %d for [%s] [%s]", problem, ucid, currentDT), Color.PURPLE));
    }
}
