package functional_chess_model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Record representing the valid positions of a chess board. Contains two
 * int attributes x and y, representing the X and Y coordinate of the
 * position, respectively.
 * @author Alfonso Gallego
 * @param x X coordinate of the Position.
 * @param y Y coordinate of the Position.
 */
public record Position(int x, int y) implements Serializable {
    
    /**
     * Static factory method to create new Positions from a String representing
     * the position in algebraic chess notation (A1, A2, etc.)
     * @param pos String representing the position.
     * @return A new Position constructed by converting the first char of the
     * String to an integer, to store it as the x coordinate of the position,
     * and storing the second as the y coordinate.
     * If the String doesn't have length 2, or if its characters doesn't
     * represent a position in algebraic notation, an error message is printed
     * and {@code null} is returned.
     * @deprecated This method only works for positions with y coordinate of up
     * to 9, so it doesn't work for Chess games with boards of that size. In
     * any case, the {@link Position#of(int, int)} method is generally preferred
     * to create new Positions.
     */
    @Deprecated
    public static Position of(String pos) {
        try {
            if (pos.length()!=2) throw new IllegalArgumentException("The specified String doesn't have length 2.");
            final int x = convertLetterToNumber(pos.charAt(0));
            final int y = Integer.parseInt(""+pos.charAt(1));
            if (x >= 1 && y >= 1) return new Position(x, y);
            else throw new IllegalArgumentException(pos+" represents coordinates ("+x+", "+y+"), which are outside the chess board");
        } catch (IllegalArgumentException e) {
            System.err.println("Error occurred while trying to create the position with value: " + pos + " " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Static factory method to create new Positions from a pair of integers
     * representing the X and Y coordinates of the Position.
     * @param x X coordinate of the new Position.
     * @param y Y coordinate of the new Position.
     * @return Returns a new Position constructed by storing the two integers
     * as the X and Y coordinate.
     * If supposed new Position is not within the Chess board, an error message
     * is printed and {@code null} is returned.
     */
    public static Position of(int x, int y) {
        if (x >= 1 && y >= 1) return new Position(x, y);
        else throw new IllegalArgumentException("Coordinates ("+x+", "+y+") are outside the chess board");
    }
    
    /**
     * Calculates the signed distance in the X axis between two positions.
     * @param initPos The initial position.
     * @param finPos The final position.
     * @return The x coordinate of the final position minus the x coordinate
     * of the initial position.
     */
    public static int xDist(Position initPos, Position finPos) {
        return finPos.x - initPos.x;
    }
    
    /**
     * Calculates the signed distance in the Y axis between two positions.
     * @param initPos The initial position.
     * @param finPos The final position.
     * @return The y coordinate of the final position minus the y coordinate
     * of the initial position.
     */
    public static int yDist(Position initPos, Position finPos) {
        return finPos.y - initPos.y;
    }

    /**
     * Represents the Position using the algebraic chess notation, A1, A2, etc.
     * @return Returns a String whose first character is the x coordinate of the
     * Position converted to a letter (1 -> A,  and so on) and the rest being
     * the digits of the y coordinate.
     */
    @Override
    public String toString() {
        return "" + convertNumberToLetter(x) + y;
    }
    
    /**
     * Static method to convert a letter to a number.
     * @param letter Letter to convert.
     * @return The integer number representning its position in the english
     * alphabet.
     * @hidden 
     */
    public static int convertLetterToNumber(char letter) {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".indexOf(Character.toUpperCase(letter))+1;
    }
    
    /**
     * Static method to convert a number to a letter.
     * @param num Number to convert to a letter.
     * @return The letter in the number's position in the english alphabet.
     * @hidden 
     */
    public static char convertNumberToLetter(int num) {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(num-1);
    }

}