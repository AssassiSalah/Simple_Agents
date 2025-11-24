package app;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Paint;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;

import jade.wrapper.AgentContainer;

import java.util.Arrays;
import java.util.List;


public class AgentController {

    @FXML private TextFlow logArea;

    public MyAgent agent;
    public String color;

    public void setAgent(MyAgent agent, AgentContainer container) {
        this.agent = agent;
        this.color = getColor(agent.getLocalName());
        MainApp.controllers.add(this);
        MainApp.agentsNames.put(agent.getLocalName(), this.color);
    }
    
    private static int dynamicColorIndex = 0;
    private String getColor(String agentName) {
        // Fixed colors for first 4 agents
        switch (agentName) {
            case "Agent1":
                return "#e74c3c"; // Red
            case "Agent2":
                return "#3498db"; // Blue
            case "Agent3":
                return "#2ecc71"; // Green
            case "Agent4":
                return "#9b59b6"; // Purple
        }
        
        List<String> dynamicColors = Arrays.asList(
                "#1abc9c", // Teal
                "#e67e22", // Orange
                "#34495e", // Dark Gray
                "#f1c40f", // Yellow 
                "#f39c12", // Amber
                "#16a085", // Deep Teal
                "#c0392b", // Dark Red
                "#2980b9"  // Deep Blue
        );

        // For other agents → assign from dynamic list if not already assigned
        if (!MainApp.agentsNames.containsKey(agentName)) {
            String color = dynamicColors.get(dynamicColorIndex % dynamicColors.size());
            MainApp.agentsNames.put(agentName, color);
            dynamicColorIndex++;
        }

        return MainApp.agentsNames.get(agentName);
    }

    // Action codes
    public static final int ACTION_NAME      = 1;
    public static final int ACTION_CONTAINER = 2;
    public static final int ACTION_CLONE     = 3;
    public static final int ACTION_TRANSFER  = 4;
    public static final int ACTION_SEND      = 5;
    public static final int ACTION_BROADCAST = 6;
    public static final int ACTION_KILL      = 7;
    // ----------------- BASIC ACTIONS -----------------
    @FXML void onKill() {
    	log("Killing agent...");
    	agent.performAction(AgentAction.KILL);
    	//OR
    	//agent.performAction(ACTION_KILL);
    }

    @FXML void onCleanLog() {
    	logArea.getChildren().clear();
    }

    // ----------------- NAME -----------------
    @FXML public void onShowName() {
    	agent.performAction(AgentAction.NAME);
    	//OR
		//agent.performAction(ACTION_NAME);
    }

    // ----------------- Container -----------------
    @FXML public void onShowContainer() {
    	agent.performAction(AgentAction.CONTAINER);
    	//OR
    	//agent.performAction(ACTION_CONTAINER);
    }

    // ----------------- MESSAGING ----------------
	@FXML
	private void onSendMessage() {
	    Dialog<Pair<String, String>> dialog = createDialog(
	        "Send Message",
	        "Select receiver and write your message",
	        "Send"
	    );
	
	    GridPane grid = createGrid();
	
	    ComboBox<String> receiverCombo = new ComboBox<>();
	    receiverCombo.setPromptText("Choose Receiver Agent");
	    receiverCombo.getItems().addAll(MainApp.getAvailableAgents(agent));
	
	    TextField messageField = new TextField();
	    messageField.setPromptText("Message Content");
	
	    grid.add(new Label("Receiver:"), 0, 0);
	    grid.add(receiverCombo, 1, 0);
	    grid.add(new Label("Message:"), 0, 1);
	    grid.add(messageField, 1, 1);
	
	    dialog.getDialogPane().setContent(grid);
	
	    // Enable/disable button
	    Node sendButton = dialog.getDialogPane().lookupButton(dialog.getDialogPane().getButtonTypes().get(0));
	    sendButton.setDisable(true);
	
	    receiverCombo.valueProperty().addListener((obs, oldVal, newVal) ->
	        sendButton.setDisable(newVal == null || messageField.getText().trim().isEmpty())
	    );
	    messageField.textProperty().addListener((obs, oldVal, newVal) ->
	        sendButton.setDisable(receiverCombo.getValue() == null || newVal.trim().isEmpty())
	    );
	
	    dialog.setResultConverter(dialogButton -> {
	        if (dialogButton == dialog.getDialogPane().getButtonTypes().get(0)) {
	            return new Pair<>(receiverCombo.getValue(), messageField.getText());
	        }
	        return null;
	    });
	
	    dialog.showAndWait().ifPresent(data ->
	    {
	    	String receiver = data.getKey();
	        String msg = data.getValue();
		    if (receiver == null || msg == null || receiver.isBlank() || msg.isBlank()) {
		        showWarning("Invalid Input", "Please select a receiver and enter a message.");
		        return;
		    }
		    agent.performAction(AgentAction.SEND, receiver, msg);
		    //OR
		    //agent.performAction(ACTION_SEND, receiver, msg);
	    });
	}
	
