package application.gui;

import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class WelcomePanel extends BorderPane {
  private final double width;
  private final Text welcome;
  private final Text instructions;
  private final Text last;
  
  public WelcomePanel(double width) {
    this.width = width;
    
    instructions = new Text();
    welcome = new Text();
    last = new Text();
    construct();
    
    var container =  new VBox();
    container.getChildren().addAll(welcome, instructions, last);
    container.setAlignment(Pos.CENTER);
    BorderPane.setAlignment(container, Pos.CENTER);
    this.setCenter(container);
  }
  
  private void construct() {
    style("ברוך הבא!", welcome, 40, TextAlignment.CENTER);
    String text = """
                  במשחק זה יוצגו לפניך מחרוזות בנות 5 אותיות לזמן קצר.
                  עליך לזהות את האות האמצעית.
                  לחץ על החץ השמאלי / על האות V במידה והאות האמצעית היא V.
                  לחץ על החץ הימני / על האות U במידה והאות האמצעית היא U.

                  בהצלחה!
                  """;
    style(text, instructions, 30, TextAlignment.RIGHT);
    style("לחץ על מקש כלשהו כדי להמשיך", last, 40, TextAlignment.CENTER);
  }
  
  private void style(String str, Text text, int fontSize, TextAlignment alignment) {
    text.setFont(Font.font("Roboto", FontWeight.BOLD, FontPosture.REGULAR, fontSize));
    text.setText(str);
    text.setWrappingWidth(width/2);
    text.setFill(Color.WHITE);
    text.setTextAlignment(alignment);
  }
}
