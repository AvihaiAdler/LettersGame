package application.util;

import java.util.Random;
import application.gui.CrossPanel;
import application.gui.LettersPanel;
import application.gui.Screen;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class ScreenGenerator {
	private final double width;
	private final double height;
	private final Color bgColor;
	private final Random random;

	public ScreenGenerator(double screenWidth, double screenHeight, Color bgColor) {
		this.width = screenWidth;
		this.height = screenHeight;
		this.bgColor = bgColor;
		random = new Random();
	}

	/*
	 * Creates a screen with a sequence of 5 'V', 'W' letters
	 */
	public Screen createLettersScreen(String[] possibleStrings) {
	  var index = random.nextInt(possibleStrings.length);
		var lettersPanel = new LettersPanel(possibleStrings[index]);
		lettersPanel.styleText();
		return new Screen(lettersPanel, ScreenType.Letters, width, height, bgColor);
	}

	/*
	 * Creates a screen with a cross in the middle of it
	 */
	public Screen createCrossScreen(int proportion, int lineWidth) {
		var screen = new CrossPanel(Color.rgb(220, 220, 220), proportion, lineWidth, height, height);
		return new Screen(screen, ScreenType.Cross, width, height, bgColor);
	}

	public Screen createBlankPanel() {
		return new Screen(new StackPane(), ScreenType.Blank, width, height, bgColor);
	}
}
