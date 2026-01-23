module com.example.psy {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;


    opens com.example.psy to javafx.fxml;
    exports com.example.psy.application;
    opens com.example.psy.application to javafx.fxml;
}