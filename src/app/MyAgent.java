package app;

import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.wrapper.AgentContainer;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MyAgent extends Agent {
    private transient Stage guiStage;
    private transient AgentController controller;

    @Override
    protected void setup() {
    	addBehaviour(new CyclicBehaviour() {
			
			@Override
			public void action() {
		        //MainApp.updateAgentsSentMsg(getLocalName(), receiverName, message);
				ACLMessage msg = receive(); // Get MSG
				if(msg != null) {
					controller.appendColoredLog(msg.getSender().getLocalName(),  msg.getContent(), MainApp.agentsNames.get(msg.getSender().getLocalName()));
					System.out.println("Message received at " + getLocalName());
				} else {
					block(); // wait until new msg arrive
				}
				
				
			}
		});
        Platform.runLater(this::createAndShowGUI);
        
    }

    private void createAndShowGUI() {
    	Platform.runLater(() -> {
	        try {
	            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/agent.fxml"));
	            Scene scene = new Scene(loader.load());
	            controller = loader.getController();
	            controller.setAgent(this, getContainerController());
	            
	            guiStage = new Stage();
	            guiStage.setTitle(getLocalName());
	            guiStage.setScene(scene);
	            guiStage.show();
	
	            controller.log("Started.");
	
	            // Handle X button close
	            guiStage.setOnCloseRequest(event -> {
	                event.consume(); // Prevent default close
	                System.out.println("GUI requested killing agent With X Button: " + getLocalName());
	                doDelete();
	                guiStage.close();
	            });
	
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		});
    }
    
    protected Object performAction(AgentAction action, Object... args) {
        switch (action) {
        	case NAME:
        		System.out.println("Getting agent name: " + getLocalName());
        		controller.log("Getting agent name: " + getLocalName());
				return getLocalName();
        	case CONTAINER:
        		System.out.println("Getting container name: " + here().getName());
        		controller.log("Getting container name: " + here().getName());
        		return here().getName();
            case CLONE:
                if (args.length > 1 && args[0] instanceof String && args[1] instanceof String) {
                    cloneAgent((String) args[0], (String) args[1]);
                } else {
                	controller.log("⚠ Missing argument: new agent name for clone.");
                }
                break;
            case TRANSFER:
                if (args.length > 0 && args[0] instanceof AgentContainer) {
                    return transferAgent((AgentContainer) args[0]);
                } else {
                	controller.log("⚠ Missing argument: destination container for transfer.");
                }
                break;
            case SEND:
                if (args.length > 1 && args[0] instanceof String && args[1] instanceof String) {
                    sendMessage((String) args[0], (String) args[1]);
                } else {
                	controller.log("⚠ Missing arguments: target agent name and message.");
                }
                break;
            case BROADCAST:
				if (args.length > 0 && args[0] instanceof String) {
					ACLMessage message = new ACLMessage(ACLMessage.INFORM);
			        for (String name : MainApp.getAvailableAgents(this)) {
			            message.addReceiver(new AID(name, AID.ISLOCALNAME));
			        }
			        String text = (String) args[0];
			        message.setContent(text);
			        send(message);

			        controller.log("Broadcasted message: " + text);
				} else {
					controller.log("⚠ Missing arguments: message to broadcast.");
				}
				break;
            case KILL:
                doDelete();
                controller.log("Agent terminated.");
                break;
            default:
            	controller.log("❌ Unknown action: " + action);
                break;
        }
        return null;
    }
    
    // Action codes
    public static final int ACTION_NAME      = 1;
    public static final int ACTION_CONTAINER = 2;
    public static final int ACTION_CLONE     = 3;
    public static final int ACTION_TRANSFER  = 4;
    public static final int ACTION_SEND      = 5;
    public static final int ACTION_BROADCAST = 6;
    public static final int ACTION_KILL      = 7;
    protected Object performAction(int action, Object... args) {
        switch (action) {
        	case ACTION_NAME:
        		System.out.println("Getting agent name: " + getLocalName());
        		controller.log("Getting agent name: " + getLocalName());
				return getLocalName();
        	case ACTION_CONTAINER:
        		System.out.println("Getting container name: " + here().getName());
        		controller.log("Getting container name: " + here().getName());
        		return here().getName();
            case ACTION_CLONE:
                if (args.length > 1 && args[0] instanceof String && args[1] instanceof String) {
                    cloneAgent((String) args[0], (String) args[1]);
                } else {
                	controller.log("⚠ Missing argument: new agent name for clone.");
                }
                break;
            case ACTION_TRANSFER:
                if (args.length > 0 && args[0] instanceof AgentContainer) {
                    return transferAgent((AgentContainer) args[0]);
                } else {
                	controller.log("⚠ Missing argument: destination container for transfer.");
                }
                break;
            case ACTION_SEND:
                if (args.length > 1 && args[0] instanceof String && args[1] instanceof String) {
                    sendMessage((String) args[0], (String) args[1]);
                } else {
                	controller.log("⚠ Missing arguments: target agent name and message.");
                }
                break;
            case ACTION_BROADCAST:
				if (args.length > 0 && args[0] instanceof String) {
					ACLMessage message = new ACLMessage(ACLMessage.INFORM);
			        for (String name : MainApp.getAvailableAgents(this)) {
			            message.addReceiver(new AID(name, AID.ISLOCALNAME));
			        }
			        String text = (String) args[0];
			        message.setContent(text);
			        send(message);

			        controller.log("Broadcasted message: " + text);
				} else {
					controller.log("⚠ Missing arguments: message to broadcast.");
				}
				break;
            case ACTION_KILL:
                doDelete();
                controller.log("Agent terminated.");
                break;
            default:
            	controller.log("❌ Unknown action: " + action);
                break;
        }
        return null;
    }


    @Override
    protected void takeDown() {
        Platform.runLater(() -> {
        	MainApp.agentsNames.remove(getLocalName());
            if (guiStage != null) 
            	guiStage.close();
        });
        System.out.println("["+ getLocalName() + "]"+ " terminated.");
    }

    // Send Message
    private void sendMessage(String receiverName, String message) {
	    controller.log("Sending message to " + receiverName + ": " + message);
        ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
        msg.addReceiver(new AID(receiverName, AID.ISLOCALNAME));
        msg.setContent(message);
        send(msg);

        controller.log("Sent message to " + receiverName + ": " + message);
	    //System.out.println("Message sent from " + getLocalName() + " to " + receiverName + ": " + msg);
    }

    // Transfer Agent
    private boolean transferAgent(AgentContainer container) {
        if (container == null) {
        	controller.log("Transfer failed: agent or container is null.");
            return false;
        }
        try {
            String targetName = container.getContainerName();
            ContainerID dest = new ContainerID(targetName, null);

            controller.log("Transfaring '" + getLocalName() + "' → '" + targetName + "'");
            
            // To Ensure no GUI references are serialized
            cleanupBeforeSerialization();
            
            doMove(dest);
            return true;
        } catch (Exception ex) {
            ex.printStackTrace();
            controller.log("Transfer ERROR: " + ex.getMessage());
            return false;
        }
    }
    
    private void cleanupBeforeSerialization() {
        // Null the controller immediately (avoid serialization of GUI)
        controller = null;

        // Close Stage on JavaFX thread
        Platform.runLater(() -> {
            try {
                if (guiStage != null) {
                    guiStage.close();
                    guiStage = null;
                }
            } catch (Exception ignored) {}
        });
    }

    
    @Override
    protected void beforeMove() {
    	//controller.log("Preparing to transfer...");
        // immediate null
        controller = null;
        // schedule UI close
        Platform.runLater(() -> {
            if (guiStage != null) {
                guiStage.close();
                guiStage = null;
            }
        });
    }


    @Override
    protected void afterMove() {
    	Platform.runLater(() -> {
            createAndShowGUI();
            // debug
            System.out.println("afterMove: GUI recreated for " + getLocalName() + " in " + (here()!=null?here().getName():"?"));
        });
    }

    // Clone
    public void cloneAgent(String newName, String targetContainerName) {
        new Thread(() -> {
            try {
            	controller.log("Cloning to new agent: " + newName + " in container: " + targetContainerName + " ...");

                // Get target container (default to current if missing)
                jade.wrapper.AgentContainer targetContainer = MainApp.containers.get(targetContainerName);
                jade.core.Location targetLocation;

                if (targetContainer != null) {
                    // Convert to JADE Location (ContainerID)
                    targetLocation = new ContainerID(targetContainerName, null);
                } else {
                	controller.log("Target container not found. Using current container instead.");
                    targetLocation = here(); // fallback
                }

                // Perform the actual clone
                doClone(targetLocation, newName);

            } catch (Exception e) {
            	controller.log("Clone failed: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    protected void afterClone() {
        //log("Cloning completed. I am now " + getLocalName());
        Platform.runLater(this::createAndShowGUI);
    }
}
