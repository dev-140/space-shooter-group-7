module application {
	requires javafx.controls;
    requires javafx.fxml;
	requires transitive javafx.graphics;
	opens application to javafx.fxml;

    //if necessary, please activate:
exports application;
    //requires transitive javafx.graphics;
}