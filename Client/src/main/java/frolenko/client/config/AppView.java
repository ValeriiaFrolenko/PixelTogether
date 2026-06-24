package frolenko.client.config;

public enum AppView {

    MAIN("view/main-view.fxml"),
    ROOM("view/room-view.fxml"),
    WORK_VIEW("view/work-view.fxml"),

    DIALOG_CREATE_ROOM("view/dialog/create-room-dialog.fxml"),
    DIALOG_JOIN_PRIVATE("view/dialog/join-private-dialog.fxml"),
    DIALOG_SAVE_WORK("view/dialog/save-work-dialog.fxml");

    private final String fxmlPath;

    AppView(String fxmlPath) {
        this.fxmlPath = fxmlPath;
    }

    public String getFxmlPath() {
        return fxmlPath;
    }
}