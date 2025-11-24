package app;

import java.util.ArrayList;
import java.util.HashMap;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.stage.Stage;

public class MainApp extends Application {

	public static HashMap<String, AgentContainer> containers = new HashMap<String, AgentContainer>();
	public static ArrayList<String> agentsName = new ArrayList<String>();

	@Override
    public void start(Stage primaryStage) {
		// Prevent JavaFX from exiting when last window is closed
        Platform.setImplicitExit(false);

        // Keep a hidden primary stage so the toolkit remains running.
        // We don't show it to the user.
        primaryStage.setTitle("Hidden FX Anchor");
        primaryStage.setWidth(0);
        primaryStage.setHeight(0);
		
        // Initialize JADE only after JavaFX toolkit is ready
        initJade();
    }

    private static void initJade() {
    	try {
            // JADE runtime setup
            Runtime rt = Runtime.instance();
            Profile p = new ProfileImpl();
            AgentContainer mainContainer = rt.createMainContainer(p);

            // Add the main container to the containers map
            containers.put("Main-Container", mainContainer);
            
            System.out.println("JADE Main Container started.");

            // Start the official JADE RMA GUI
            mainContainer.createNewAgent("rma", "jade.tools.rma.rma", null).start();
            System.out.println("RMA started.");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AgentContainer getMainContainer() {
        return containers.get("Main-Container");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
