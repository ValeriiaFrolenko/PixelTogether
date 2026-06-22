package frolenko.client.controller.tab;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import frolenko.client.core.AppState;
import frolenko.client.service.AuthService;
import frolenko.client.util.AlertHelper;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

@Singleton
public class AccountTabController {

    private final AuthService authService;
    private final AppState appState;

    @FXML private VBox guestPane;
    @FXML private VBox loggedInPane;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label authErrorLabel;
    @FXML private Label usernameLabel;

    @Inject
    public AccountTabController(AuthService authService, AppState appState) {
        this.authService = authService;
        this.appState = appState;
    }

    @FXML
    public void initialize() {
        appState.tokenProperty().addListener((obs, old, token) -> Platform.runLater(() -> {
            boolean loggedIn = token != null;
            guestPane.setVisible(!loggedIn);
            guestPane.setManaged(!loggedIn);
            loggedInPane.setVisible(loggedIn);
            loggedInPane.setManaged(loggedIn);
        }));
    }

    @FXML
    public void onLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            authErrorLabel.setText("Enter username and password.");
            return;
        }
        authService.login(username, password,
                token -> Platform.runLater(() -> {
                    appState.setToken(token);
                    usernameLabel.setText(username);
                    authErrorLabel.setText("");
                    passwordField.clear();
                }),
                error -> Platform.runLater(() -> authErrorLabel.setText(error))
        );
    }

    @FXML
    public void onRegister() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        if (username.isEmpty() || password.isEmpty()) {
            authErrorLabel.setText("Enter username and password.");
            return;
        }
        authService.register(username, password,
                token -> Platform.runLater(() -> {
                    appState.setToken(token);
                    usernameLabel.setText(username);
                    authErrorLabel.setText("");
                    passwordField.clear();
                }),
                error -> Platform.runLater(() -> authErrorLabel.setText(error))
        );
    }

    @FXML
    public void onLogout() {
        authService.logout(appState.getToken(),
                () -> Platform.runLater(() -> {
                    appState.setToken(null);
                    usernameLabel.setText("");
                }),
                error -> Platform.runLater(() -> AlertHelper.showError(error))
        );
    }
}