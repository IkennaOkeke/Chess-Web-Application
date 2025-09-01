package com.chess.model;

class Board {
    private Piece[][] grid;
    private Move lastMove;
    private String lastSpecialMove;
    private Piece sourcePiece;
    private Piece destinationPiece;

    public Board() {
        grid = new Piece[8][8];
    }

    // places all of the chess pieces in their corresponding starting positions
    public void initialize() {
        // places pawns
        for (int col = 0; col < 8; col++) {
            grid[1][col] = new Pawn(Colour.BLACK);
            grid[6][col] = new Pawn(Colour.WHITE);
        }

        // places rooks
        grid[0][0] = new Rook(Colour.BLACK);
        grid[0][7] = new Rook(Colour.BLACK);
        grid[7][0] = new Rook(Colour.WHITE);
        grid[7][7] = new Rook(Colour.WHITE);

        // places knights
        grid[0][1] = new Knight(Colour.BLACK);
        grid[0][6] = new Knight(Colour.BLACK);
        grid[7][1] = new Knight(Colour.WHITE);
        grid[7][6] = new Knight(Colour.WHITE);

        // places bishops
        grid[0][2] = new Bishop(Colour.BLACK);
        grid[0][5] = new Bishop(Colour.BLACK);
        grid[7][2] = new Bishop(Colour.WHITE);
        grid[7][5] = new Bishop(Colour.WHITE);

        // places queens
        grid[0][3] = new Queen(Colour.BLACK);
        grid[7][3] = new Queen(Colour.WHITE);

        // places kings
        grid[0][4] = new King(Colour.BLACK);
        grid[7][4] = new King(Colour.WHITE);

    }

    // makes a move on the chessboard
    public void makeMove(Move move) {
        lastMove = move;
        lastSpecialMove = null;
        // finds where the piece the player is trying to move is currently placed
        Position source = move.getSource();
        sourcePiece = getPieceAt(source);

        // finds where the player wants to move the piece
        Position destination = move.getDestination();
        destinationPiece = getPieceAt(destination);

        // moves the piece to the desired location
        Piece piece = grid[source.getRow()][source.getCol()];
        grid[source.getRow()][source.getCol()] = null;
        grid[destination.getRow()][destination.getCol()] = piece;
    }

    public void undoMove() {
        if (lastMove != null) {
            Position source = lastMove.getSource();
            Position destination = lastMove.getDestination();

            grid[source.getRow()][source.getCol()] = sourcePiece;
            grid[destination.getRow()][destination.getCol()] = destinationPiece;
        } else {
            Piece king = null;
            Piece rook = null;
            switch (lastSpecialMove) {
                case "WKC":
                    king = grid[7][6];
                    rook = grid[7][5];
                    grid[7][4] = king;
                    grid[7][7] = rook;
                    grid[7][5] = null;
                    grid[7][6] = null;
                    break;
                case "WQC":
                    king = grid[7][2];
                    rook = grid[7][3];
                    grid[7][4] = king;
                    grid[7][0] = rook;
                    grid[7][2] = null;
                    grid[7][3] = null;
                    break;
                case "BKC":
                    king = grid[0][6];
                    rook = grid[0][5];
                    grid[0][4] = king;
                    grid[0][7] = rook;
                    grid[0][5] = null;
                    grid[0][6] = null;
                    break;
                case "BQC":
                    king = grid[0][2];
                    rook = grid[0][3];
                    grid[0][4] = king;
                    grid[0][0] = rook;
                    grid[0][2] = null;
                    grid[0][3] = null;
                    break;
                default:
                    int colTaking = lastSpecialMove.charAt(1) - '0';
                    int rowTaking = lastSpecialMove.charAt(2) - '0';
                    int colTaken = lastSpecialMove.charAt(3) - '0';
                    int rowTaken = lastSpecialMove.charAt(4) - '0';
                    if (lastSpecialMove.charAt(0) == 'W') {
                        grid[rowTaking][colTaking] = new Pawn(Colour.WHITE);
                        grid[rowTaken][colTaken] = new Pawn(Colour.BLACK);
                        grid[rowTaken - 1][colTaken] = null;

                    } else if (lastSpecialMove.charAt(0) == 'B') {
                        grid[rowTaking][colTaking] = new Pawn(Colour.BLACK);
                        grid[rowTaken][colTaken] = new Pawn(Colour.WHITE);
                        grid[rowTaken + 1][colTaken] = null;
                    } else {
                        if (rowTaken == 0) {
                            grid[rowTaking][colTaking] = new Pawn(Colour.WHITE);
                            grid[rowTaken][colTaken] = destinationPiece;
                        } else {
                            grid[rowTaking][colTaking] = new Pawn(Colour.BLACK);
                            grid[rowTaken][colTaken] = destinationPiece;
                        }

                    }
            }
        }

    }

