package frolenko.client.util;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

public class Debouncer {

    private final PauseTransition pauseTransition;

    public Debouncer(long millis) {
        this.pauseTransition = new PauseTransition(Duration.millis(millis));
    }

    public void debounce(Runnable action) {
        pauseTransition.setOnFinished(event -> action.run());
        pauseTransition.playFromStart();
    }
}