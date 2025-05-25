package controller.online;

import functional_chess_model.Position;

public interface MoveListener {
    void onMovePerformed(Position initPos, Position finPos);
}

