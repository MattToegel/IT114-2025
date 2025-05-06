package Memory.Client.Interfaces;

public interface IBoardEvents extends IGameEvents {

    void onReceiveDimensions(int rows, int columns);

    void onReceiveCell(int row, int column, String value, boolean isFlipped, boolean isCollected);

    void onReceiveSelection(int row, int column, boolean isSelected);
}
