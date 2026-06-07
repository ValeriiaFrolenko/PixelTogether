module frolenko.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires common;
    requires com.google.guice;
    requires com.google.guice.extensions.assistedinject;

    opens frolenko.client to javafx.fxml, com.google.guice;
    opens frolenko.client.controler to javafx.fxml, com.google.guice;
    opens frolenko.client.core to com.google.guice;
    opens frolenko.client.network to com.google.guice;

    exports frolenko.client;
    exports frolenko.client.controler;
    exports frolenko.client.core;
    exports frolenko.client.network;
}