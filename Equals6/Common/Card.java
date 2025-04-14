package Equals6.Common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Card implements Cloneable, Serializable {
    private int value;
    private String name;
    private long id;
    private static long idCounter = 0;

    public Card(String name, int value) {
        this.value = value;
        this.name = name;
        idCounter++;
        this.id = idCounter;
    }

    public long getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return String.format("Card[%s](%s) = %d", getId(), name, value);
    }

    @Override
    public Card clone() {
        try {
            // Serialize the object to a byte array
            ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOut);
            out.writeObject(this);

            // Deserialize the byte array to create a new object
            ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
            ObjectInputStream in = new ObjectInputStream(byteIn);
            return (Card) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Cloning via serialization failed", e);
        }
    }
}
