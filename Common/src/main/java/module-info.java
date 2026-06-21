module common {
    requires com.google.guice;
    requires com.fasterxml.jackson.databind;

    exports common.dto;
    exports common.model;
    exports common.network;
    exports common.protocol;
    exports common.utils;
    exports common.dto.user;
    exports common.dto.room;
    exports common.dto.draw;
    exports common.dto.work;
}