package Project.Common;

public class PointsPayload extends Payload {
    private int points;

    public PointsPayload() {
        setPayloadType(PayloadType.POINTS);
    }

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
        return super.toString() + String.format(" points=%d", points);
    }
}