	@FXML
	void onBroadcastMessage() {
	    if (MainApp.agentsNames.isEmpty()) {
	        showWarning("No Agents", "No agents available for broadcast.");
	        return;
	    }
	
	    TextInputDialog messageDialog = new TextInputDialog();
	    messageDialog.setTitle("Broadcast Message");
	    messageDialog.setHeaderText("Send a message to all agents");
	    messageDialog.setContentText("Message:");
	
	    messageDialog.showAndWait().ifPresent(text -> {
	    	agent.performAction(AgentAction.BROADCAST, text);
	    	//OR
	    	//agent.performAction(ACTION_BROADCAST, text);
	    });
	}
	
	// ----------------- CLONE -----------------
	@FXML
	void onCloneAgent() {
	    Dialog<Pair<String, String>> dialog = createDialog(
	        "Clone Agent",
	        "Enter the clone name and choose the target container:",
	        "Clone"
	    );
	
	    GridPane grid = createGrid();
	
	    TextField nameField = new TextField(agent.getLocalName() + "_clone");
	
	    ComboBox<String> containerCombo = new ComboBox<>();
	    containerCombo.getItems().addAll(MainApp.containers.keySet());
	    if (!containerCombo.getItems().isEmpty()) {
	        containerCombo.setValue(containerCombo.getItems().get(0));
	    }
	
	    grid.add(new Label("Clone Name:"), 0, 0);
	    grid.add(nameField, 1, 0);
	    grid.add(new Label("Container:"), 0, 1);
	    grid.add(containerCombo, 1, 1);
	
	    dialog.getDialogPane().setContent(grid);
	
	    dialog.setResultConverter(dialogButton -> {
	        if (dialogButton == dialog.getDialogPane().getButtonTypes().get(0)) {
	            return new Pair<>(nameField.getText(), containerCombo.getValue());
	        }
	        return null;
	    });
	
	    dialog.showAndWait().ifPresent(pair -> {
	        String cloneName = pair.getKey();
	        String targetContainer = pair.getValue();
	        if (cloneName == null || cloneName.isBlank() || agent == null) {
		        log("Invalid clone name or agent not initialized.");
		        return;
		    }
		    if (MainApp.agentsNames.containsKey(cloneName)) {
		        showWarning("Duplicate Name", "An agent with this name already exists.");
		        return;
		    }
		    
		    log("Cloning agent to: " + cloneName + " in container: " + targetContainer);
		    agent.performAction(AgentAction.CLONE, cloneName, targetContainer);
		    //OR
		    //agent.performAction(ACTION_CLONE, cloneName, targetContainer);
	    });
	}
	
	// ----------------- TRANSFER -----------------
	@FXML
	void onTransfetAgent() {
	    Dialog<String> dialog = new Dialog<>();
	    dialog.setTitle("Transfer Agent");
	    dialog.setHeaderText("Select or create a target container for Transfer:");
	
	    ButtonType transferButtonType = new ButtonType("Transfer", ButtonBar.ButtonData.OK_DONE);
	    dialog.getDialogPane().getButtonTypes().addAll(transferButtonType, ButtonType.CANCEL);
	
	    GridPane grid = createGrid();
	
	    ComboBox<String> containerCombo = createContainerCombo();
	    TextField newContainerField = attachNewContainerField(containerCombo, grid);
	
	    grid.add(new Label("Target Container:"), 0, 0);
	    grid.add(containerCombo, 1, 0);
	    dialog.getDialogPane().setContent(grid);
	
	    dialog.setResultConverter(dialogButton -> {
	        if (dialogButton == transferButtonType) {
	            if ("New Container".equals(containerCombo.getValue()))
	                return newContainerField.getText();
	            return containerCombo.getValue();
	        }
	        return null;
	    });
	
	    dialog.showAndWait().ifPresent(this::handleMigration);
	}
	
