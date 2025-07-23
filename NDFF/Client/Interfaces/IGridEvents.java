package NDFF.Client.Interfaces;

public interface IGridEvents extends IGameEvents {
    void onReceiveGridSize(int rows, int cols);

    void onReceiveCellUpdate(int row, int col, int value);
}
