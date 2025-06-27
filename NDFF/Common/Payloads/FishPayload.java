package NDFF.Common.Payloads;

import NDFF.Common.CatchData;

public class FishPayload extends CoordPayload {
    private CatchData fishQuantity;

    public FishPayload(int x, int y, CatchData fishQuantity) {
        super(x, y);
        // prevents pass by reference issues
        this.fishQuantity = new CatchData(fishQuantity.getFishType(), fishQuantity.getQuantity());
        setPayloadType(PayloadType.FISH);
    }

    public CatchData getFishQuantity() {
        // may need to handle potential pass by reference issues
        return fishQuantity;
    }

    @Override
    public String toString() {
        return super.toString() + " {" +
                "fishQuantity=" + fishQuantity +
                '}';
    }

}
