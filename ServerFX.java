import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.util.HashMap;

public class ServerFX extends Application {

	HashMap<String, Scene> sceneMap;
	ListView<String> listItems;
	Stage serverStage;
	Server server;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Poker Server");
		sceneMap = new HashMap<String,Scene>();
		listItems = new ListView<String>();
		sceneMap.put("root", createInitialScreen());
		sceneMap.put("server",createServerGui());

		primaryStage.setScene(sceneMap.get("root")); // game starts here at the root node
		primaryStage.show(); // show the screen to the user
	}

	public Scene createInitialScreen() {
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(20));

		// Create the UI controls
		Label portLabel = new Label("Enter a valid Port Number here to enable server:");
		portLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

		TextField portText = new TextField();
		portText.setPrefWidth(200);

		Button enable = new Button("Enable Server");
		enable.setDisable(true);

		portText.textProperty().addListener((observable, oldValue, newValue) -> {
			// Check if the entered text is a valid integer
			enable.setDisable(!newValue.matches("\\d+"));
		});

		Button disable = new Button("Disable Server");
		disable.setDisable(true);

		enable.setOnAction(e -> {
			server = new Server(data -> {
				Platform.runLater(()->{ listItems.getItems().add(data.toString()); });
			}, Integer.parseInt(portText.getText()));

			Scene sceneToDisplay = sceneMap.get("server");
			serverStage = new Stage();
			serverStage.setTitle("Server Port: " + portText.getText());
			serverStage.setScene(sceneToDisplay);
			serverStage.show();

			serverStage.setOnCloseRequest(event -> {
				server.stop();
				portText.setText("Enter a valid Port Number here to enable server!");
				enable.setDisable(true); disable.setDisable(true);
			});

			enable.setDisable(true);
			disable.setDisable(false);
		});

		disable.setOnAction(e -> {
			server.stop();
			serverStage.close();
			portText.setText("Enter a valid Port Number here to enable server!");
			enable.setDisable(true); disable.setDisable(true); listItems.getItems().clear();
		});

		HBox butts = new HBox(10, enable, disable);
		butts.setAlignment(Pos.CENTER);

		// Add the controls to the pane
		pane.setTop(portLabel);
		pane.setCenter(portText);
		pane.setBottom(butts);

		// Set the styles
		pane.setStyle("-fx-background-color: #f0f0f0;");
		portLabel.setStyle("-fx-padding: 0 0 10 0;");
		enable.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
		disable.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");
		butts.setStyle("-fx-padding: 10;");
		butts.setSpacing(20);
		butts.setAlignment(Pos.CENTER);
		portText.setStyle("-fx-border-color: #aaa; -fx-border-radius: 5;");
		portText.setFont(Font.font("Arial", FontWeight.BOLD, 16));

		return new Scene(pane, 500, 400);
	}

	public Scene createServerGui() {
		BorderPane pane = new BorderPane();
		pane.setPadding(new Insets(70));
		pane.setStyle("-fx-background-color: lavender");
		pane.setCenter(listItems);
		return new Scene(pane, 500, 400);
	}
}
