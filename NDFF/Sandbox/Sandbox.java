package NDFF.Sandbox;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sandbox {
    public static void main(String[] args) {
        // Create a grid with 5 rows and 5 columns
        Grid grid = new Grid(5, 5);

        // Print the initial state of the grid
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                System.out.println(grid.getCell(i, j));
            }
        }
        Deck deck = new Deck();
        List<Card> drawnCards = new ArrayList<>();
        // Draw 5 cards from the deck
        for (int i = 0; i < 5; i++) {
            Card card = deck.drawCard();
            if (card != null) {
                drawnCards.add(card);
                System.out.println("Drawn card: " + card);
            } else {
                System.out.println("No more cards in the deck.");
                break;
            }
        }
        // use a random card
        // use a switch case to handle different card types
        Card testCard = drawnCards.get((int) (Math.random() * drawnCards.size()));
        System.out.println("Using card: " + testCard);
        float CATCH_MULTIPLIER = 1.0f; // Default multiplier
        Cell randomCell = grid.getCell((int) (Math.random() * 5), (int) (Math.random() * 5));
        int fishingAttempts = 1;
        switch (testCard.getType()) {
            case CATCH_MULTIPLIER:
                // Apply catch multiplier logic
                CATCH_MULTIPLIER = testCard.getValue();
                System.out.println("Applying catch multiplier: " + CATCH_MULTIPLIER);
                break;
            case LONG_TERM_PROBABILITY:
                // Adjust long-term probability for all cells
                System.out.println("Adjusting long-term probability by: " + testCard.getValue());
                randomCell.changeLongTermProbability(testCard.getValue());
                break;
            case TEMPORARY_PROBABILITY:
                // Adjust temporary probability for a specific cell
                System.out.println("Adjusting temporary probability by: " + testCard.getValue());
                randomCell.changeTempProbability(testCard.getValue());
                break;
            case FISHING_ATTEMPTS:
                // Adjust fishing attempts logic
                fishingAttempts += (int) testCard.getValue();
                System.out.println("Adjusting fishing attempts: " + fishingAttempts);
                break;
            default:
                System.out.println("Unknown card type.");
        }

        // Try to catch fish from a specific cell
        Cell cell = grid.getCell(2, 2);
        for (int i = 0; i < fishingAttempts; i++) {
            FishQuantity caughtFish = cell.tryCatchFish();
            if (caughtFish != null) {
                System.out.println("Caught fish: " + caughtFish);
                if (CATCH_MULTIPLIER > 1.0f) {
                    int adjustedQuantity = (int) (caughtFish.getQuantity() * CATCH_MULTIPLIER);
                    System.out.println("Adjusted quantity with multiplier: " + adjustedQuantity);
                }
            } else {
                System.out.println("No fish caught.");
            }
        }

    }
}

class Deck {
    private List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
        // load cards from a csv file from the Assets package of this project

        try (java.io.InputStream is = getClass().getResourceAsStream("../Assets/cards.csv");
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(is))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.startsWith("#"))
                    continue; // skip empty or comment lines
                String[] parts = line.split(",");

                String id = parts[0].trim();
                CardType type = CardType.valueOf(parts[1].trim());
                float value = Float.parseFloat(parts[2].trim());
                String description = parts[3].trim();
                int quantity = parts.length > 4 ? Integer.parseInt(parts[4].trim()) : 1; // optional quantity
                for (int i = 0; i < quantity; i++) {
                    // create multiple cards if quantity is specified
                    cards.add(new Card(id, type, value, description));
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load cards: " + e.getMessage());
        }
        Collections.shuffle(cards); // shuffle the deck after loading
    }

    public Card drawCard() {
        if (cards.isEmpty()) {
            return null;
        }
        return cards.remove((int) (Math.random() * cards.size()));
    }

    public int getTotalCards() {
        return cards.size();
    }

    public List<Card> getCards() {
        return cards;
    }

    public void clear() {
        cards.clear();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    @Override
    public String toString() {
        return "Deck{" +
                "cards=" + cards +
                '}';
    }

}

class Card {
    private String id;
    private CardType type;
    private float value;
    private String description;

    public Card(String id, CardType type, float value, String description) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public CardType getType() {
        return type;
    }

    public float getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return String.format("Card{id='%s', type=%s, value=%.2f, description='%s'}",
                id, type, value, description);
    }
}

enum CardType {
    // set to modify caught quantity (single user)
    // set to adjust Cell's longTermProbability (all users)
    // set to temporarily modify Cell's tempProb (current user)
    CATCH_MULTIPLIER,
    LONG_TERM_PROBABILITY,
    TEMPORARY_PROBABILITY,
    FISHING_ATTEMPTS
}

class Grid {
    private Cell[][] cells;

