package Memory.Common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CoordsPayload extends Payload {
    private List<Coord> coords = new ArrayList<>();

    public void setCoord(Coord coord) {
        this.coords.add(coord);
    }

    public void setCoords(List<Coord> coords) {
        if (coords == null) {
            this.coords.clear();
            return;
        }
        this.coords = coords.stream().filter(c -> c != null).collect(Collectors.toList());
    }

    public List<Coord> getCoords() {
        return coords;
    }

    @Override
    public String toString() {
        return super.toString() + String.format("CoordsPayload(%s)",
                String.join("\n", coords.stream().map(c -> c.toString()).toArray(String[]::new)));
    }
}