    public Boolean whiteKingsideCastle() {
        Position kingsideBishop = new Position(7, 5);
        Position kingsideKnight = new Position(7, 6);

        if ((getPieceAt(kingsideBishop) != null || getPieceAt(kingsideKnight) != null)) {
            return Boolean.FALSE;
        } else {
            lastMove = null;
            lastSpecialMove = "WKC";
            sourcePiece = null;
            destinationPiece = null;
            Piece king = grid[7][4];
            Piece rook = grid[7][7];
            grid[7][4] = null;
            grid[7][7] = null;
            grid[7][5] = rook;
            grid[7][6] = king;
            return Boolean.TRUE;
        }
    }

    public Boolean whiteQueensideCastle() {
        Position Queen = new Position(7, 3);
        Position QueensideBishop = new Position(7, 2);
        Position QueensideKnight = new Position(7, 1);

        if ((getPieceAt(Queen) != null || getPieceAt(QueensideBishop) != null || getPieceAt(QueensideKnight) != null)) {
            return Boolean.FALSE;
        } else {
            lastMove = null;
            lastSpecialMove = "WQC";
            sourcePiece = null;
            destinationPiece = null;
            Piece king = grid[7][4];
            Piece rook = grid[7][0];
            grid[7][4] = null;
            grid[7][0] = null;
            grid[7][2] = king;
            grid[7][3] = rook;
            return Boolean.TRUE;
        }
    }

    public Boolean blackKingsideCastle() {
        Position kingsideBishop = new Position(0, 5);
        Position kingsideKnight = new Position(0, 6);

        if ((getPieceAt(kingsideBishop) != null || getPieceAt(kingsideKnight) != null)) {
            return Boolean.FALSE;
        } else {
            lastMove = null;
            lastSpecialMove = "BKC";
            sourcePiece = null;
            destinationPiece = null;
            Piece king = grid[0][4];
            Piece rook = grid[0][7];
            grid[0][4] = null;
            grid[0][7] = null;
            grid[0][5] = rook;
            grid[0][6] = king;
            return Boolean.TRUE;
        }
    }

    public Boolean blackQueensideCastle() {
        Position Queen = new Position(0, 3);
        Position QueensideBishop = new Position(0, 2);
        Position QueensideKnight = new Position(0, 1);

        if ((getPieceAt(Queen) != null || getPieceAt(QueensideBishop) != null || getPieceAt(QueensideKnight) != null)) {
            return Boolean.FALSE;
        } else {
            lastMove = null;
            lastSpecialMove = "BQC";
            sourcePiece = null;
            destinationPiece = null;
            Piece king = grid[0][4];
            Piece rook = grid[0][0];
            grid[0][4] = null;
            grid[0][0] = null;
            grid[0][2] = king;
            grid[0][3] = rook;
            return Boolean.TRUE;
        }
    }

