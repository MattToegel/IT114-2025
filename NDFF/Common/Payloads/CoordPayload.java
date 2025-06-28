package NDFF.Common.Payloads;

public class CoordPayload extends Payload {
    private int x;
    private int y;

    public CoordPayload(int x, int y) {
        this.x = x;
        this.y = y;
        setPayloadType(PayloadType.USE);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public String toString() {
        return super.toString() + " {" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}
