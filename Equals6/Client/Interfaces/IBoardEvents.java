package Equals6.Client.Interfaces;

public interface IBoardEvents extends IGameEvents {

    void onReceiveDimensions(int rows, int columns);

    void onReceiveCell(int row, int column, int value);
}