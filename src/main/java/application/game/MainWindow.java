package application.game;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.tinylog.Logger;
import application.dal.ConfigureManager;
import application.dal.DataOutputHandler;
import application.dal.DataType;
import application.dal.StimulusSender;
import application.gui.LettersPanel;
import application.util.ConfigValues;
import application.util.ScreenGenerator;
import application.util.ScreenType;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class MainWindow extends Stage {
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
	private final Random rand;
	
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
		
		rand = new Random();
		var screenDim = Screen.getPrimary().getBounds();
		screenGenerator = new ScreenGenerator(screenDim.getWidth(), screenDim.getHeight(), Color.BLACK);
		
		this.addEventFilter(KeyEvent.KEY_PRESSED, this::keyBoardEventHandler);
	}
	
	private void keyBoardEventHandler(KeyEvent e) {
		if(e.isControlDown() && e.getCode() == KeyCode.C)
			terminate(true);
		
		if(currentScreen.getType() == ScreenType.Welcome)
		  showNext();
		
    if (currentScreen.getType() == ScreenType.Letters) {
      switch (e.getCode()) {
        case LEFT, V -> {
          interactedMilliTime = System.currentTimeMillis(); // get the time of user interaction
          userAnswer = ((LettersPanel) currentScreen.getRoot()).getMiddleLetter() == 'V';
          showNext();
        }
        case RIGHT, U -> {
          interactedMilliTime = System.currentTimeMillis(); // get the time of user interaction
          userAnswer = ((LettersPanel) currentScreen.getRoot()).getMiddleLetter() == 'U';
          showNext();
        }
        default -> userAnswer = false;
      }
//      if (e.getCode() == KeyCode.LEFT || e.getCode() == KeyCode.RIGHT) {
//        interactedMilliTime = System.currentTimeMillis(); // get the time of user interaction
//        switch (e.getCode()) {
//          case RIGHT -> userAnswer = ((LettersPanel) currentScreen.getRoot()).getMiddleLetter() == 'V';
//          case LEFT -> userAnswer = ((LettersPanel) currentScreen.getRoot()).getMiddleLetter() == 'U';
//          default -> userAnswer = false;
//        }
//        showNext();
//      }
    }
	}
	
	private void createTimer(double millis) {
		Logger.info("creating a new timer with " + millis + "ms delay");
		if (timer != null)
			timer.stop();
		timer = new Timeline(new KeyFrame(Duration.millis(millis), e -> showNext()));
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

		currentScreen = screenGenerator.createWelcomeScreen();
		
		this.initStyle(StageStyle.UNDECORATED);
		this.setScene(currentScreen);
		this.setMaximized(true);
		this.setResizable(false);
		this.centerOnScreen();
		
		signal(100L, 0L);
		this.show();
	}
	
  private void showNext() {
	  switch (currentScreen.getType()) {
		  case Welcome -> {
			  currentScreen = screenGenerator.createCrossScreen(40, 8);
			  createTimer(0.5 * 1000);
		  }
		  case Cross -> {
			  saveResults(getData(), false);
			  currentScreen = screenGenerator.createLettersScreen(configValues.getStrings());
			  signal(5000L, 0L);
			  displayedMilliTime = System.currentTimeMillis();
			  interactedMilliTime = 0;
			  createTimer(0.8 * 1000);
		  }
		  case Letters -> {
			  saveResults(getData(), false);
			  signal(7000L, 0L);
			  var color = userAnswer ? Color.DARKGREEN : Color.DARKRED;
			  var feedback = userAnswer ? "צדקת!" : "טעית!";
			  currentScreen = screenGenerator.createFeedbackScreen(feedback, color);
			  userAnswer = false;
			  createTimer((rand.nextDouble(0.2 - 0.06) + 0.06) * 10000);
		  }
		  case Feedback -> {
			  saveResults(getData(), true);
			  gamesCounter++;
			  currentScreen = screenGenerator.createCrossScreen(40, 8);
			  createTimer(0.5 * 1000);
		  }
	  }

    if (gamesCounter < configValues.getNumOfGames()) {
      Logger.info("Switching to " + currentScreen.getType() + " screen");
      this.setScene(currentScreen);
    } else {
      terminate(false);
    }
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
				case Letters -> (gamesCounter + 1) + ","
						+ (interactedMilliTime == 0 ? "no response" : (interactedMilliTime - displayedMilliTime)) + ","
						+ ((LettersPanel) currentScreen.getRoot()).getMiddleLetter() + "," + userAnswer;
				default -> "";
			};
	}
	
	public void terminate(boolean forced) {
	  if(forced)
	    saveResults("", true);
	  
		signal(200L, 0L);
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
   * 100 - app start
   * 200 - app shutdown
   * 5000 - stim for when the user chooses a the letter
   * 7000 - stim for when the user gets the blinking screen
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
