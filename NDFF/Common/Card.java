package NDFF.Common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

// immutable
public class Card implements Serializable {
    private final String id;
    private final CardType type;
    private final float value;
    private final String description;

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

    /**
     * Used to determine if using a card requires coordinates based on card type.
     * 
     * @return
     */
    public boolean requiresCoordinates() {
        return type == CardType.LONG_TERM_PROBABILITY || type == CardType.TEMPORARY_PROBABILITY;
    }

    public Card clone() {
        // serialize/deserialize to create a deep copy
        // Generally not recommended for performance-sensitive applications,
        // but useful for immutable objects like Card
        // This method assumes that Card and its fields are Serializable
        try {
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(this);
            out.flush();
            out.close();

            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            return (Card) in.readObject();
        } catch (Exception e) {
            throw new RuntimeException("Failed to clone Card", e);
        }
    }
}
