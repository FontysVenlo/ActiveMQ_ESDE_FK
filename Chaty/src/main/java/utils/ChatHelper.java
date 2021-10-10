package utils;

import javafx.geometry.Insets;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextBoundsType;
import javafx.scene.text.TextFlow;
import model.ChatMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatHelper {

    /**
     * Creates a chat message from the received message object to be displayed to the user
     *
     * @param chatMessage message object
     * @return a string containing the message in the desired form
     */
    private static String createMessageFromObject(ChatMessage chatMessage){
        StringBuffer sb = new StringBuffer();
        sb.append(chatMessage.getUser().getUsername() + " " +chatMessage.getDateTimeSent());
        sb.append("\n");
        sb.append(chatMessage.getMessage());
        return sb.toString();
    }

    /**
     * Creates a {@link Text} object from the received message object that displays the users logo
     *
     * @param chatMessage received message object
     * @return a new Text object populated with the desired username initial
     */
    private static Text createTextFromReceivedMessage(ChatMessage chatMessage){
        String userLabelText = chatMessage.getUser().getUsername().substring(0, 1);
        Text userLogoText = new Text(userLabelText);
        userLogoText.setBoundsType(TextBoundsType.VISUAL);
        userLogoText.setFont(new Font(20));
        return  userLogoText;
    }

    /**
     * Creates a box, sets the icon for the user and returns a HBox to be added to the chat
     *
     * @param receivedMessage the received message object
     * @return a HBox containing the message
     */
    public static HBox displayReceivedMessage(ChatMessage receivedMessage) {
        HBox hbox = new HBox(12);

        // Add user Circle
        Circle img = new Circle(32, 32, 16);
        img.setFill(Color.valueOf(receivedMessage.getUser().getColor()));

        // Generate the user icon
        Text userLogoText = ChatHelper.createTextFromReceivedMessage(receivedMessage);

        // Added to a pane
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(img, userLogoText);

        // Generate the message
        Text text = new Text(ChatHelper.createMessageFromObject(receivedMessage));
        TextFlow tempFlow = new TextFlow();
        tempFlow.getChildren().add(text);
        tempFlow.setMaxWidth(200);

        // add the Nodes to the HBox
        hbox.getChildren().add(stackPane);
        hbox.getChildren().add(tempFlow);

        // Set Message box padding
        hbox.setPadding(new Insets(5));

        return hbox;
    }

    /**
     * Returns the current local date time as a string in the dd-MM-yyyy HH:mm format
     *
     * @return current local date time as a string
     */
    public static String returnCurrentLocalDateTimeAsString() {
        LocalDateTime dateTime = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String timeSent = dateTime.format(format);
        return timeSent;
    }
}
