package functional_chess_model;

public record Movement(
    Position init,
    Position fin,
    int dx,
    int dy
) {

    public static Movement of(Position init, Position fin) {
        return new Movement(init, fin, Position.xDist(init, fin), Position.yDist(init, fin));
    }

    public int initX() {
        return this.init.x();
    }

    public int initY() {
        return this.init.y();
    }

    public boolean isDiagonal() {
        return Math.abs(dx) == Math.abs(dy);
    }

    public boolean isStraight() {
        return dx == 0 || dy == 0;
    }

    public boolean isKnightLike() {
        return Math.abs(dx) + Math.abs(dy) == 3
            && Math.abs(dx) <= 2 && Math.abs(dx) >= 1
            && Math.abs(dy) <= 2 && Math.abs(dy) >= 1;
    }

    public boolean isForwardFor(ChessColor color) {
        return dy * color.yDirection() > 0;
    }

    public boolean isDoubleStepForwardFor(ChessColor color, GameVariant variant) {
        return dx == 0 && dy == 2 * color.yDirection() && init.y() == variant.initRow(color);
    }

    public boolean isNull() {
        return dx == 0 && dy == 0;
    }

}

