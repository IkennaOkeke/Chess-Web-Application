package com.chess.controller;

import com.chess.service.GameService;
import com.chess.model.MoveRequest;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.CrossOrigin;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/game")
public class GameController {
    private final GameService gameService;

    public GameController(GameService gameService) {
        this.gameService = gameService;
    }

    @GetMapping("/board")
    public String[][] getBoard() {
        return gameService.getBoard();
    }

    @PostMapping("/move")
    public ResponseEntity<String> makeMove(@RequestBody MoveRequest moveRequest) {
        if (moveRequest.getTo() == null) {
            return ResponseEntity.badRequest().body(moveRequest.getFrom() + moveRequest.getTo());
        }
        Object move = gameService.makeMove(moveRequest.getFrom(), moveRequest.getTo());
        if (move.equals(-6)) {
            return ResponseEntity.badRequest().body("Game is over. Restart to play again.");
        }
        if (move.equals(-5)) {
            return ResponseEntity.badRequest().body("Black in check, valid move not made.");
        }
        if (move.equals(-4)) {
            return ResponseEntity.badRequest().body("White in check, valid move not made.");
        }
        if (move.equals(-3)) {
            return ResponseEntity.badRequest()
                    .body("Invalid move: The selected move is not valid for the chosen piece.");
        }
        if (move.equals(-2)) {
            return ResponseEntity.badRequest()
                    .body("Invalid move: The selected piece does not belong to the current player.");
        }
        if (move.equals(-1)) {
            return ResponseEntity.badRequest().body("Invalid move: No piece at the source position.");
        }
        if (move.equals(1)) {
            return ResponseEntity.ok("Move successful!");
        }
        if (move.equals(2)) {
            return ResponseEntity.ok("Checkmate! Winner: WHITE!");
        }
        if (move.equals(3)) {
            return ResponseEntity.ok("Checkmate! Winner: BLACK!");
        }
        if (move.equals(4)) {
            return ResponseEntity.ok("White now in check.");
        }
        if (move.equals(5)) {
            return ResponseEntity.ok("Black now in check");
        }
        if (move.equals(6)) {
            return ResponseEntity.ok("White castle.");
        }
        if (move.equals(7)) {
            return ResponseEntity.ok("Black castle.");
        }
        if (move.equals(8)) {
            return ResponseEntity.ok("Stalemate. Game has ended in a draw.");
        }

        return ResponseEntity.badRequest().body("An error has occurred. Please restart the game.");

    }

    @PostMapping("/restart")
    public ResponseEntity<String> restart() {
        return ResponseEntity.ok(gameService.restartGame());
    }

}