package com.chess.service;

import com.chess.model.ChessGame;
import org.springframework.stereotype.Service;

@Service
public class GameService {
    private ChessGame chessGame;

    public GameService() {
        this.chessGame = new ChessGame();
    }

    public String[][] getBoard() {
        return chessGame.getBoard();
    }

    public Object makeMove(String from, String to) {
        return chessGame.makeMove(from, to);
    }

    public String restartGame() {
        this.chessGame = new ChessGame();
        return "Game restarted.";
    }

}