	/** Handles transfer logic */
	private void handleMigration(String targetName) {
	    if (targetName == null || targetName.isBlank()) {
	        showWarning("Invalid Selection", "Please enter or select a valid container name.");
	        return;
	    }

	    if (agent == null || agent.here() == null) {
	        showWarning("Error", "Agent not initialized.");
	        return;
	    }

	    String currentContainer = agent.here().getName();
	    if (targetName.equals(currentContainer)) {
	        showWarning("Same Container", "The agent is already in container '" + currentContainer + "'.");
	        return;
	    }

	    try {
	        AgentContainer targetContainer = MainApp.containers.get(targetName);
	        if (targetContainer == null) {
	            targetContainer = createAgentContainer(targetName);
	            MainApp.containers.put(targetName, targetContainer);
	            log("New container '" + targetName + "' created successfully.");
	            System.out.println("New container '" + targetName + "' created successfully.");
	        }

	        log("Migration initiated to container: " + targetName);
	        Object t = agent.performAction(AgentAction.TRANSFER, targetContainer);
	        //OR
	        //Object t = agent.performAction(ACTION_TRANSFER, targetContainer);

	        if (t instanceof Boolean success && success) {
	            log("Migration successful to container " + targetName);
	        } else {
	            showWarning("Migration Failed", "Agent transfer failed.");
	        }
	    } catch (Exception ex) {
	        ex.printStackTrace();
	        showWarning("Migration Failed", ex.getMessage());
	    }
	}

    private AgentContainer createAgentContainer(String name) {
        try {
            log("Creating new container: " + name + " ...");
            jade.core.Runtime rt = jade.core.Runtime.instance();
            jade.core.ProfileImpl profile = new jade.core.ProfileImpl();
            profile.setParameter(jade.core.Profile.CONTAINER_NAME, name);
            return rt.createAgentContainer(profile);
        } catch (Exception e) {
            log("Failed to create container: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
	
	// ---------- COMMON UTILITIES ----------
	/** Standard grid for dialog layout */
	private GridPane createGrid() {
	    GridPane grid = new GridPane();
	    grid.setHgap(10);
	    grid.setVgap(10);
	    grid.setPadding(new Insets(20, 150, 10, 10));
	    return grid;
	}

	/** Creates a combo box with existing containers */
	private ComboBox<String> createContainerCombo() {
	    ComboBox<String> combo = new ComboBox<>();
	    combo.getItems().addAll(MainApp.containers.keySet());
	    combo.getItems().add("New Container");
	    if (!combo.getItems().isEmpty()) {
	        combo.setValue(combo.getItems().get(0));
	    }
	    return combo;
	}

	/** Attaches a new-container text field to the combo */
	private TextField attachNewContainerField(ComboBox<String> combo, GridPane grid) {
	    TextField field = new TextField();
	    field.setPromptText("Enter new container name...");
	    field.setVisible(false);
	    combo.setOnAction(e -> field.setVisible("New Container".equals(combo.getValue())));
	    grid.add(field, 1, 1);
	    return field;
	}

    // ----------------- UTILITIES -----------------
    private Dialog<Pair<String, String>> createDialog(String title, String header, String okButtonText) {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(header);

        ButtonType okButtonType = new ButtonType(okButtonText, ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        return dialog;
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    public void log(String msg) {
    	Text senderText = new Text("[" + agent.getLocalName() + "] ");
        senderText.setFill(Paint.valueOf(color)); // 
        senderText.setStyle("-fx-font-weight: bold;");
        
        Text messageText = new Text(msg + "\n");
        messageText.setFill(Paint.valueOf(color)); // black text for message
        
        appendLog(senderText, messageText);
        System.out.println("[" + agent.getLocalName() + "] " + msg);
    }
    
    public void appendLog(Text senderText, Text messageText) {
        Platform.runLater(() -> logArea.getChildren().addAll(senderText, messageText));
    }
    
    public void appendColoredLog(String sender, String message, String color) {
        Platform.runLater(() -> {
            // Sender part (colored)
            Text senderText = new Text("[" + sender + "] ");
            senderText.setFill(Paint.valueOf(color));
            senderText.setStyle("-fx-font-weight: bold;");

            // Message part (default color)
            Text messageText = new Text(message + "\n");
            messageText.setFill(Paint.valueOf(color)); // black text for message

            // Add to the TextFlow
            appendLog(senderText, messageText);
        });
    }
}
