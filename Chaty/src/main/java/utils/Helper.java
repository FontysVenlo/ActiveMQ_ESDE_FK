package utils;

import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;

public final class Helper {

    /**
     * Generates a list of strings containing colour names
     *
     * @return a list of colours
     */
    public static List<String> colourList(){
        String colourAsWord = "white,red,green,yellow,blue,pink,gray,brown,orange,purple,aquamarine";
        String[] colours = colourAsWord.split(",");

        return Arrays.asList(colours);
    }

}
