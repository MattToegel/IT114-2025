package Equals6.Common;

public class BoardPayload extends Payload {
    private int rows, cols;
    private long seed;

    public BoardPayload(int rows, int cols, long seed) {
        this.rows = rows;
        this.cols = cols;
        this.seed = seed;
        setPayloadType(PayloadType.BOARD_DATA);
    }

    /**
     * @return the rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * @param rows the rows to set
     */
    public void setRows(int rows) {
        this.rows = rows;
    }

    /**
     * @return the cols
     */
    public int getCols() {
        return cols;
    }

    /**
     * @param cols the cols to set
     */
    public void setCols(int cols) {
        this.cols = cols;
    }

    /**
     * @return the seed
     */
    public long getSeed() {
        return seed;
    }

    /**
     * @param seed the seed to set
     */
    public void setSeed(long seed) {
        this.seed = seed;
    }

    @Override
    public String toString() {
        return super.toString() + String.format(" rows=[%d], cols=[%d], seed=[%d]", rows, cols, seed);
    }
}
