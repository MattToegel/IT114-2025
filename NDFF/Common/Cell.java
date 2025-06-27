package NDFF.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Cell {
    private float baseProbability;
    private float longTermProbability;
    private float tempProbability;
    private List<CatchData> fish = new ArrayList<>();
    private int row;
    private int col;
    private static Random random = new Random();

    public Cell(int row, int col) {
        this.row = row;
        this.col = col; // in case we need to reference the cell's position
    }

    /**
     * Initializes the cell with a random base probability and a random number of
     * fish spawns.
     * Used Server-side only
     */
    public void initialize() {
        this.baseProbability = 0.3f + (float) Math.random() * (0.65f - 0.3f);

        int fishSpawn = 1 + (int) (Math.random() * 5);
        fish.clear(); // clear any existing fish before initializing
        for (int i = 0; i < fishSpawn; i++) {
            // random fish type and quantity
            FishType fishType = FishType.values()[Cell.random.nextInt(FishType.values().length)];
            int quantity = 5 + Cell.random.nextInt(11); // Random quantity between 5 and 15
            this.fish.add(new CatchData(fishType, quantity));
        }
        System.out.println("Initialized: " + this.toString());
    }

    public void clearFish() {
        // clear the fish list
        this.fish.clear();
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
        // return fish.stream().mapToInt(Spawn::getQuantity).sum();
        return fish.stream().mapToInt(CatchData::getQuantity).sum();
    }

    public void setFishCount(FishType fishType, int quantity) {
        // set the quantity of fish for a specific FishType
        for (CatchData data : fish) {
            if (data.getFishType() == fishType) {
                data.changeQuantity(quantity);
                return; // Exit after updating the first match
            }
        }
        // If no existing fish type found, add a new one
        fish.add(new CatchData(fishType, quantity));
    }

    /**
     * Sets the quantity of fish for a specific FishType.
     * If the FishType already exists, it updates the quantity.
     * If it does not exist, it adds a new CatchData instance.
     * (Client-side only)
     * 
     * @param fishType
     * @param quantity
     */
    public void changeFishCount(FishType fishType, int change) {
        // change the quantity of fish for a specific FishType
        for (CatchData data : fish) {
            if (data.getFishType() == fishType) {
                data.changeQuantity(change);
                return; // Exit after updating the first match
            }
        }
        // If no existing fish type found, add a new one with the change as quantity
        fish.add(new CatchData(fishType, change));
    }

    public CatchData tryCatchFish() {
        // these two checks shouldn't happen, but are here for safety
        if (fish.isEmpty()) {
            LoggerUtil.INSTANCE.warning(String.format("Cell[%d][%d] has no fish to catch. (empty list)", row, col));
            return null; // No fish available
        }
        if (getTotalFish() <= 0) {
            LoggerUtil.INSTANCE.warning(String.format("Cell[%d][%d] has no fish to catch. (no quantity)", row, col));
            return null; // No fish available to catch
        }

        // see if random value meets current probability
        float randomValue = (float) Math.random();
        float currentProbability = getCalcedProbability();
        System.out.println(String.format("Rand: %.2f vs Current: %.2f", randomValue, currentProbability));

        if (randomValue > currentProbability) {
            LoggerUtil.INSTANCE.info("Failed to catch fish in Cell[" + row + "][" + col + "] (probability check)");
            return null; // No fish caught
        }

        // randomly select a CatchData from the fish list
        CatchData potential = fish.get(Cell.random.nextInt(fish.size()));
        LoggerUtil.INSTANCE.info(String.format("Potential catch: %s in Cell[%d][%d]", potential, row, col));
        int catchQuantity = 1 + Cell.random.nextInt(potential.getQuantity()); // Randomly catch 1 to max quantity
        if (potential.getQuantity() - catchQuantity <= 0) {
            fish.remove(potential); // Remove if no fish left
            LoggerUtil.INSTANCE.info("Removing empty fish data from Cell[" + row + "][" + col + "]");
        } else {
            potential.changeQuantity(-catchQuantity); // Reduce quantity of caught fish
        }
        LoggerUtil.INSTANCE.info(String.format("Caught %s in Cell[%d][%d]", potential,
                row, col));
        return new CatchData(potential.getFishType(), catchQuantity);
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
