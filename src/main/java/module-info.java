module com.myapp {
    requires transitive javafx.controls;
    requires transitive javafx.fxml;

    opens com.myapp to javafx.fxml;
    exports com.myapp;
}
