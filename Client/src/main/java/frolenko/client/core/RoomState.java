package frolenko.client.core;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class RoomState {

    private final int roomId;
    private final int width;
    private final int height;
    private final int[] pixels;
    private final boolean isOwner;
    private final String code;
    private final ObservableList<String> nicknames = FXCollections.observableArrayList();

    private Runnable onPixelChanged;

    public RoomState(int roomId, int width, int height, int[] pixels, boolean isOwner, String code) {
        this.roomId = roomId;
        this.width = width;
        this.height = height;
        this.pixels = pixels;
        this.isOwner = isOwner;
        this.code = code;
    }

    public int getRoomId() { return roomId; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int[] getPixels() { return pixels; }
    public boolean isOwner() { return isOwner; }
    public String getCode() { return code; }
    public ObservableList<String> getNicknames() { return nicknames; }

    public void setOnPixelChanged(Runnable onPixelChanged) {
        this.onPixelChanged = onPixelChanged;
    }

    public void setPixel(int x, int y, int color) {
        pixels[y * width + x] = color;
        if (onPixelChanged != null) {
            onPixelChanged.run();
        }
    }

    public int getPixel(int x, int y) {
        return pixels[y * width + x];
    }
}