    public Grid(int rows, int cols) {
        cells = new Cell[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }

    public Cell getCell(int row, int col) {
        return cells[row][col];
    }
}

class Cell {
    private float baseProbability;
    private float longTermProbability;
    private float tempProbability;
    private List<Spawn> fish;
    private int row;
    private int col;

    public Cell(int row, int col) {
        this.row = row;
        this.col = col; // in case we need to reference the cell's position
        this.baseProbability = 0.3f + (float) Math.random() * (0.65f - 0.3f);

        int fishspawn = 1 + (int) (Math.random() * 5);
        this.fish = new ArrayList<>();
        for (int i = 0; i < fishspawn; i++) {
            this.fish.add(new Spawn());
        }

        System.out.println("Created: " + this.toString());
    }

    public void changeLongTermProbability(float change) {
        // change the long term probability by a value
        // this is a long-term adjustment, so it should be persistent
        this.longTermProbability += change;
    }

    public void changeTempProbability(float change) {
        // change the temporary probability by a value
        // this is a temporary adjustment, so it should be reset after use
        this.tempProbability += change;
    }

    public void resetTempProbability() {
        this.tempProbability = 0;
    }

    private float getCalcedProbability() {
        // combine baseProbability, longTermProbability, tempProbability
        float prob = baseProbability + longTermProbability + tempProbability;
        // clamp the probability between 0 and 1
        prob = Math.max(0f, Math.min(1f, prob));
        return prob;
    }

    public int getTotalFish() {
        return fish.stream().mapToInt(Spawn::getQuantity).sum();
    }

    public FishQuantity tryCatchFish() {
        if (fish.isEmpty()) {
            return null; // No fish available
        }
        // see if random value meets current probability
        float randomValue = (float) Math.random();
        float currentProbability = getCalcedProbability();
        System.out.println(String.format("Rand: %.2f vs Current: %.2f", randomValue, currentProbability));

        if (randomValue > currentProbability) {
            return null; // No fish caught
        }
        Spawn spawn = fish.get((int) (Math.random() * fish.size()));
        if (spawn.hasFish()) {
            // catch a random quantity of fish based on Spawn quantity as max
            int catchQuantity = 1 + (int) (Math.random() * spawn.getQuantity());
            FishQuantity caughtFish = spawn.catchFish(catchQuantity);
            if (spawn.getQuantity() <= 0) {
                fish.remove(spawn); // Remove spawn if no fish left
            }
            return caughtFish;

        } else {
            fish.remove(spawn); // Remove empty spawn
            return tryCatchFish(); // Try next spawn
        }
    }

    @Override
    public String toString() {
        return String.format(
                "Cell[%d][%d]: baseProb=%.2f, currentProb=%.2f, spawns=%d, totalFish=%d",
                row,
                col,
                baseProbability,
                longTermProbability,
                fish.size(),
                getTotalFish());
    }
}

class Spawn {
    private FishQuantity fish;

    public Spawn() {
        FishType fishType = FishType.values()[(int) (Math.random() * FishType.values().length)];
        int quantity = 5 + (int) (Math.random() * 11);
        this.fish = new FishQuantity(fishType, quantity);
    }

    public Spawn(FishType fishType, int quantity) {
        this.fish = new FishQuantity(fishType, quantity);
    }

    public int getQuantity() {
        return fish.getQuantity();
    }

    public FishQuantity catchFish() {
        return catchFish(1);
    }

    public FishQuantity catchFish(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (fish.getQuantity() < quantity) {
            throw new IllegalArgumentException("Not enough fish available to catch");
        }
        fish.changeQuantity(-quantity);
        // create a data container for the caught fish (type, quantity caught)
        // Note: this is a new instance, not the original fish instance
        return new FishQuantity(fish.getFishType(), quantity);
    }

    public boolean hasFish() {
        return fish.getQuantity() > 0;
    }

    public FishQuantity getFishQuantity() {
        return fish;
    }
}

class FishQuantity {
    private FishType fishType;
    private int quantity;

    public FishQuantity(FishType fishType, int quantity) {
        this.fishType = fishType;
        this.quantity = quantity;
    }

    public FishType getFishType() {
        return fishType;
    }

    public int getQuantity() {
        return quantity;
    }

    public void changeQuantity(int quantity) {
        if (this.quantity + quantity < 0) {
            throw new IllegalArgumentException("Resulting quantity cannot be negative");
        }
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return String.format("%s x %d", fishType.name(), quantity);
    }
}

enum FishType {
    COMMON(1),
    UNCOMMON(5),
    RARE(10),
    SUPER_RARE(25),
    LEGENDARY(100);

    private final int pointValue;

    FishType(int pointValue) {
        this.pointValue = pointValue;
    }

    public int getPointValue() {
        return pointValue;
    }
}