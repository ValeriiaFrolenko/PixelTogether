module server {
    requires common;
    requires com.google.guice;
    requires com.google.guice.extensions.assistedinject;
    requires com.google.common;
    requires java.sql;
    requires com.h2database;
    requires bcrypt;

    opens server.core to com.google.guice;
    opens server.database to com.google.guice;
    opens server.database.dao to com.google.guice;
    opens server.database.model to com.google.guice;
    opens server.handler to com.google.guice;
    opens server.network to com.google.guice;

    exports server.core;
    exports server.database;
    exports server.database.dao;
    exports server.database.model;
    exports server.handler;
    exports server.network;
    exports server.handler.user;
    opens server.handler.user to com.google.guice;
    exports server.handler.room;
    opens server.handler.room to com.google.guice;
}