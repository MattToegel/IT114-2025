package NDFF.Common.Payloads;

import java.util.ArrayList;
import java.util.List;

public class RoomResultPayload extends Payload {
    private List<String> rooms = new ArrayList<String>();

    public RoomResultPayload() {
        setPayloadType(PayloadType.ROOM_LIST);
    }

    public List<String> getRooms() {
        return rooms;
    }

    public void setRooms(List<String> rooms) {
        this.rooms = rooms;
    }

    @Override
    public String toString() {
        return super.toString() + "Rooms [" + String.join(",", rooms) + "]";
    }
}
