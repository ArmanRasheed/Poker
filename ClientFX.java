import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

public class ClientFX extends Application {

	HashMap<String, Scene> sceneMap;
	Client client;
	boolean validPort = false; boolean validHost = false;
	boolean validAnte = false; boolean validPP = true;
	String host; int port;
	ListView<String> listItems;
	Text pHand, dHand, result, roundEarning, money;

	public static void main(String[] args) { launch(args); }

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Poker!!!");
		sceneMap = new HashMap<String,Scene>();
		listItems = new ListView<String>();
		sceneMap.put("root", welcome(primaryStage));

		primaryStage.setScene(sceneMap.get("root")); // game starts here at the root node
		primaryStage.show(); // show the screen to the user
	}

	public Scene welcome(Stage primaryStage) {
		BorderPane pane = new BorderPane();
		Text hello = new Text("Welcome to 3 card Poker!");
		hello.setFont(Font.font("Verdana", FontWeight.BOLD, 30));
		hello.setFill(Color.DARKBLUE);

		Text dir = new Text("Enter your IP and Port Info Below:");
		dir.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
		dir.setFill(Color.BLACK);

		Text hD = new Text("IP: ");
		hD.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
		hD.setFill(Color.BLACK);

		Text pD = new Text("Port: ");
		pD.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));
		pD.setFill(Color.BLACK);

		TextField hT = new TextField();
		hT.setPromptText("Enter IP address here");
		hT.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));

		TextField pT = new TextField();
		pT.setPromptText("Enter port number here");
		pT.setFont(Font.font("Verdana", FontWeight.NORMAL, 16));

		HBox h = new HBox(10, hD, hT);
		h.setAlignment(Pos.CENTER);

		HBox p = new HBox(10, pD, pT);
		p.setAlignment(Pos.CENTER);

		Button connect = new Button("Connect!");
		connect.setDisable(true);
		connect.setFont(Font.font("Verdana", FontWeight.BOLD, 16));
		connect.setStyle("-fx-background-color: #3CB371; -fx-text-fill: white; -fx-font-weight: bold;");

		VBox UI = new VBox(20, hello, dir, h, p, connect);
		UI.setAlignment(Pos.CENTER);

		hT.textProperty().addListener((observable, oldValue, newValue) -> {
			validHost = !(newValue.isEmpty());
			if (validPort && validHost) connect.setDisable(false);
			else connect.setDisable(true);
		});

		pT.textProperty().addListener((observable, oldValue, newValue) -> {
			// Check if the entered text is a valid integer
			validPort = newValue.matches("\\d+");
			if (validPort && validHost) connect.setDisable(false);
			else connect.setDisable(true);
		});

		connect.setOnAction(e -> {
			host = hT.getText(); port = Integer.parseInt(pT.getText());

			try {
				client = new Client(data->{
					Platform.runLater(()->{ listItems.getItems().add(data.toString()); });
				}, host, port);
				client.start();
			} catch(Exception ex) {
				client.stopClient();
				Platform.runLater(()->{
					listItems.getItems().add("Error: " + ex.getMessage());
				});
				hT.clear(); pT.clear();
				connect.setDisable(true);
			}

			primaryStage.setOnCloseRequest(event -> {
				client.stopClient();
			});

			sceneMap.put("game", game(primaryStage));
			primaryStage.setScene(sceneMap.get("game"));
			primaryStage.show();
		});

		pane.setCenter(UI);
		pane.setStyle("-fx-background-color: #F0ECEC;");
		return new Scene(pane, 500, 400);
	}

	public Scene game(Stage primaryStage) {
		BorderPane pane = new BorderPane();

		//							****************** TOP BAR ******************

		MenuItem mI2,mI3,mI4; MenuBar mB1; Menu m1;
		m1 = new Menu("Options"); mB1 = new MenuBar(); // bar to hold menu
		mI2 = new MenuItem("Fresh Start"); // Reset the game
		mI3 = new MenuItem("Exit");       // Exit Application
		mI3.setOnAction(e->{ client.stopClient(); Platform.exit(); });
		mI4 = new MenuItem("New Look"); // menu to change the look of the game...
		mI4.setOnAction(e -> {
			Random rand = new Random();
			int r = rand.nextInt(256);
			int g = rand.nextInt(256);
			int b = rand.nextInt(256);
			pane.setStyle("-fx-background-color: rgb(" + r + "," + g + "," + b + ");");
		});
		m1.getItems().addAll(mI2,mI4,mI3); // adding all the menus to the bar...
		mB1.getMenus().add(m1);

		//							****************** TOP BAR ******************
		//							****************** MAIN UI ******************

		Text dealerT = new Text("Dealer Hand: ");
		dHand = new Text();
		Text playerT = new Text("Your Hand: ");
		pHand = new Text();
		HBox dealer = new HBox(10, dealerT, dHand);
		dealer.setAlignment(Pos.CENTER);
		HBox player = new HBox(10, playerT, pHand);
		player.setAlignment(Pos.CENTER);
		VBox hands = new VBox(20, dealer, player);
		hands.setAlignment(Pos.CENTER);

		Text gameInfo = new Text("Connected to Server! - Place your bet and hit DEAL!");

		Text anteT = new Text("Place Ante Bet ($5-$25): ");
		TextField anteF = new TextField();
		Text playT = new Text("Play Wager: ");
		TextField playF = new TextField();
		playF.setEditable(false);
		Text ppT = new Text("Place Optional Pair-Plus Bet ($5-$25): ");
		TextField ppF = new TextField();
		HBox ante = new HBox(10, anteT, anteF);
		ante.setAlignment(Pos.CENTER);
		HBox play = new HBox(10, playT, playF);
		play.setAlignment(Pos.CENTER);
		HBox pp = new HBox(10, ppT, ppF);
		pp.setAlignment(Pos.CENTER);
		HBox abet = new HBox(20, ante, play);
		abet.setAlignment(Pos.CENTER);
		VBox bet = new VBox(10, abet, pp);
		bet.setAlignment(Pos.CENTER);

		Text moneyT = new Text("Total Winnings: $");
		money = new Text("0");
		HBox winnings = new HBox(5, moneyT, money);
		winnings.setAlignment(Pos.CENTER);

		Button deal = new Button("DEAL!");
		deal.setDisable(true);
		Button fold = new Button("Fold Hand");
		fold.setDisable(true);
		Button playB = new Button("Play Hand");
		playB.setDisable(true);
		HBox butts = new HBox(30, deal, playB, fold);
		butts.setAlignment(Pos.CENTER);

		VBox gameUI = new VBox(40, hands, gameInfo, bet, winnings, butts);
		gameUI.setAlignment(Pos.CENTER);
		pane.setTop(mB1); pane.setCenter(gameUI);

		//							****************** MAIN UI ******************
		//							****************** EVENTS  ******************

		anteF.textProperty().addListener((observable, oldValue, newValue) -> {
			// Check if the entered text is a valid integer between 5 and 25
			validAnte = newValue.matches("[5-9]|1[0-9]|2[0-5]");
			deal.setDisable(!(validAnte && validPP));
		});

		ppF.textProperty().addListener((observable, oldValue, newValue) -> {
			// Check if the entered text is either empty or a valid integer between 5 and 25
			validPP = newValue.isEmpty() || newValue.matches("[5-9]|1[0-9]|2[0-5]");
			deal.setDisable(!(validAnte && validPP));
		});

		deal.setOnAction(e->{
			if (ppF.getText().isEmpty()) ppF.setText("0");
			client.send(new PokerInfo("Ante:" + anteF.getText() + ",PP:" + ppF.getText(), client.getID()));
			deal.setDisable(true);
			anteF.setEditable(false); ppF.setEditable(false);
			playB.setDisable(false); fold.setDisable(false);

			try {
				Thread.sleep(1000); // wait for the server to respond
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

			client.updateLatestData(client.getHand());
			pHand.setText(client.getLatestData().toString());
			client.updateLatestData(client.getDealer());
			dHand.setText(client.getLatestData().toString());
			dHand.setVisible(false);
			if(ppF.getText().isEmpty()) gameInfo.setText("Player Hand Dealt - Play or Fold?");
			else {
				int cash = client.getPP();
				if (cash > 0) gameInfo.setText("Pair Plus Win for $" + cash + "!");
				else gameInfo.setText("No Pair Plus Win :(");
			}
		});

		fold.setOnAction(e->{
			client.send(new PokerInfo("Fold", client.getID()));

			try {
				Thread.sleep(1000); // wait for the server to respond
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

			result = new Text(); roundEarning = new Text();
			client.updateLatestData(client.getTotal());
			money.setText(client.getLatestData().toString());
			result.setText("Folded - lost the ante bet");
			client.updateLatestData(client.getRoundWin());
			int cash = (int)client.getLatestData();
			if(cash >= 0) roundEarning.setText("Pair Plus Win!: $" + cash);
			else roundEarning.setText("Money lost: $ " + cash);
			sceneMap.put("end", result(primaryStage));
			primaryStage.setScene(sceneMap.get("end"));
			primaryStage.show();
		});

		playB.setOnAction(e-> {
			dHand.setVisible(true); playF.setText(anteF.getText());
			result = new Text(); roundEarning = new Text();
			playB.setDisable(true); fold.setDisable(true);
			client.send(new PokerInfo("Play", client.getID()));

			try {
				Thread.sleep(1000); // wait for the server to respond
			} catch (InterruptedException ex) {
				ex.printStackTrace();
			}

			client.updateLatestData(client.getRes());
			int res = (int)client.getLatestData();
			if (res == 0) gameInfo.setText("Dealer does not have at least Queen high; ante wager is pushed");
			else if (res == 1) gameInfo.setText("Player beat dealer!");
			else if (res == -1) gameInfo.setText("Player loses dealer :(");
			client.updateLatestData(client.getTotal());
			money.setText(client.getLatestData().toString());
			client.updateLatestData(client.getRoundWin());
			int cash = (int)client.getLatestData();
			if (cash >= 0) result.setText("Player won $" + cash + "!");
			else result.setText("Player lost $" + cash + " :(");

			// create a PauseTransition object with a duration of 2 seconds
			PauseTransition delay = new PauseTransition(Duration.seconds(5));
			// set the action to be performed after the delay
			delay.setOnFinished(event -> {
				sceneMap.put("end", result(primaryStage));
				primaryStage.setScene(sceneMap.get("end"));
				primaryStage.show();
			});
			// start the delay
			delay.play();
		});

		mI2.setOnAction(e-> {
			client.send(new PokerInfo("RESET", client.getID()));
			money.setText("" + client.getTotal()); // update money label
			sceneMap.put("game", game(primaryStage));
			primaryStage.setScene(sceneMap.get("game"));
			primaryStage.show();
		});

		//							****************** EVENTS  ******************

		return new Scene(pane, 500, 400);
	}

	public Scene result(Stage primaryStage) {
		BorderPane pane = new BorderPane();
		Button newGame = new Button("Another Game?");
		Button exit = new Button("Quit :)");
		HBox butts = new HBox(20, newGame, exit);
		butts.setAlignment(Pos.CENTER);
		VBox resUI = new VBox(20, result, roundEarning, butts);
		resUI.setAlignment(Pos.CENTER);
		pane.setCenter(resUI);

		exit.setOnAction(e-> { client.stopClient(); Platform.exit(); } );
		newGame.setOnAction(e -> {
			client.send(new PokerInfo("New!", client.getID()));
			sceneMap.put("game", game(primaryStage));
			money.setText("" + client.getTotal());
			primaryStage.setScene(sceneMap.get("game"));
			primaryStage.show();
		});

		return new Scene(pane, 500, 400);
	}
}
