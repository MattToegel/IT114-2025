package NDFF.Common;

/**
 * Utility to provide colored text for terminal or HTML output.
 * <p>
 * Important: This does not satisfy the full text formatting feature/requirement
 * for chatroom projects.
 * </p>
 */
public abstract class TextFX {

    /**
     * List of available text colors, with both terminal (ANSI escape codes) and
     * HTML hex color values.
     */
    public enum Color {
        BLACK("\033[0;30m", "#000000"),
        RED("\033[0;31m", "#d32f2f"),
        GREEN("\033[0;32m", "#388e3c"),
        YELLOW("\033[0;33m", "#fbc02d"),
        BLUE("\033[0;34m", "#1976d2"),
        PURPLE("\033[0;35m", "#8e24aa"),
        CYAN("\033[0;36m", "#00838f"),
        WHITE("\033[0;37m", "#fafafa");

        private final String ansiCode;
        private final String htmlColor;

        /**
         * @param ansiCode  ANSI escape code for terminal colorization.
         * @param htmlColor HTML hex color string for use in HTML output.
         */
        Color(String ansiCode, String htmlColor) {
            this.ansiCode = ansiCode;
            this.htmlColor = htmlColor;
        }

        /**
         * Gets the ANSI escape code for terminal colorization.
         * 
         * @return ANSI color code string.
         */
        public String getAnsiCode() {
            return ansiCode;
        }

        /**
         * Gets the HTML hex color code.
         * 
         * @return HTML hex color string.
         */
        public String getHtmlColor() {
            return htmlColor;
        }
    }

    /** ANSI reset code for terminal output */
    public static final String RESET = "\033[0m";

    /**
     * Generates a string with the original message wrapped in the ANSI color and
     * RESET.
     * <p>
     * Note: May not work for all terminals.
     * </p>
     * <p>
     * Important: This does not satisfy the full text formatting feature/requirement
     * for chatroom projects.
     * </p>
     *
     * @param text  Input text to colorize.
     * @param color Enum value of Color choice from TextFX.Color.
     * @return String wrapped with ANSI color and RESET codes.
     */
    public static String colorize(String text, Color color) {
        return colorize(text, color, false);
    }

    /**
     * Generates a colorized string for terminal (ANSI) or HTML output.
     * <p>
     * - If isHtml is false, wraps text with ANSI codes (for terminal use).
     * - If isHtml is true, wraps text with HTML span and style (for JEditorPane,
     * etc).
     * </p>
     *
     * @param text   Input text to colorize.
     * @param color  Enum value of Color choice from TextFX.Color.
     * @param isHtml True for HTML output (e.g., JEditorPane); false for terminal
     *               ANSI output.
     * @return Colorized string using ANSI or HTML.
     */
    public static String colorize(String text, Color color, boolean isHtml) {
        StringBuilder sb = new StringBuilder();
        if (isHtml) {
            // HTML-wrapped color string
            sb.append("<span style=\"color: ");
            sb.append(color.getHtmlColor());
            sb.append(";\">");
            sb.append(text);
            sb.append("</span>");
        } else {
            // Terminal ANSI color code
            sb.append(color.getAnsiCode());
            sb.append(text);
            sb.append(RESET);
        }
        return sb.toString();
    }

    /**
     * Example usage and demonstration.
     */
    public static void main(String[] args) {
        // Terminal output (if supported)
        System.out.println(TextFX.colorize("Hello, world!", Color.RED));
        System.out.println(TextFX.colorize("This is some blue text.", Color.BLUE));
        System.out.println(TextFX.colorize("And this is green!", Color.GREEN));

        // HTML output (for use in JEditorPane, etc)
        System.out.println(TextFX.colorize("This is HTML red!", Color.RED, true));
        // Example output: <span style="color: #d32f2f;">This is HTML red!</span>
    }
}
