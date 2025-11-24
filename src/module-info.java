module Agents_gui_tp2 {
	requires javafx.controls;
	requires javafx.fxml;
	requires jade;
	requires javafx.swing;
	requires javafx.graphics;

    opens app to javafx.graphics, javafx.fxml;
    
    exports app;
    /*
    app.MyAgent
    */
}
