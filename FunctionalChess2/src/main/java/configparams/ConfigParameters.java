package configparams;

import functional_chess_model.GameVariant;

import java.util.List;
import java.util.stream.Stream;

public class ConfigParameters {

    public static final String[] variantNames = Stream.of(GameVariant.values())
        .map(Enum::toString)
        .map(input -> {
            if (input == null || input.isEmpty()) return input;
            if (input.length() == 1) return input.toUpperCase();
            return Character.toUpperCase(input.charAt(0)) + input.substring(1).toLowerCase();
        })
        .map(input -> {
            if (input == null) return null;
            if (input.equals("Almostchess")) return "Almost Chess";
            return input + " Chess";
        })
        .toArray(String[]::new);

    public static final String[] variantSizes = Stream.of(GameVariant.values())
        .map(variant -> variant.rows() + "x" + variant.cols())
        .toArray(String[]::new);

    public static final List<String> variantEnumNames =
        Stream.of(GameVariant.values())
            .map(Enum::toString)
            .toList();

    public static final String TIMER_TOGGLE = "Timer";
    public static final String BOARD_BUTTON = "Board Button";
    public static final String RESET_BUTTON = "Reset";
    public static final String SAVE_BUTTON = "Save";
    public static final String LOAD_BUTTON = "Load";
    public static final String BACK_BUTTON = "Back";
    public static final String EXIT_BUTTON = "Exit";
    public static final String NEW_PIECES_BUTTON = "New Pieces";
}
