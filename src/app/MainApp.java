package app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class MainApp extends Application {

	public static HashMap<String, AgentContainer> containers = new HashMap<String, AgentContainer>(); // Name_Con, Container
	public static ArrayList<AgentController> controllers = new ArrayList<AgentController>(); // List of Controllers
	public static HashMap<String, String> agentsNames = new HashMap<String, String>(); // Name, Color

    public static void updateAgentsBrodCast(String fromAgent, String message) {
    	Platform.runLater(() -> {
    		String color = agentsNames.get(fromAgent);
    		for (AgentController controller : controllers) {
				if (!controller.agent.getName().equals(fromAgent)) {
					controller.appendColoredLog(fromAgent, message, color);
				}
    		}
    	});
    }
    
	public static void updateAgentsSentMsg(String fromAgent, String to, String message) {
		Platform.runLater(() -> {
            System.out.println("D2 " + fromAgent + " sending message to " + to + ": " + message);

    		String color = agentsNames.get(fromAgent);
    		for (AgentController controller : controllers) {
    			String agentName = controller.agent.getLocalName();
    			System.out.println("D3 Checking agent " + agentName);
				if (agentName.equals(fromAgent)) {
					controller.appendColoredLog(fromAgent, message, color);
					System.out.println("D4 Message sent from " + fromAgent);
				}
				if (agentName.equals(to)) {
					controller.appendColoredLog(fromAgent, message, color);
					System.out.println("D5 Message received at " + to);
				}
    		}
    	});
	}
	
    public static List<String> getAvailableAgents(MyAgent agent) {
        List<String> agents = new ArrayList<>(MainApp.agentsNames.keySet());
        if (agent != null) {
            agents.remove(agent.getLocalName());
        }
        return agents;
    }
	
	@Override
    public void start(Stage primaryStage) {
		// Prevent JavaFX from exiting when last window is closed
        Platform.setImplicitExit(false);
		
        // Initialize JADE only after JavaFX toolkit is ready
        initJade();
    }

    private static void initJade() {
    	try {
            // JADE runtime setup
            Runtime rt = Runtime.instance();
            Profile mainProfile = new ProfileImpl();
            mainProfile.setParameter(Profile.MAIN, "true");
            mainProfile.setParameter(Profile.LOCAL_PORT, "1200");
            
            AgentContainer mainContainer = rt.createMainContainer(mainProfile);

            // Add the main container to the containers map
            containers.put("Main-Container", mainContainer);
         
            System.out.println("JADE Main Container started.");
            mainContainer.createNewAgent("Agent1", "app.MyAgent", null).start();
            mainContainer.createNewAgent("Agent2", "app.MyAgent", null).start();
            
            // Start the official JADE RMA GUI
            
            Profile secondaryProfile = new ProfileImpl();
            secondaryProfile.setParameter(Profile.MAIN, "false");
            secondaryProfile.setParameter(Profile.LOCAL_PORT, "1201");
            secondaryProfile.setParameter(Profile.CONTAINER_NAME, "Secondary-Container");
            
            AgentContainer SecondaryContainer = rt.createAgentContainer(secondaryProfile);
            
            // Add the main container to the containers map
            containers.put("Secondary-Container", SecondaryContainer);
            System.out.println("JADE Secondary Container started.");
            SecondaryContainer.createNewAgent("Agent3", "app.MyAgent", null).start();
            SecondaryContainer.createNewAgent("Agent4", "app.MyAgent", null).start();

            mainContainer.createNewAgent("rma", "jade.tools.rma.rma", null).start();
            System.out.println("RMA started.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
