package app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import jade.wrapper.AgentContainer;
import jade.core.Runtime;
import jade.core.ContainerID;
import jade.core.Profile;
import jade.core.ProfileImpl;

public class AgentController {

    @FXML private TextArea logArea;
    @FXML private TextField cloneNameField;
    @FXML private ComboBox<String> containerCombo;

    private MyAgent agent;

    public void setAgent(MyAgent agent) {
        this.agent = agent;
        MainApp.agentsName.add(agent.getLocalName());
        // Initialize with main container
        updateContainerCombo();
    }

    // ----------------- BASIC ACTIONS -----------------
    @FXML
    void onKill() {
        log("Killing agent...");
        System.out.println("GUI requested killing agent: " + agent.getLocalName() + ((agent != null) ? "" : " (agent reference is null)"));
        if (agent != null) agent.doDelete();
    }

    @FXML
    void onCleanLog() {
        logArea.clear();
    }

    // ----------------- CLONE -----------------
    @FXML
    void onCloneAgent() {
    	Platform.runLater(() -> {		
    		cloneNameField.setText(agent.getLocalName() + "_clone");
	        cloneNameField.setVisible(true);
	        cloneNameField.setPromptText("Enter clone name");
	
	        cloneNameField.setOnAction(e -> {
	            String cloneName = cloneNameField.getText();
	            if (cloneName != null && !cloneName.isBlank() && agent != null) {
	            	// 🔹 Check if name already exists in the global list
	                if (MainApp.agentsName.contains(cloneName)) {
	                    showWarning("Duplicate Name",
	                        "An agent with this name already exists. Please choose another name.");
	                    cloneNameField.requestFocus();
	                    return;
	                }
	            	
	                log("Cloning agent to: " + cloneName + "...");
	                agent.cloneAgent(cloneName); // real clone method
	                cloneNameField.clear();
	                cloneNameField.setVisible(false);
	            } else {
	                log("Invalid clone name or agent not initialized.");
	            }
	        });
    	});
    }

    // ----------------- MIGRATE -----------------
    @FXML
	void onMigrateAgent() {
    	Platform.runLater(() -> {		
		    containerCombo.setVisible(true);
		    updateContainerCombo();
		
		    containerCombo.setOnAction(e -> {
		        String selected = containerCombo.getValue();
		        if (selected == null || selected.equals("Choose Container")) return;
		        
		        // Check if agent is already in the selected container
	            if (agent != null && agent.here() != null) {
	                String currentContainer = agent.here().getName();
	                if (selected.equals(currentContainer)) {
	                    showWarning("Same Container",
	                        "The agent is already in container '" + currentContainer + "'.");
	                    //onMigrateAgent(); // re-open combo after warning
	                    updateContainerCombo();
	                    return; // stop migration
	                }
	            }
		
		        if (selected.equals("New Container")) {
		        	
		            // Ask for new container name
		            TextInputDialog dialog = new TextInputDialog();
		            dialog.setTitle("Create New Container");
		            dialog.setHeaderText(null);
		            dialog.setContentText("Enter new container name:");
		
		            dialog.showAndWait().ifPresent(name -> {
		                if (name.isBlank()) {
		                    showWarning("Invalid name", "Container name cannot be empty.");
		                    return;
		                }
		                
		                if(name.equals("Choose Container") || name.equals("New Container")) {
		                    showWarning("Invalid name", "Container name cannot be '" + name + "'.");
		                    return;
		                }
		
		                // Access the global containers map (from the other class)
		                if (MainApp.containers.containsKey(name)) {
		                    showWarning("Duplicate Container", 
		                        "A container with this name already exists. Please choose another name.");
		                    onMigrateAgent(); // re-open combo after warning
		                } else {
		                    try {
		                        // Create new JADE container
		                        AgentContainer newContainer = createAgentContainer(name);
		                        MainApp.containers.put(name, newContainer);
		
		                        log("Container '" + name + "' created successfully.");
		                        updateContainerCombo();
		
		                        migrateAgent(newContainer);
		
		                    } catch (Exception ex) {
		                        ex.printStackTrace();
		                        showWarning("Error", "Failed to create container: " + ex.getMessage());
		                    }
		                }
		            });
		        } else {
		            // Migrate to an existing container
		            migrateAgent(MainApp.containers.get(selected));
		        }
		    });
    	});
	}

	/** Utility method to show a warning dialog */
	private void showWarning(String title, String message) {
	    Alert alert = new Alert(Alert.AlertType.WARNING);
	    alert.setTitle(title);
	    alert.setHeaderText(null);
	    alert.setContentText(message);
	    alert.showAndWait();
	}
	
	/** Performs the actual migration of the agent to the specified container */
	private void migrateAgent(AgentContainer container) {
	    if (agent == null) {
	        log("Migration failed: agent reference is null.");
	        return;
	    }
	    if (container == null) {
	        log("Migration failed: target container is null.");
	        return;
	    }

	    try {
	        String targetName = container.getContainerName();
	        if (targetName == null || targetName.isBlank()) {
	            log("Migration failed: invalid container name.");
	            return;
	        }

	        // **CORRECT ContainerID CREATION**
	        ContainerID dest = new ContainerID();
	        dest.setName(targetName);
	        
	        // For same-platform migration, address can be null/localhost
	        // JADE will handle local routing automatically
	        
	        log("Migrating '" + agent.getLocalName() + "' → '" + targetName + "'");
	        agent.doMove(dest);
	        
	        // Hide combo after migration starts
	        Platform.runLater(() -> containerCombo.setVisible(false));
	        
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        log("Migration ERROR: " + ex.getMessage());
	        showWarning("Migration Failed", ex.getMessage());
	    }
	}


    /**
     * Creates a new JADE container synchronously.
     * @param name Name of the new container.
     * @return The created AgentContainer, or null if creation failed.
     */
    private AgentContainer createAgentContainer(String name) {
        try {
            log("Creating new container: " + name + " ...");

            Runtime rt = Runtime.instance();
            ProfileImpl profile = new ProfileImpl();
            profile.setParameter(Profile.CONTAINER_NAME, name);

            AgentContainer newContainer = rt.createAgentContainer(profile);
            MainApp.containers.put(name, newContainer);

            return newContainer;

        } catch (Exception e) {
            log("Failed to create container: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    
    private void updateContainerCombo() {
        Platform.runLater(() -> {
            containerCombo.getItems().clear();
            containerCombo.getItems().addAll(MainApp.containers.keySet());
            containerCombo.getItems().add("New Container");
            containerCombo.setValue("Choose Container");
        });
        
        // Set the default value to the container the agent is currently in
        /*if (agent != null && agent.here() != null) {
            String currentContainer = agent.here().getName();
            containerCombo.setValue(currentContainer);
        }*/
    }

    // ----------------- NAME -----------------
    public void onShowName() {
		log("Agent name: " + agent.getLocalName());
	}
    
    // ----------------- Container -----------------
    public void onShowContainer() {
        try {
            if (agent != null && agent.here() != null) {
                String containerName = agent.here().getName();
                log("Current container: " + containerName);
            } else {
                log("Agent or container information unavailable.");
            }
        } catch (Exception e) {
            log("Error retrieving container name: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    // ----------------- UTILITIES -----------------
    public void appendLog(String text) {
        Platform.runLater(() -> logArea.appendText(text + "\n"));
    }

    private void log(String msg) {
        appendLog("[" + agent.getLocalName() + "] " + msg);
        System.out.println("[" + agent.getLocalName() + "] " + msg);
    }
}
