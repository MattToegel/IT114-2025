package Memory.Common;

public class PointsPayload extends Payload {
    private int points;

    /**
     * @return the points
     */
    public int getPoints() {
        return points;
    }

    /**
     * @param points the points to set
     */
    public void setPoints(int points) {
        this.points = points;
    }

    @Override
    public String toString() {
        return super.toString() + String.format("Points (%d)", points);
    }

}
