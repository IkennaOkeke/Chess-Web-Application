package com.chess.model;

import java.util.List;

class Player {
    private Colour colour;

    public Player(Colour colour) {
        this.colour = colour;
    }

    // gets the colour of the player
    public Colour getColour() {
        return colour;
    }

    // gets the move from the player
    public Move getMove(String from, String to, Board board) {
        // System.out.println("Enter your move (e.g., E7 E5). To leave, type 'exit': ");
        // String input = scanner.nextLine();
        // String[] positions;
        // positions[0] = from;
        // positions[1] = to;

        // checks if the desired move is in the right format
        // if (positions.length != 2) {
        // System.out.println("Invalid move: Incorrect input format. Please enter the
        // move in the format 'A2 A4'.");
        // return new Move(new Position(-1, -1), new Position(-1, -1));
        // }

        // translates player's move to readable format e.g., E7 would be position (6, 4)
        Position source = findPosition(from);
        Position destination = findPosition(to);

        // prevents player from moving empty tiles
        Piece piece = board.getPieceAt(source);
        if (piece == null) {
            // System.out.println("Invalid move: No piece at the source position.");
            return new Move(new Position(-1, -1), new Position(-1, -1));
        }

        // checks to see if player is moving their own piece
        Colour currentPlayerColour = getColour();
        Colour pieceColour = piece.getColour();
        if (pieceColour != currentPlayerColour) {
            // System.out.println("Invalid move: The selected piece does not belong to the
            // current player.");
            return new Move(new Position(-1, -2), new Position(-1, -2));
        }

        // gets the list of possible moves the piece can make in its current position
        List<Move> possibleMoves = piece.getPossibleMoves(source, board);
        Move selectedMove = new Move(source, destination);

        // checks if the desired move is legal based on the possible moves
        boolean isValidMove = false;
        for (Move move : possibleMoves) {
            if (move.equals(selectedMove)) {
                isValidMove = true;
                break;
            }
        }

        // prompts the player for a valid move if the desired move is not legal
        if (!isValidMove) {
            // System.out.println("Invalid move: The selected move is not valid for the
            // chosen piece.");
            return new Move(new Position(-1, -3), new Position(-1, -3));
        }

        return selectedMove;

    }

    // finds a position from player input (E7 would be (6, 4))
    public Position findPosition(String input) {
        int col = input.charAt(0) - 'A';
        int row = input.charAt(1) - '1';
        return new Position(row, col);
    }
} // end Player