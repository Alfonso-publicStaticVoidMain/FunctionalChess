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
            if (input.equals("Almostchess")) return "Almost Chess";
            return input + " Chess";
        })
        .toArray(String[]::new);

    public static final String[] variantSizes = Stream.of(GameVariant.values())
        .map(variant -> variant.rows() + "x" + variant.cols())
        .toArray(String[]::new);

    public static List<String> variantNamesUpperCase =
        Stream.of(GameVariant.values())
            .map(Enum::toString)
            .toList();

    public static final String timerToggleActionCommand = "Timer";
    public static final String boardButtonActionCommand = "Board Button";
    public static final String resetButtonActionCommand = "Reset";
    public static final String saveButtonActionCommand = "Save";
    public static final String loadButtonActionCommand = "Load";
    public static final String backButtonActionCommand = "Back";
    public static final String exitButtonActionCommand = "Exit";
    public static final String newPiecesButtonActionCommand = "New Pieces";
}
