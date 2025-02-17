package M3;

/*
Challenge 2: Simple Slash Command Handler
-----------------------------------------
- Accept user input as slash commands
  - "/greet <name>" → Prints "Hello, <name>!"
  - "/roll <num>d<sides>" → Roll <num> dice with <sides> and returns a single outcome as "Rolled <num>d<sides> and got <result>!"
  - "/echo <message>" → Prints the message back
  - "/quit" → Exits the program
- Commands are case-insensitive
- Print an error for unrecognized commands
- Print errors for invalid command formats (when applicable)
- Capture 3 variations of each command except "/quit"
*/

import java.util.Scanner;

public class SlashCommandHandler extends BaseClass {
    private static String ucid = "mt85"; // <-- change to your UCID

    public static void main(String[] args) {
        printHeader(ucid, 2, "Objective: Implement a simple slash command parser.");

        Scanner scanner = new Scanner(System.in);

        // Can define any variables needed here

        while (true) {
            System.out.print("Enter command: ");
            // get entered text

            // check if greet
            //// process greet

            // check if roll
            //// process roll
            //// handle invalid formats

            // check if echo
            //// process echo

            // check if quit
            //// process quit

            // handle invalid commnads

            // delete this condition/block, it's just here so the sample runs without edits
            if (1 == 1) {
                System.out.println("Breaking loop");
                break;
            }
        }

        printFooter(ucid, 2);
        scanner.close();
    }
}
