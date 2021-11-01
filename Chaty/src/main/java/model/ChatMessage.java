package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Message object that is sent to the topic
 */
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {

    private String message;
    private String dateTimeSent;
    private User user;
}