    public void enPassant(Position pawnTakingPosition, Position pawnTakenPosition) {
        Piece pawnTaking = getPieceAt(pawnTakingPosition);
        if (pawnTaking.getColour() == Colour.WHITE) {
            lastMove = null;
            lastSpecialMove = "W" + Integer.toString(pawnTakingPosition.getRow())
                    + Integer.toString(pawnTakingPosition.getCol()) + Integer.toString(pawnTakenPosition.getRow())
                    + Integer.toString(pawnTakenPosition.getCol());
            ;
            sourcePiece = null;
            destinationPiece = null;
            grid[pawnTakenPosition.getRow() - 1][pawnTakenPosition.getCol()] = pawnTaking;
            grid[pawnTakingPosition.getRow()][pawnTakingPosition.getCol()] = null;
            grid[pawnTakenPosition.getRow()][pawnTakenPosition.getCol()] = null;
        } else {
            lastMove = null;
            lastSpecialMove = "B" + Integer.toString(pawnTakingPosition.getRow())
                    + Integer.toString(pawnTakingPosition.getCol()) + Integer.toString(pawnTakenPosition.getRow())
                    + Integer.toString(pawnTakenPosition.getCol());
            ;
            sourcePiece = null;
            destinationPiece = null;
            grid[pawnTakenPosition.getRow() + 1][pawnTakenPosition.getCol()] = pawnTaking;
            grid[pawnTakingPosition.getRow()][pawnTakingPosition.getCol()] = null;
            grid[pawnTakenPosition.getRow()][pawnTakenPosition.getCol()] = null;
        }

    }

    public void pawnPromotion(Move move, Piece piece) {
        lastMove = null;
        lastSpecialMove = "P" + Integer.toString(move.getSource().getRow())
                + Integer.toString(move.getSource().getCol()) + Integer.toString(move.getDestination().getRow())
                + Integer.toString(move.getDestination().getCol());
        ;
        sourcePiece = grid[move.getSource().getRow()][move.getSource().getCol()];
        destinationPiece = grid[move.getDestination().getRow()][move.getDestination().getCol()];
        grid[move.getSource().getRow()][move.getSource().getCol()] = null;
        grid[move.getDestination().getRow()][move.getDestination().getCol()] = piece;
    }

    public Move getLastMove() {
        return lastMove;
    }

    public String getLastSpecialMove() {
        return lastSpecialMove;
    }

    public Piece getSourcePiece() {
        return sourcePiece;
    }

    public Piece getDestinationPiece() {
        return destinationPiece;
    }

    public void replaceLastMove(Move move, String string, Piece spiece, Piece dpiece) {
        lastMove = move;
        lastSpecialMove = string;
        sourcePiece = spiece;
        destinationPiece = dpiece;
    }

    // checks if where the player wants to move is within the chessboard
    public boolean isValidPosition(Position position) {
        int row = position.getRow();
        int col = position.getCol();
        return row >= 0 && row < 8 && col >= 0 && col < 8;
    }

    // checks if the position the player wants to go to is empty
    public boolean isEmptyPosition(Position position) {
        if (grid[position.getRow()][position.getCol()] == null) {
            return true;
        }
        return false;
    }

    // removes a taken piece at a certain position
    public Piece getPieceAt(Position position) {
        if (isValidPosition(position)) {
            return grid[position.getRow()][position.getCol()];
        }
        return null;
    }

    // checks if a king of its specific colour the has been taken (if true, the game
    // ends)
    public boolean isKingTaken(Colour colour) {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];
                if (piece instanceof King && piece.getColour() == colour) {
                    return false; // King of the specific colour is still on the board
                }
            }
        }
        return true; // king of the specific colour has been taken
    }

    // @Override
    // //used to display the chessboard's rows(numbers) and columns(letters)
    // public String toString() {
    // StringBuilder display = new StringBuilder();

    // //adds the board's columns
    // display.append(" A B C D E F G H\n");

    // //adds the board's rows
    // for (int row = 0; row < 8; row++) {
    // display.append(row + 1).append(" ");
    // for (int col = 0; col < 8; col++) {
    // Piece piece = grid[row][col];
    // if ((row + col) % 2 == 1) {
    // display.append("\033[102m");
    // }
    // else {
    // display.append("\033[43m");
    // }
    // display.append(piece != null ? piece.getSymbol() : "\033[90m ·").append(" ");
    // display.append("\033[0m");
    // }
    // display.append("\n");
    // }

    // return display.toString();
    // }

    public String[][] getBoard() {
        String[][] newBoard = new String[8][8];
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = grid[row][col];
                if (piece != null) {
                    String colour = (piece.getColour() == Colour.WHITE) ? "W|" : "B|";
                    newBoard[row][col] = colour + piece.getSymbol();
                }
            }
        }
        return newBoard;
    }

} // end Board
