package application.game;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.tinylog.Logger;
import application.dao.ConfigureManager;
import application.dao.DataOutputHandler;
import application.dao.DataType;
import application.dao.StimulusSender;
import application.gui.LettersPanel;
import application.util.ConfigValues;
import application.util.ScreenGenerator;
import application.util.ScreenType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainWindow extends Stage {
	private final double width;
	private final double height;	
	private int gamesCounter;
	private long displayedMilliTime;
	private long interactedMilliTime;
	private Timeline timer;
	private application.gui.Screen currentScreen;	
	private boolean userAnswer;
	private ConfigValues configValues;
	private StimulusSender stimSender;
	private final DataOutputHandler dataHandler;
	private final ScreenGenerator screenGenerator;
	
	// blink params
	private Color color;
	private boolean isBlack;
	private int blinkCounter;
	
	public MainWindow(String configFileName, String dataFileName) throws Exception {
		this.dataHandler = new DataOutputHandler(dataFileName);

		try {
			// reading configuration values
			configValues = (new ConfigureManager(configFileName)).getProperties();					
			dataHandler.writeLine(getColumnsNames(), DataType.Title);
			stimSender = new StimulusSender(configValues.getHost(), configValues.getPort());	
			stimSender.open();			
		} catch (IOException io) {
			Logger.error(io);
		}	catch (Exception e) {
		  Logger.error(e);
		  throw e;
		}
		
		var screenDim = Screen.getPrimary().getBounds();
		width = screenDim.getWidth();
		height = screenDim.getHeight();
		screenGenerator = new ScreenGenerator(width, height, Color.BLACK);
		
		this.addEventFilter(KeyEvent.KEY_PRESSED, this::keyBoardEventHandler);
		this.addEventFilter(MouseEvent.MOUSE_CLICKED, this::mouseEventHandler);
	}
	
	private void keyBoardEventHandler(KeyEvent e) {
		if(e.isControlDown() && e.getCode() == KeyCode.C)
			terminate();
	}
	
	private void mouseEventHandler(MouseEvent e) {		
		if(currentScreen != null && currentScreen.getType() == ScreenType.Letters) {
			if(e.getButton() == MouseButton.PRIMARY || e.getButton() == MouseButton.SECONDARY) {
				interactedMilliTime = System.currentTimeMillis();	//get the time of user interaction
				switch(e.getButton()) {
				case PRIMARY:
					userAnswer = ((LettersPanel)currentScreen.getRoot()).getMiddleLetter() == 'V' ? true : false;
					break;
				case SECONDARY:
					userAnswer = ((LettersPanel)currentScreen.getRoot()).getMiddleLetter() == 'U' ? true : false;
					break;
				default:
					userAnswer = false;
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
	
	/*
	 * starts the app. this method has to be called externally
	 */
	public void start() {
		Logger.info("Constructing main window");
		gamesCounter = 0;
		userAnswer = false;
		color = Color.BLACK;
		isBlack = true;

		currentScreen = screenGenerator.createCrossScreen(40, 8);

		this.initStyle(StageStyle.UNDECORATED);
		this.setScene(currentScreen);
		this.setMaximized(true);
		this.setResizable(false);
		this.centerOnScreen();
		
		signal(111, Instant.now().toEpochMilli());
		createTimer(0.5 * 1000);
		this.show();
	}
	
	private void showNext() {
		switch (currentScreen.getType()) {
		case Cross:
			saveResults(getData(), false);
			
			currentScreen = screenGenerator.createLettersScreen(configValues.getStrings());
			displayedMilliTime = System.currentTimeMillis();
			interactedMilliTime = 0;
			createTimer(0.8 * 1000);
			break;
		case Letters:
			saveResults(getData(), false);
			signal(333, Instant.now().toEpochMilli());
			
			currentScreen = screenGenerator.createBlankPanel();
			createTimer(0.2 * 1000);
			
			// blink params
			color = userAnswer ? Color.DARKGREEN : Color.DARKRED;
			userAnswer = false;
			blinkCounter = 0;
			break;
		case Blank:
			var shouldBreak = blink();
			blinkCounter++;
			if(shouldBreak)
				break;
			
			saveResults(getData(), true);
			
			gamesCounter++;
			
			currentScreen = screenGenerator.createCrossScreen(40, 8);
			createTimer(0.5 * 1000);
			break;
		}
		
		
		if(gamesCounter < configValues.getNumOfGames()) {
			Logger.info("Switching to " + currentScreen.getType() + " screen");
			this.setScene(currentScreen);	
		} else {
			terminate();			
		}
	}
	
	private boolean blink() {
		if(blinkCounter < 5) {
			if(isBlack) 
				this.getScene().setFill(color);
			else 
				this.getScene().setFill(Color.BLACK);
			isBlack = !isBlack;
			return true;
		} else if(blinkCounter == 5) {
			this.getScene().setFill(Color.BLACK);
			createTimer(1.2 * 1000);
			isBlack = true;
			return true;
		}
		return false;
	}
	
	private String getData() {
		Logger.info("Getting data for [" + currentScreen.getType() + "] screen");
		return 
			switch (currentScreen.getType()) {
				case Cross -> {
					var session = "-";
					if (gamesCounter == 0)
						session = "start";
					else if (gamesCounter == configValues.getNumOfGames() - 1)
						session = "end";
					yield session + "," + LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + ",";
				}
				case Letters -> {
					yield (gamesCounter + 1) + ","
							+ (interactedMilliTime == 0 ? "no response" : (interactedMilliTime - displayedMilliTime)) + ","
							+ ((LettersPanel) currentScreen.getRoot()).getMiddleLetter() + "," + userAnswer;
				}
				case Blank -> {
					yield "";
				}
			};
	}
	
	public void terminate() {
		saveResults("", true);
		signal(222, Instant.now().toEpochMilli());
		Logger.info("Terminating program");
		Platform.exit();
	}
	
	public void close() {
		try {
			dataHandler.close();
			stimSender.close();
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
	
  /*
   * send a signal to a server. 
   * 111 - app start
   * 222 - app shutdown
   * 333 - test signal
   */
  public void signal(long signal, long timeStamp) {
    try {
      stimSender.send(signal, timeStamp);
    } catch (IOException e) {
      Logger.error(e);
    }
  }
	
	public String getColumnsNames() {	
		return Stream.of(configValues.getColumns())
				.map(String::valueOf)
				.collect(Collectors.joining(","));
	}
}
