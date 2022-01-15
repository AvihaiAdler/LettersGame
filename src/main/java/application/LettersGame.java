package application;

import org.tinylog.Logger;

import application.game.MainWindow;
import javafx.application.Application;
import javafx.stage.Stage;
	
public class LettersGame extends Application {
	private MainWindow app;
	
	@Override
	public void start(Stage stage) throws Exception {
		stage.close();
		Logger.info("Starting letters game");
		app = new MainWindow("config.json", "./data/letters_game.csv");
		app.start();
	}
	
	public void stop() {
		app.close();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

}
