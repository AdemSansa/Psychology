module com.example.psy {
    requires javafx.fxml;
    requires java.sql;
    requires jbcrypt;
    requires java.desktop;
    requires com.calendarfx.view;
    requires mysql.connector.j;

    requires com.google.zxing.javase;
    requires javafx.web;
    requires com.google.gson;


    opens com.example.psy to javafx.fxml;
    exports application;
    opens application to javafx.fxml;
    exports Controllers.auth;
    opens Controllers.auth to javafx.fxml;
    exports Controllers.Welcome;
    opens Controllers.Welcome to javafx.fxml;

    // open controller and entities packages used by FXML
    exports Controllers.User;
    opens Controllers.User to javafx.fxml;
    exports Entities;
    opens Entities to javafx.fxml;
    exports Service;
    opens Service;
    opens Controllers.Therapists to javafx.fxml;
    opens Controllers.forum to javafx.fxml;
    opens Controllers.Question  to javafx.fxml;
    opens Controllers.Quiz to javafx.fxml;
    opens Controllers.Appointment to javafx.fxml;



    opens Controllers.QuizAssesment to javafx.fxml;
    opens Controllers.QuizResults to javafx.fxml;


    opens Controllers.Event to javafx.fxml;

    // optional but recommended
}