package frolenko.client.core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RoomState {

    private final int roomId;
    private final int width;
    private final int height;
    private final int[] pixels;
    private final ObservableList<String> nicknames = FXCollections.observableArrayList();

    public RoomState(int roomId, int width, int height, int[] pixels) {
        this.roomId = roomId;
        this.width = width;
        this.height = height;
        this.pixels = pixels;
    }

    public int getRoomId() { return roomId; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int[] getPixels() { return pixels; }
    public ObservableList<String> getNicknames() { return nicknames; }

    public void setPixel(int x, int y, int color) {
        pixels[y * width + x] = color;
    }

    public int getPixel(int x, int y) {
        return pixels[y * width + x];
    }
}