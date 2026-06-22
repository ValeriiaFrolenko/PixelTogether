module frolenko.client {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires common;
    requires com.google.guice;
    requires com.google.guice.extensions.assistedinject;
    requires java.logging;

    opens frolenko.client to javafx.fxml, com.google.guice;
    opens frolenko.client.controller to javafx.fxml, com.google.guice;
    opens frolenko.client.core to com.google.guice;
    opens frolenko.client.network to com.google.guice;
    opens frolenko.client.controller.tab to com.google.guice, javafx.fxml;
    opens frolenko.client.controller.dialog to com.google.guice, javafx.fxml;
    opens frolenko.client.handler to com.google.guice, javafx.fxml;
    opens frolenko.client.service to com.google.guice, javafx.fxml;

    exports frolenko.client;
    exports frolenko.client.controller;
    exports frolenko.client.core;
    exports frolenko.client.network;
    exports frolenko.client.controller.tab;
    exports frolenko.client.controller.dialog;
    exports frolenko.client.handler;
    exports frolenko.client.service;
    exports frolenko.client.ui;
    opens frolenko.client.ui to com.google.guice, javafx.fxml;

}