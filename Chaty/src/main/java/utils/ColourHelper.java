package utils;

import javafx.application.Application;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public final class ColourHelper {

    /**
     * Generates a list of strings containing colour names
     *
     * @return a list of colours
     */
    private static List<String> colourList() {
        String colourAsWord = "white,red,green,yellow,blue,pink,gray,brown," +
                "orange,purple,aquamarine,crimson,deeppink,navy,wheat,chocolate,lime,silver,golden,yellowgreen";
        String[] colours = colourAsWord.split(",");

        return Arrays.asList(colours);
    }


    /**
     * Returns random Colour object based on a list
     *
     * @return a Color
     */
    public static Color generateRandomColour() {
        List<String> colours = ColourHelper.colourList();
        Random rand = new Random();
        int randomNumber = rand.nextInt(colours.size() - 1);

        String colourValue = colours.get(randomNumber);

        return Color.valueOf(colourValue.toUpperCase());
    }


}
