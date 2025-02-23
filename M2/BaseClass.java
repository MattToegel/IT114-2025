package M2;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

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

    /**
     * Generates a String with the original message wrapped in the ASCII of the
     * color and RESET
     * 
     * <p>
     * Note: May not work for all terminals
     * </p>
     * <p>
     * Important: This does not satisfy the text formatting feature/requirement for
     * chatroom projects.
     * </p>
     * 
     * @param text  Input text to colorize
     * @param color Enum of Color choice from TextFX.Color
     * @return wrapped String
     */
    public static String colorize(String text, Color color) {
        StringBuilder builder = new StringBuilder();
        builder.append(color.getCode());
        builder.append(text);
        builder.append(RESET);
        return builder.toString();
    }

    public static void printHeader(String ucid, int problem) {
        LocalDateTime currentDT = LocalDateTime.now();
        System.out.println(
                colorize(String.format("Running Problem %d for [%s] [%s]", problem, ucid, currentDT), Color.PURPLE));
        switch (problem) {
            case 1:
                System.out.println("Objective: Print out only odd values in a single line separate by commas");
                break;
            case 2:
                System.out.println("Objective: Print out the total sum of the passed array");
                break;
            case 3:
                System.out.println(
                        "Objective: Make each array value positive, convert it back to the original data type, and assign it to the proper slot in the `output` array");
                break;
            case 4:
                System.out.println(
                        "Objective: \n" +
                                "Challenge 1: Remove non-alphanumeric characters except spaces\n" +
                                "Challenge 2: Convert text to Title Case\n" +
                                "Challenge 3: Trim leading/trailing spaces and remove duplicate spaces\n" +
                                "Result 1-3: Assign final phrase to `placeholderForModifiedPhrase`\n" +
                                "Challenge 4: Extract middle 3 characters (beginning starts at middle of phrase),\n" +
                                "assign to 'placeholderForMiddleCharacters'\n" +
                                "if not enough characters assign \"Not enough characters\"");
                break;
            default:
                break;
        }
    }

    public static void printFooter(String ucid, int problem) {
        LocalDateTime currentDT = LocalDateTime.now();
        System.out.println(
                colorize(String.format("Completed Problem %d for [%s] [%s]", problem, ucid, currentDT), Color.PURPLE));
    }

    // overloads
    public static void printArrayInfo(int[] arr, int arrayNumber) {
        final String message = String.format("Problem %s: Original Array: %s", arrayNumber, Arrays.toString(arr));
        System.out.println(colorize(message, Color.BLUE));
    }

    public static void printArrayInfo(double[] arr, int arrayNumber) {
        final String message = String.format("Problem %s: Original Array: %s", arrayNumber, Arrays.toString(arr));
        System.out.println(colorize(message, Color.BLUE));
    }

    public static void printArrayInfo(Object[] arr, int arrayNumber) {
        final String message = String.format("Problem %s: Original Array:", arrayNumber);
        System.out.println(colorize(message, Color.BLUE));
        System.out.print(Color.BLUE.getCode());
        printOutputWithType(arr);

        System.out.println(RESET);
    }

    public static void printArrayInfoBasic(String[] arr, int arrayNumber) {
        final String message = String.format("Problem %s: Original Array: %s", arrayNumber, Arrays.toString(arr));
        System.out.println(colorize(message, Color.BLUE));
    }

    public static void printOutputWithType(Object[] arr) {
        List<Object> list = Arrays.asList(arr); // Handles null values safely
        Iterator<Object> iterator = list.iterator();

        while (iterator.hasNext()) {
            Object item = iterator.next();
            if (item == null) {
                System.out.println(colorize("Invalid value for output array", Color.RED));
                continue;
            }
            String o = String.format("%s[%s]", item, item.getClass().getSimpleName().substring(0, 1));
            System.out.print(o);
            if (iterator.hasNext()) {
                System.out.print(", "); // Not last element, add separator
            }
        }
    }

}