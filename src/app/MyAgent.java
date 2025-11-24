package app;

import jade.core.Agent;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MyAgent extends Agent {
	private transient Stage guiStage;
    private transient AgentController controller;

    @Override
    protected void setup() {
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agent.fxml"));
                Scene scene = new Scene(loader.load());
                controller = loader.getController();
                controller.setAgent(this);

                guiStage = new Stage();
                guiStage.setTitle(getLocalName());
                guiStage.setScene(scene);
                guiStage.show();

                log("Started.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void takeDown() {
        Platform.runLater(() -> {
            if (guiStage != null) guiStage.close();
        });
        System.out.println(getLocalName() + " terminated.");
    }
    
    /** Utility to add messages to GUI log */
    public void log(String msg) {
        Platform.runLater(() -> {
        	if (controller != null)
                controller.appendLog("[Agent " + getLocalName() + "] " + msg);
            else
                System.out.println("[Agent " + getLocalName() + "] " + msg);
        });
    }
	  
    @Override
    protected void beforeMove() {
        log("Preparing to migrate...");
        
        // Close old GUI before migration (runs on JavaFX thread)
        Platform.runLater(() -> {
            if (guiStage != null) {
                guiStage.close();
                guiStage = null;
            }
            controller = null; // Reset reference
        });
    }

    @Override
    protected void afterMove() {
        // Recreate GUI after migration (runs on JavaFX thread)
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agent.fxml"));
                Scene scene = new Scene(loader.load());
                controller = loader.getController();
                controller.setAgent(this); // Link to the new agent instance

                guiStage = new Stage();
                guiStage.setTitle(getLocalName());
                guiStage.setScene(scene);
                guiStage.show();

                log("GUI restarted after migration in container: " + (here() != null ? here().getName() : "unknown"));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // Log migration completion (will use System.out if controller null, or GUI after recreation)
        log("Migration completed.");
        
        String here = (here() != null) ? here().getName() : "unknown";
        System.out.println("[Agent " + getLocalName() + "] Arrived in container: " + here);
    }


	    /*===========================
	      ========= CLONING =========
	      ===========================*/

	    public void cloneAgent(String newName) {
	        new Thread(() -> {
	            try {
	                log("Cloning to new agent: " + newName + " ...");
	                doClone(here(), newName);
	            } catch (Exception e) {
	                log("Clone failed: " + e.getMessage());
	                e.printStackTrace();
	            }
	        }).start();
	    }

	    @Override
	    protected void beforeClone() {
	        log("Preparing to clone...");
	    }

	    @Override
	    protected void afterClone() {
	        log("Cloning completed. I am now " + getLocalName());

	        Platform.runLater(() -> {
	            try {
	                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agent.fxml"));
	                Scene scene = new Scene(loader.load());
	                controller = loader.getController();
	                controller.setAgent(this);

	                guiStage = new Stage();
	                guiStage.setTitle(getLocalName());
	                guiStage.setScene(scene);
	                guiStage.show();

	                log("GUI started for cloned agent: " + getLocalName());
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        });
	    }

}
