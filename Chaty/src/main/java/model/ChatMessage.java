package model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

/**
 * Message object that is sent to the queue/topic
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
