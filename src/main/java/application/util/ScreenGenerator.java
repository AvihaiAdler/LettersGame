package application.util;

import java.util.Random;
import application.gui.BlankPanel;
import application.gui.CrossPanel;
import application.gui.LettersPanel;
import javafx.scene.paint.Color;

public class ScreenGenerator {
	private final double width;
	private final double height;

	public ScreenGenerator(double screenWidth, double screenHeight) {
		this.width = screenWidth;
		this.height = screenHeight;
	}

	/*
	 * Creates a screen with a sequence of 5 'V', 'W' letters
	 */
	public LettersPanel createLettersScreen(String[] possibleStrings) {
		var random = new Random();

		var lettersPanel = new LettersPanel(possibleStrings[random.nextInt(possibleStrings.length)].trim());
		lettersPanel.styleText();
		return lettersPanel;
	}

	/*
	 * Creates a screen with a cross in the middle of it
	 */
	public CrossPanel createCrossScreen(Color color, int proportion, int lineWidth) {
		return new CrossPanel(color, proportion, lineWidth, width, height);
	}

	public BlankPanel createBlankPanel() {
		return new BlankPanel();
	}
}
