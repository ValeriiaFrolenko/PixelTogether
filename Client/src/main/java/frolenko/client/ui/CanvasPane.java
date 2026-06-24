package frolenko.client.ui;

import common.dto.draw.PixelUpdate;
import frolenko.client.core.RoomState;
import frolenko.client.service.DrawService;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CanvasPane extends Canvas {

    private static final double MIN_ZOOM = 1.0;
    private static final double MAX_ZOOM = 32.0;
    private static final long FLUSH_INTERVAL_MS = 30;

    private final RoomState roomState;
    private final DrawService drawService;
    private final boolean readOnly;

    private double zoom = 8.0;
    private double offsetX = 0;
    private double offsetY = 0;

    private double dragStartX;
    private double dragStartY;
    private double dragStartOffsetX;
    private double dragStartOffsetY;
    private boolean isDragging = false;
    private boolean hasPanned = false;

    private volatile boolean dirty = true;
    private boolean centered = false;

    private final List<PixelUpdate> pendingPixels = new ArrayList<>();
    private final ScheduledExecutorService flushScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "canvas-flush");
        t.setDaemon(true);
        return t;
    });

    private Color selectedColor = Color.BLACK;

    public CanvasPane(RoomState roomState, DrawService drawService, boolean readOnly) {
        this.roomState = roomState;
        this.drawService = drawService;
        this.readOnly = readOnly;

        roomState.setOnPixelChanged(() -> dirty = true);

        widthProperty().addListener((obs, o, n) -> {
            if (!centered && n.doubleValue() > 0 && getHeight() > 0) {
                centerView();
                centered = true;
            } else if (!hasPanned) {
                centerView();
            }
            dirty = true;
        });
        heightProperty().addListener((obs, o, n) -> {
            if (!centered && n.doubleValue() > 0 && getWidth() > 0) {
                centerView();
                centered = true;
            } else if (!hasPanned) {
                centerView();
            }
            dirty = true;
        });

        setOnMousePressed(this::onMousePressed);
        setOnMouseDragged(this::onMouseDragged);
        setOnMouseReleased(this::onMouseReleased);
        setOnScroll(this::onScroll);
        setOnMouseClicked(this::onMouseClicked);

        startAnimationTimer();
        if (!readOnly) {
            startFlushScheduler();
        }
    }

    public void setSelectedColor(Color color) {
        this.selectedColor = color;
    }

    private void centerView() {
        offsetX = (getWidth() - roomState.getWidth() * zoom) / 2.0;
        offsetY = (getHeight() - roomState.getHeight() * zoom) / 2.0;
    }

    private void startAnimationTimer() {
        new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (dirty) {
                    render();
                    dirty = false;
                }
            }
        }.start();
    }

    private void startFlushScheduler() {
        flushScheduler.scheduleAtFixedRate(() -> {
            List<PixelUpdate> toSend;
            synchronized (pendingPixels) {
                if (pendingPixels.isEmpty()) return;
                toSend = new ArrayList<>(pendingPixels);
                pendingPixels.clear();
            }
            drawService.draw(roomState.getRoomId(), toSend, err ->
                    System.err.println("Draw error: " + err));
        }, FLUSH_INTERVAL_MS, FLUSH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private void render() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();

        gc.setFill(Color.web("#e0e0e0"));
        gc.fillRect(0, 0, w, h);

        int canvasW = roomState.getWidth();
        int canvasH = roomState.getHeight();

        for (int y = 0; y < canvasH; y++) {
            for (int x = 0; x < canvasW; x++) {
                int argb = roomState.getPixel(x, y);
                if ((argb & 0xFF000000) != 0) {
                    Color color = Color.rgb(
                            (argb >> 16) & 0xFF,
                            (argb >> 8) & 0xFF,
                            argb & 0xFF
                    );
                    gc.setFill(color);
                    gc.fillRect(offsetX + x * zoom, offsetY + y * zoom, zoom, zoom);
                } else {
                    gc.setFill(Color.WHITE);
                    gc.fillRect(offsetX + x * zoom, offsetY + y * zoom, zoom, zoom);
                }
            }
        }

        if (zoom >= 4) {
            gc.setStroke(Color.web("#cccccc"));
            gc.setLineWidth(0.5);
            for (int x = 0; x <= canvasW; x++) {
                gc.strokeLine(offsetX + x * zoom, offsetY, offsetX + x * zoom, offsetY + canvasH * zoom);
            }
            for (int y = 0; y <= canvasH; y++) {
                gc.strokeLine(offsetX, offsetY + y * zoom, offsetX + canvasW * zoom, offsetY + y * zoom);
            }
        }
    }

    private void onMousePressed(MouseEvent e) {
        if (e.getButton() == MouseButton.SECONDARY ||
                e.getButton() == MouseButton.MIDDLE ||
                (e.getButton() == MouseButton.PRIMARY && e.isAltDown())) {
            isDragging = true;
            dragStartX = e.getX();
            dragStartY = e.getY();
            dragStartOffsetX = offsetX;
            dragStartOffsetY = offsetY;
        } else if (e.getButton() == MouseButton.PRIMARY && !readOnly) {
            paintPixel(e.getX(), e.getY());
        }
    }

    private void onMouseDragged(MouseEvent e) {
        if (isDragging) {
            offsetX = dragStartOffsetX + (e.getX() - dragStartX);
            offsetY = dragStartOffsetY + (e.getY() - dragStartY);
            hasPanned = true;
            dirty = true;
        } else if (e.getButton() == MouseButton.PRIMARY && !readOnly) {
            paintPixel(e.getX(), e.getY());
        }
    }

    private void onMouseReleased(MouseEvent e) {
        isDragging = false;
    }

    private void onMouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
            centerView();
            hasPanned = false;
            dirty = true;
        }
    }

    private void onScroll(ScrollEvent e) {
        double oldZoom = zoom;
        double delta = e.getDeltaY() > 0 ? 1.1 : 1.0 / 1.1;
        zoom = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, zoom * delta));
        offsetX = e.getX() - (e.getX() - offsetX) * (zoom / oldZoom);
        offsetY = e.getY() - (e.getY() - offsetY) * (zoom / oldZoom);
        dirty = true;
    }

    private void paintPixel(double screenX, double screenY) {
        int px = (int) ((screenX - offsetX) / zoom);
        int py = (int) ((screenY - offsetY) / zoom);

        if (px < 0 || px >= roomState.getWidth() || py < 0 || py >= roomState.getHeight()) return;

        int argb = colorToArgb(selectedColor);
        roomState.setPixel(px, py, argb);

        synchronized (pendingPixels) {
            pendingPixels.removeIf(p -> p.x() == px && p.y() == py);
            pendingPixels.add(new PixelUpdate(px, py, argb));
        }
    }

    private int colorToArgb(Color color) {
        int r = (int) (color.getRed() * 255);
        int g = (int) (color.getGreen() * 255);
        int b = (int) (color.getBlue() * 255);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    public void shutdown() {
        flushScheduler.shutdown();
    }
}