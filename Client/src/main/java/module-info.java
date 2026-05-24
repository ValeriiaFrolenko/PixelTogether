module frolenko.client {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;

    opens frolenko.client to javafx.fxml;
    exports frolenko.client;
    exports frolenko.client.controler;
    opens frolenko.client.controler to javafx.fxml;
}