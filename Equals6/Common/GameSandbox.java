package Equals6.Common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class GameSandbox {
    static Scanner scanner = new Scanner(System.in);
    static Board board = new Board();
    static int points = 0;
    static int round = 0;

    static void doRound() {
        // wait for input /cell x,y,value
        // apply to board
        // print grid
        // show points acquired
        // repeat until 10 points
        round++;
        System.out.println("Round " + round + ": Enter your action in the format: /cell x,y,value");
        String text = scanner.nextLine();
        String[] parts = text.split(" ");
        if (parts.length != 2) {
            System.out.println("Invalid input. Please enter in the format: /cell x,y,value");
            return;
        }
        String[] coordinates = parts[1].split(",");
        try {
            int x = Integer.parseInt(coordinates[0]);
            int y = Integer.parseInt(coordinates[1]);
            int value = Integer.parseInt(coordinates[2]);
            board.applyAction(x, y, value);
            board.print();
            int received = board.getPoints();
            System.out.println("Points: " + received);
            points += received;
            if (points >= 10) {
                System.out.println("You have reached 10 points! Game over.");
                return;
            } else {
                doRound();
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter valid integers for x, y, and value.");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid coordinates. Please enter valid x and y values.");
        } catch (Exception e) {
            System.out.println("An unexpected error occurred: " + e.getMessage());
        }
    }

    public static void main(String[] args) {

        board.initialize();
        board.print();
        // draw hand of 5 cards
        // play 1 card per turn (update scanner to use card name instead of x,y,value)
        // start of round draw up to 5 cards
        doRound();
    }
}

class Deck {
    private List<Card> deck = new ArrayList<>();

    void initialize() {
        deck.clear();
        // generate 10 of each card +1, +2, +3, -1, -2, -3, 0
        // TODO some projects require loading content from a file (recommended 1 card
        // per line using csv data for each row)
        for (int i = 0; i < 10; i++) {
            deck.add(new Card("+1", 1));
            deck.add(new Card("+2", 2));
            deck.add(new Card("+3", 3));
            deck.add(new Card("-1", -1));
            deck.add(new Card("-2", -2));
            deck.add(new Card("-3", -3));
            deck.add(new Card("0", 0));
        }
        Collections.shuffle(deck);
    }

    List<Card> drawCards(int count) {
        List<Card> drawnCards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            if (deck.isEmpty()) {
                System.out.println("Deck is empty, reinitializing...");
                initialize();
                // return drawCards(count);
            }
            // draw from end to prevent array shift
            int index = deck.size() - 1;
            drawnCards.add(deck.remove(index));
        }
        return drawnCards;
    }
}

class Card {
    private int value;
    private String name;

    Card(String name, int value) {
        this.value = value;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("Card(%d) = %d", name, value);
    }
}

class Board {
    private Cell[][] cells = new Cell[3][3];
    private Random random = new Random();

    void initialize() {
        cells = new Cell[3][3];
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                cells[i][j] = new Cell(i, j, random.nextInt(4));// 0-3
            }
        }
    }

    // count all rows and columns that equal 6
    int getPoints() {
        int points = 0;
        for (int i = 0; i < cells.length; i++) {
            int rowSum = 0;
            for (int j = 0; j < cells[i].length; j++) {
                rowSum += cells[i][j].value;
            }
            if (rowSum == 6) {
                points++;
            }
        }
        for (int j = 0; j < cells[0].length; j++) {
            int colSum = 0;
            for (int i = 0; i < cells.length; i++) {
                colSum += cells[i][j].value;
            }
            if (colSum == 6) {
                points++;
            }
        }
        return points;
    }

    void applyAction(int x, int y, int value) {
        if (x < 0 || x >= cells.length || y < 0 || y >= cells[0].length) {
            throw new IllegalArgumentException("Invalid cell coordinates");
        }
        cells[x][y].addValue(value);
    }

    void print() {
        // print as a formatted grid
        for (int i = 0; i < cells.length; i++) {
            for (int j = 0; j < cells[i].length; j++) {
                System.out.print(String.format("[%s]", cells[i][j].value));
            }
            System.out.println();
        }
    }
}

class Cell {
    int x, y;
    int value;

    Cell(int x, int y, int value) {
        this.x = x;
        this.y = y;
        this.value = value;
    }

    protected void addValue(int value) {
        // Ensure the value overflows to a range of 0-3
        this.value = (this.value + value) % 4;
        if (this.value < 0) {
            this.value += 4; // Handle negative values to keep within 0-3
        }
    }

    @Override
    public String toString() {
        return String.format("Cell(%d,%d) = %d", x, y, value);
    }
}