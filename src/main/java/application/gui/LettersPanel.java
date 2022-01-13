package application.gui;

import application.util.ScreenType;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class LettersPanel extends StackPane implements Panel {
	private final ScreenType type;
	private final Text text;
	private final char middleLetter;

	public LettersPanel(String text) {
		this.text = new Text(text);
		type = ScreenType.Letters;
		middleLetter = text.charAt(text.length() / 2);

		this.getChildren().add(this.text);
	}

	public void styleText() {
		text.setFont(Font.font("Roboto", FontWeight.BOLD, FontPosture.REGULAR, 80));
		text.setFill(Color.rgb(220, 220, 220));
		text.setTextAlignment(TextAlignment.CENTER);
	}

	public char getMiddleLetter() {
		return middleLetter;
	}

	@Override
	public ScreenType getType() {
		return type;
	}
}
