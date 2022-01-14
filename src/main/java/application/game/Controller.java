package application.game;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.tinylog.Logger;
import application.gui.LettersPanel;
import application.gui.Panel;
import application.util.ConfigureManager;
import application.util.DataOutputHandler;
import application.util.DataType;
import application.util.ScreenGenerator;
import application.util.ScreenType;
import application.util.StimulusSender;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Controller {
	private int totalGames;
	private final double width;
	private final double height;	
	private int gamesCounter;
	private long displayedMilliTime;
	private long interactedMilliTime;
	private Timeline timer;
	private Panel currentScreen;
	private StackPane panel;
	// main GUI component
	private Stage stage;	
	// represent the user's answer (right/wrong)
	private boolean answer;
	// represent all configuration values read from config.json
	private Map<String, Object> configValues;
	private StimulusSender sender;
	private final DataOutputHandler dataHandler;
	private final ScreenGenerator screenGenerator;
	private String[] possibleStrings;
	
	// blink params
	private Color color;
	private boolean isBlack;
	private int blinkCounter;
	
	public Controller(Stage stage, String configFileName, String dataFileName) throws FileNotFoundException {
		this.stage = stage;
		this.dataHandler = new DataOutputHandler(dataFileName);

		try {
			// reading configuration values
			configValues = (new ConfigureManager(configFileName)).getProperties();					
			totalGames = (int)configValues.get("number_of_games");
			sender = new StimulusSender((String)configValues.get("host"), (int)configValues.get("port"));	
			sender.open();			
		} catch (FileNotFoundException fof) {
			Logger.error(fof);
			throw fof;
		} catch (IOException io) {
			Logger.error(io);
		}
		possibleStrings = getPossibleStrings();
		if(possibleStrings == null)
			throw new NullPointerException("Couldn't read possible_strings from " + configFileName);
		
		var screenDim = Screen.getPrimary().getBounds();
		width = screenDim.getWidth() / 2;
		height = screenDim.getHeight();
		screenGenerator = new ScreenGenerator(width, height);
		
		stage.addEventFilter(MouseEvent.MOUSE_CLICKED, this::mouseEventHandler);
	}
	
	private void mouseEventHandler(MouseEvent e) {
		if(currentScreen != null && currentScreen.getType() == ScreenType.Letters) {
			if(e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.SECONDARY) {
				interactedMilliTime = System.currentTimeMillis();	//get the time of user interaction
				switch(e.getButton()) {
				case PRIMARY:
					answer = ((LettersPanel)currentScreen).getMiddleLetter() == 'V' ? true : false;
					break;
				case SECONDARY:
					answer = ((LettersPanel)currentScreen).getMiddleLetter() == 'U' ? true : false;
					break;
				default:
					answer = false;
					break;
				}
				showNext();
			}
		}
	}
	
	private void createTimer(double millis) {
		Logger.info("creating a new timer with " + Double.toString(millis) + "ms delay");
		if (timer != null)
			timer.stop();
		timer = new Timeline(new KeyFrame(Duration.millis(millis), new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent e) {
				showNext();
			}
		}));
		timer.setCycleCount(Timeline.INDEFINITE);
		timer.play();
	}
	
	public void show() {
		Logger.info("Constructing main screen");
		gamesCounter = 0;
		answer = false;
		color = Color.BLACK;
		isBlack = true;
		
		panel = new StackPane();
		currentScreen = screenGenerator.createCrossScreen(Color.rgb(220, 220, 220), 40, 8);
		panel.getChildren().add((Pane)currentScreen);
		
		stage.setScene(new Scene(panel, width, height, color));
		stage.setMaximized(true);
		stage.setResizable(false);
		stage.centerOnScreen();
		stage.show();
		writeCriteria();
		
		createTimer(0.5 * 1000);
	}
	
	private void showNext() {
		switch (currentScreen.getType()) {
		case Cross:
			saveResults(getData(), false);
			
			currentScreen = screenGenerator.createLettersScreen(possibleStrings);
			displayedMilliTime = System.currentTimeMillis();
			interactedMilliTime = 0;
			createTimer(0.8 * 1000);
			break;
		case Letters:
			saveResults(getData(), false);
			
			currentScreen = screenGenerator.createBlankPanel();
			createTimer(0.2 * 1000);
			
			// blink params
			color = answer ? Color.DARKGREEN : Color.DARKRED;
			answer = false;
			blinkCounter = 0;
			break;
		case Blank:
			var shouldBreak = blink();
			blinkCounter++;
			if(shouldBreak)
				break;
			
			saveResults(getData(), true);
			
			gamesCounter++;
			
			currentScreen = screenGenerator.createCrossScreen(Color.rgb(220, 220, 220), 40, 8);
			createTimer(0.5 * 1000);
			break;
		}
		
		
		if(gamesCounter < totalGames) {
			Logger.info("Switching to " + currentScreen.getType().toString() + " screen");
			panel.getChildren().clear();
			panel.getChildren().add((Pane)currentScreen);	
		} else {
			terminate();			
		}
	}
	
	private boolean blink() {
		if(blinkCounter < 5) {
			if(isBlack) 
				stage.getScene().setFill(color);
			else 
				stage.getScene().setFill(Color.BLACK);
			isBlack = !isBlack;
			return true;
		} else if(blinkCounter == 5) {
			stage.getScene().setFill(Color.BLACK);
			createTimer(1.2 * 1000);
			isBlack = true;
			return true;
		}
		return false;
	}
	
	private String getData() {
		Logger.info("Getting data for [" + currentScreen.getType() + "] screen");
		return switch (currentScreen.getType()) {
		case Cross -> {
			var session = "-";
			if (gamesCounter == 0)
				session = "start";
			else if (gamesCounter == totalGames - 1)
				session = "end";
			yield session + "," + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ",";
		}
		case Letters -> {
			yield (gamesCounter + 1) + ","
					+ (interactedMilliTime == 0 ? "No response" : (interactedMilliTime - displayedMilliTime)) + ","
					+ ((LettersPanel) currentScreen).getMiddleLetter() + "," + answer;
		}
		case Blank -> {
			yield "";
		}
		};
	}
	
	public void terminate() {
		Logger.info("Terminating program");
		Platform.exit();
	}
	
	public void close() {
		try {
			dataHandler.close();
			sender.close();
		} catch (IOException e) {
			Logger.error(e);
		}
	}
	
	/*
	 * saving the result for a series of 3 games
	 */
	private void saveResults(String data, boolean endLine) {
		try {
			if(endLine)
				dataHandler.writeLine(data, DataType.Data);
			else
				dataHandler.write(data, DataType.Data);
		} catch (IOException e) {
			Logger.error(e);
		}
	}
	
	private String[] getPossibleStrings() {
		return Stream.of(configValues.get("possible_strings"))
				.map(String::valueOf)
				.map(str -> str.replaceAll("[\\[\\]]", ""))
				.collect(Collectors.joining())
				.split(",");
	}
	
	/*
	 * Writes criteria columns into the corresponding .csv file
	 */
	public void writeCriteria() {	
		var title = Stream.of(configValues.get("columns"))
				.map(String::valueOf)
				.map(str -> str.replaceAll("[\\[\\]]", ""))
				.collect(Collectors.joining(","));
		

		try {
			dataHandler.writeLine(title, DataType.Title);
		} catch (IOException e) {
			Logger.error(e);
		}
	}
}
