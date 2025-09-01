package com.chess.model;

import java.util.ArrayList;
import java.util.List;
import java.lang.Math;

public class ChessGame {
    private Board board; // holds the chessboard
    private Player[] players; // holds the players
    private int currentPlayerIndex; // tells us who's turn it is
    private Boolean inCheck;
    private Boolean gameIsOver;
    private List<Position> inCheckPositions;
    private List<Position> blockPositions;
    private List<Position> givingCheckPositions;
    private List<Move> inCheckLegalMoves;
    private Move lastMove;

    private Boolean whiteKingMoved;
    private Boolean blackKingMoved;
    private Boolean whiteKingsideRookMoved;
    private Boolean whiteQueensideRookMoved;
    private Boolean blackKingsideRookMoved;
    private Boolean blackQueensideRookMoved;

    private List<Position> inCheckPawnPositions;

    // creates the chessboard, players, and starts the game with White's move
    public ChessGame() {
        board = new Board();
        board.initialize();
        players = new Player[2];
        players[0] = new Player(Colour.WHITE);
        players[1] = new Player(Colour.BLACK);
        currentPlayerIndex = 0;
        inCheck = Boolean.FALSE;
        gameIsOver = Boolean.FALSE;
        inCheckPositions = new ArrayList<>();
        blockPositions = new ArrayList<>();
        inCheckLegalMoves = new ArrayList<>();
        givingCheckPositions = new ArrayList<>();
        lastMove = null;

        whiteKingMoved = Boolean.FALSE;
        blackKingMoved = Boolean.FALSE;
        whiteKingsideRookMoved = Boolean.FALSE;
        whiteQueensideRookMoved = Boolean.FALSE;
        blackKingsideRookMoved = Boolean.FALSE;
        blackQueensideRookMoved = Boolean.FALSE;

        inCheckPawnPositions = new ArrayList<>();
    }

    public Object makeMove(String from, String to) {

        if (gameIsOver) {
            return -6;
        }

        if (to == null) {
            return from;
        }
        try {
            Move currentMove = new Move(findPosition(from), findPosition(to));
            Colour colour = currentPlayerIndex == 0 ? Colour.WHITE : Colour.BLACK;
            Colour oppositeColour = currentPlayerIndex == 1 ? Colour.WHITE : Colour.BLACK;
            int returnValue = 0;

            // pawn-promotion
            if (returnValue == 0) {
                returnValue = pawnPromotion(currentMove, colour);
            }

            // en-passant
            if (returnValue == 0) {
                returnValue = enPassant(currentMove, colour);
            }

            // in check move
            if (inCheck) {
                returnValue = makeInCheckMove(currentMove, colour);
            }

            // castle
            if (returnValue == 0) {
                returnValue = castle(from, to, colour);
            }

            // move
            if (returnValue == 0) {
                returnValue = makeMove(currentMove, colour);
                clearInCheckVariables();
            }

            if (returnValue > 0) {
                if (kingInCheck(oppositeColour)) {
                    inCheck = Boolean.TRUE;

                    findInCheckLegalMove(oppositeColour);
                    returnValue = checkCheckmate(colour);
                } else if (checkStalemate(oppositeColour)) {
                    returnValue = 8;
                }
            }

            if (returnValue > 0) {
                currentPlayerIndex = (currentPlayerIndex + 1) % 2;
            }
            return returnValue;
        } catch (Throwable t) {
            return t.toString() + "     |     " + from + to;
        }

    }

    public String[][] getBoard() {
        return board.getBoard();
    }

    private boolean kingInCheck(Colour colour) {

        Position kingPosition = findKing(colour);
        Piece kingPiece = board.getPieceAt(kingPosition);
        // In Order: rook, bishop, knight
        int[] rowDirections = { -1, 1, 0, 0, -1, -1, 1, 1, -2, -2, -1, -1, 1, 1, 2, 2 };
        int[] colDirections = { 0, 0, -1, 1, -1, 1, -1, 1, -1, 1, -2, 2, -2, 2, -1, 1 };

        List<Position> cannotEvadePositions = new ArrayList<>();
        List<Position> totalBlockPositions = new ArrayList<>();
        List<Position> givingCheckTotalPositions = new ArrayList<>();
        List<Position> positions = new ArrayList<>();

        List<Move> kingPossibleMoves = kingPiece.getPossibleMoves(kingPosition, board);
        List<Position> kingPositions = new ArrayList<>();
        for (Move kingMove : kingPossibleMoves) {
            kingPositions.add(kingMove.getDestination());
        }

        for (int i = 0; i < rowDirections.length; i++) {
            int row = kingPosition.getRow() + rowDirections[i];
            int col = kingPosition.getCol() + colDirections[i];
            int count = -1;
            Position newPosition = new Position(row, col);

            // positions = ((i == 4) || (i == 8)) ? new ArrayList<>() : positions;
            positions = new ArrayList<>();

            // checks if the position is valid
            while (board.isValidPosition(newPosition)) {
                count += 1;
                positions.add(newPosition);
                Piece givingCheck = board.getPieceAt(newPosition);

                if (givingCheck == null) {
                    if (i >= 8) {
                        break;
                    }
                    row += rowDirections[i];
                    col += colDirections[i];
                    newPosition = new Position(row, col);
                } else if (givingCheck.getColour() == colour) {
                    break;
                } else {
                    if ((givingCheck instanceof Rook || givingCheck instanceof Queen) && (i < 4)) {
                        cannotEvadePositions.add(new Position(kingPosition.getRow() + rowDirections[i],
                                kingPosition.getCol() + colDirections[i]));
                        int row2 = -1 * rowDirections[i];
                        int col2 = -1 * colDirections[i];
                        cannotEvadePositions
                                .add(new Position(kingPosition.getRow() + row2, kingPosition.getCol() + col2));
                        cannotEvadePositions.addAll(positions);
                        totalBlockPositions.addAll(positions);
                        givingCheckTotalPositions.add(newPosition);
                    } else if ((givingCheck instanceof Bishop || givingCheck instanceof Queen) && (4 <= i) && (i < 8)) {
                        cannotEvadePositions.add(new Position(kingPosition.getRow() + rowDirections[i],
                                kingPosition.getCol() + colDirections[i]));
                        int row2 = -1 * rowDirections[i];
                        int col2 = -1 * colDirections[i];
                        cannotEvadePositions
                                .add(new Position(kingPosition.getRow() + row2, kingPosition.getCol() + col2));
                        cannotEvadePositions.addAll(positions);
                        totalBlockPositions.addAll(positions);
                        givingCheckTotalPositions.add(newPosition);
                    } else if ((count == 0 && givingCheck instanceof Pawn
                            && ((colour == Colour.WHITE && (i == 4 || i == 5))
                                    || (colour == Colour.BLACK && (i == 6 || i == 7))))) {
                        cannotEvadePositions.add(newPosition);
                        totalBlockPositions.add(newPosition);
                        givingCheckTotalPositions.add(newPosition);
                    } else if (givingCheck instanceof King && (i < 8) && count == 0) {
                        cannotEvadePositions.add(newPosition);
                        totalBlockPositions.add(newPosition);
                        givingCheckTotalPositions.add(newPosition);
                    } else if (givingCheck instanceof Knight && (i >= 8)) {
                        cannotEvadePositions.add(newPosition);
                        totalBlockPositions.add(newPosition);
                        givingCheckTotalPositions.add(newPosition);
                    }
                    break;
                }
            }
        }

        boolean returnValue = givingCheckTotalPositions.isEmpty() ? Boolean.FALSE : Boolean.TRUE;

        if (givingCheckPositions.isEmpty()) {
            for (int i = 0; i <= 7; i++) {
                for (int j = 0; j <= 7; j++) {
                    Position moveCanCheck = new Position(i, j);
                    if (!(board.isValidPosition(moveCanCheck))) {
                        continue;
                    }
                    Piece moveCanCheckPiece = board.getPieceAt(moveCanCheck);
                    if (moveCanCheckPiece != null && moveCanCheckPiece.getColour() != colour) {
                        List<Position> piecePositions = new ArrayList<>();
                        if (moveCanCheckPiece.getColour() == Colour.WHITE && moveCanCheckPiece instanceof Pawn) {
                            if (board.isValidPosition(new Position(i - 1, j - 1))) {
                                piecePositions.add(new Position(i - 1, j - 1));
                            }
                            if (board.isValidPosition(new Position(i - 1, j + 1))) {
                                piecePositions.add(new Position(i - 1, j + 1));
                            }

                        } else if (moveCanCheckPiece instanceof Pawn) {
                            if (board.isValidPosition(new Position(i + 1, j - 1))) {
                                piecePositions.add(new Position(i + 1, j - 1));
                            }
                            if (board.isValidPosition(new Position(i + 1, j + 1))) {
                                piecePositions.add(new Position(i + 1, j + 1));
                            }
                        } else {
                            List<Move> pieceMoves = moveCanCheckPiece.getPossibleMoves(moveCanCheck, board);
                            for (Move pieceMove : pieceMoves) {
                                piecePositions.add(pieceMove.getDestination());
                            }
                        }
                        piecePositions.retainAll(kingPositions);
                        cannotEvadePositions.addAll(piecePositions);

                    }
                }
            }

            inCheckPositions.addAll(cannotEvadePositions);
            blockPositions.addAll(totalBlockPositions);
            givingCheckPositions.addAll(givingCheckTotalPositions);

        }
        return returnValue;
    }

    private int checkCheckmate(Colour colour) {
        if (colour == Colour.WHITE && inCheckLegalMoves.isEmpty()) {
            gameIsOver = Boolean.TRUE;
            return 2;
        }
        if (colour == Colour.BLACK && inCheckLegalMoves.isEmpty()) {
            gameIsOver = Boolean.TRUE;
            return 3;
        }
        return (colour == Colour.WHITE ? 5 : 4);
    }

    private boolean checkStalemate(Colour colour) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Position currentPiecePosition = new Position(i, j);
                Piece currentPiece = board.getPieceAt(currentPiecePosition);
                if (currentPiece != null && currentPiece.getColour() == colour) {
                    Move thisLastMove = board.getLastMove();
                    String lastSpecialMove = board.getLastSpecialMove();
                    Piece lastSourcePiece = board.getSourcePiece();
                    Piece lastDestinationPiece = board.getDestinationPiece();
                    List<Move> currentPieceMoves = currentPiece.getPossibleMoves(currentPiecePosition, board);

                    for (Move currentMove : currentPieceMoves) {
                        board.makeMove(currentMove);
                        if (!kingInCheck(colour)) {
                            board.undoMove();
                            board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                            clearInCheckVariables();
                            return Boolean.FALSE;
                        }
                        board.undoMove();
                    }

                }
            }
        }

        gameIsOver = Boolean.TRUE;
        return Boolean.TRUE;
    }

    private int makeInCheckMove(Move move, Colour colour) {

        Move uncheckedMove = move;
        Position sourcePosition = uncheckedMove.getSource();

        if (inCheckLegalMoves.contains(uncheckedMove)) {
            board.makeMove(uncheckedMove);
            clearInCheckVariables();
            whiteKingMoved = !whiteKingMoved ? sourcePosition.equals(new Position(7, 4)) : whiteKingMoved;
            blackKingMoved = !blackKingMoved ? sourcePosition.equals(new Position(0, 4)) : blackKingMoved;
            whiteKingsideRookMoved = !whiteKingsideRookMoved ? sourcePosition.equals(new Position(7, 7))
                    : whiteKingsideRookMoved;
            whiteQueensideRookMoved = !whiteQueensideRookMoved ? sourcePosition.equals(new Position(7, 0))
                    : whiteQueensideRookMoved;
            blackKingsideRookMoved = !blackKingsideRookMoved ? sourcePosition.equals(new Position(0, 7))
                    : blackKingsideRookMoved;
            blackQueensideRookMoved = !blackQueensideRookMoved ? sourcePosition.equals(new Position(0, 0))
                    : blackQueensideRookMoved;
            lastMove = move;
            return 1;
        } else {
            return (colour == Colour.WHITE ? -4 : -5);
        }

    }

    private void findInCheckLegalMove(Colour colour) {

        if (givingCheckPositions.size() == 1) {
            int positionIndex = 0;
            Position kingPosition = findKing(colour);
            Piece kingPiece = board.getPieceAt(kingPosition);

            while (positionIndex < blockPositions.size()) {
                for (int blockCheckIndex = 0; blockCheckIndex <= 63; blockCheckIndex++) {
                    Position currentPosition = new Position(Math.floorDiv(blockCheckIndex, 8), blockCheckIndex % 8);
                    Piece currentPiece = board.getPieceAt(currentPosition);
                    Move blockMove;
                    if (currentPiece != null && currentPiece.getColour() == colour
                            && currentPiece.getPossibleMoves(currentPosition, board).contains(
                                    blockMove = new Move(currentPosition, blockPositions.get(positionIndex)))) {
                        if (!(currentPiece instanceof King)) {
                            inCheckLegalMoves.add(blockMove);
                        }
                    }
                }
                positionIndex += 1;
            }
            List<Move> kingMoves = kingPiece.getPossibleMoves(kingPosition, board);
            for (Move kingMove : kingMoves) {
                if (givingCheckPositions.contains(kingMove.getDestination())) {
                    Move thisLastMove = board.getLastMove();
                    String lastSpecialMove = board.getLastSpecialMove();
                    Piece lastSourcePiece = board.getSourcePiece();
                    Piece lastDestinationPiece = board.getDestinationPiece();
                    board.makeMove(kingMove);
                    if (!kingInCheck(colour)) {
                        inCheckLegalMoves.add(kingMove);
                    }
                    board.undoMove();
                    board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                    lastMove = thisLastMove;
                } else if (!(inCheckPositions.contains(kingMove.getDestination()))) {
                    inCheckLegalMoves.add(kingMove);
                }
            }
            // throw new IllegalArgumentException(errors.toString());
        } else {
            Position kingPosition = findKing(colour);
            Piece kingPiece = board.getPieceAt(kingPosition);

            List<Move> kingMoves = kingPiece.getPossibleMoves(kingPosition, board);
            for (Move kingMove : kingMoves) {
                if (givingCheckPositions.contains(kingMove.getDestination())) {
                    Move thisLastMove = board.getLastMove();
                    String lastSpecialMove = board.getLastSpecialMove();
                    Piece lastSourcePiece = board.getSourcePiece();
                    Piece lastDestinationPiece = board.getDestinationPiece();
                    board.makeMove(kingMove);
                    if (!kingInCheck(colour)) {
                        inCheckLegalMoves.add(kingMove);
                    }
                    board.undoMove();
                    board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                    lastMove = thisLastMove;
                } else if (!(inCheckPositions.contains(kingMove.getDestination()))) {
                    inCheckLegalMoves.add(kingMove);
                }
            }
        }
    }

    private int makeMove(Move move, Colour colour) {
        Move uncheckedMove = move;
        Position sourcePosition = uncheckedMove.getSource();
        Piece source = board.getPieceAt(sourcePosition);
        if ((source.getColour() == colour)
                && (source.getPossibleMoves(uncheckedMove.getSource(), board).contains(uncheckedMove))) {
            // simulate move
            Move thisLastMove = board.getLastMove();
            String lastSpecialMove = board.getLastSpecialMove();
            Piece lastSourcePiece = board.getSourcePiece();
            Piece lastDestinationPiece = board.getDestinationPiece();
            board.makeMove(uncheckedMove);
            if (kingInCheck(colour)) {
                clearInCheckVariables();
                board.undoMove();
                board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                return -3;// invalid move
            } else {
                whiteKingMoved = !whiteKingMoved ? sourcePosition.equals(new Position(7, 4)) : whiteKingMoved;
                blackKingMoved = !blackKingMoved ? sourcePosition.equals(new Position(0, 4)) : blackKingMoved;
                whiteKingsideRookMoved = !whiteKingsideRookMoved ? sourcePosition.equals(new Position(7, 7))
                        : whiteKingsideRookMoved;
                whiteQueensideRookMoved = !whiteQueensideRookMoved ? sourcePosition.equals(new Position(7, 0))
                        : whiteQueensideRookMoved;
                blackKingsideRookMoved = !blackKingsideRookMoved ? sourcePosition.equals(new Position(0, 7))
                        : blackKingsideRookMoved;
                blackQueensideRookMoved = !blackQueensideRookMoved ? sourcePosition.equals(new Position(0, 0))
                        : blackQueensideRookMoved;
                lastMove = move;
                // valid move
                return 1;
            }
        }
        return source.getColour() == colour ? -3 : -2;
    }

    private int enPassant(Move move, Colour colour) {
        Position piecePosition = move.getSource();
        Piece piece = board.getPieceAt(piecePosition);
        if (lastMove == null) {
            return 0;
        }
        Piece lastMovePiece = board.getPieceAt(lastMove.getDestination());
        int returnValue = 0;
        if (piece instanceof Pawn && lastMovePiece instanceof Pawn) {
            if (colour != lastMovePiece.getColour() && colour == Colour.WHITE
                    && (lastMove.getSource().getRow() == 1 && lastMove.getDestination().getRow() == 3)
                    && ((lastMove.getDestination().getCol() - piecePosition.getCol() == 1)
                            || (lastMove.getDestination().getCol() - piecePosition.getCol() == -1))
                    && (lastMove.getDestination().getRow() == piecePosition.getRow())
                    && (move.getDestination().equals(new Position(lastMove.getDestination().getRow() - 1,
                            lastMove.getDestination().getCol())))
                    && (lastMove.getSource().equals(new Position(lastMove.getDestination().getRow() - 2,
                            lastMove.getDestination().getCol())))) {
                Move thisLastMove = board.getLastMove();
                String lastSpecialMove = board.getLastSpecialMove();
                Piece lastSourcePiece = board.getSourcePiece();
                Piece lastDestinationPiece = board.getDestinationPiece();
                board.enPassant(piecePosition, lastMove.getDestination());
                if (kingInCheck(colour)) {
                    clearInCheckVariables();
                    board.undoMove();
                    board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                    returnValue = -3;
                } else {
                    returnValue = 1;
                }
            }
            if (colour != lastMovePiece.getColour() && colour == Colour.BLACK
                    && (lastMove.getSource().getRow() == 6 && lastMove.getDestination().getRow() == 4)
                    && ((lastMove.getDestination().getCol() - piecePosition.getCol() == 1)
                            || (lastMove.getDestination().getCol() - piecePosition.getCol() == -1))
                    && (lastMove.getDestination().getRow() == piecePosition.getRow())
                    && (move.getDestination().equals(new Position(lastMove.getDestination().getRow() + 1,
                            lastMove.getDestination().getCol())))
                    && (lastMove.getSource().equals(new Position(lastMove.getDestination().getRow() + 2,
                            lastMove.getDestination().getCol())))) {
                Move thisLastMove = board.getLastMove();
                String lastSpecialMove = board.getLastSpecialMove();
                Piece lastSourcePiece = board.getSourcePiece();
                Piece lastDestinationPiece = board.getDestinationPiece();
                board.enPassant(piecePosition, lastMove.getDestination());
                if (kingInCheck(colour)) {
                    clearInCheckVariables();
                    board.undoMove();
                    board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                    returnValue = -3;
                } else {
                    returnValue = 1;
                }
            }
        }
        return returnValue;
    }

    private int castle(String from, String to, Colour colour) {
        if (!whiteKingMoved && currentPlayerIndex == 0 && from.equals("E8")) {
            if (!whiteKingsideRookMoved && to.equals("H8")) {
                Move thisLastMove = board.getLastMove();
                String lastSpecialMove = board.getLastSpecialMove();
                Piece lastSourcePiece = board.getSourcePiece();
                Piece lastDestinationPiece = board.getDestinationPiece();
                Boolean success = board.whiteKingsideCastle();
                if (success) {
                    if (kingInCheck(colour)) {
                        clearInCheckVariables();
                        board.undoMove();
                        board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                        return -3;
                    } else {
                        whiteKingMoved = Boolean.TRUE;
                        return 6;
                    }
                } else {
                    return 0;
                }
            }
            if (!whiteQueensideRookMoved && to.equals("A8")) {
                Move thisLastMove = board.getLastMove();
                String lastSpecialMove = board.getLastSpecialMove();
                Piece lastSourcePiece = board.getSourcePiece();
                Piece lastDestinationPiece = board.getDestinationPiece();
                Boolean success = board.whiteQueensideCastle();
                if (success) {
                    if (kingInCheck(colour)) {
                        clearInCheckVariables();
                        board.undoMove();
                        board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                        return -3;
                    } else {
                        whiteKingMoved = Boolean.TRUE;
                        return 6;
                    }
                } else {
                    return 0;
                }
            }
        }
        if (!blackKingMoved && currentPlayerIndex == 1 && from.equals("E1")) {
            if (!blackKingsideRookMoved && to.equals("H1")) {
                Move thisLastMove = board.getLastMove();
                String lastSpecialMove = board.getLastSpecialMove();
                Piece lastSourcePiece = board.getSourcePiece();
                Piece lastDestinationPiece = board.getDestinationPiece();
                Boolean success = board.blackKingsideCastle();
                if (success) {
                    if (kingInCheck(colour)) {
                        clearInCheckVariables();
                        board.undoMove();
                        board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                        return -3;
                    } else {
                        blackKingMoved = Boolean.TRUE;
                        return 7;
                    }
                } else {
                    return 0;
                }
            }
            if (!blackQueensideRookMoved && to.equals("A1")) {
                Move thisLastMove = board.getLastMove();
                String lastSpecialMove = board.getLastSpecialMove();
                Piece lastSourcePiece = board.getSourcePiece();
                Piece lastDestinationPiece = board.getDestinationPiece();
                Boolean success = board.blackQueensideCastle();
                if (success) {
                    if (kingInCheck(colour)) {
                        clearInCheckVariables();
                        board.undoMove();
                        board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                        return -3;
                    } else {
                        blackKingMoved = Boolean.TRUE;
                        return 7;
                    }
                } else {
                    return 0;
                }
            }
        }
        return 0;
    }

    public int pawnPromotion(Move move, Colour colour) {
        Piece pawnPromoting = board.getPieceAt(move.getSource());
        if (colour == Colour.WHITE && pawnPromoting instanceof Pawn && pawnPromoting.getColour() == Colour.WHITE
                && (move.getDestination().getRow() >= 2 && move.getDestination().getRow() <= 5)
                && pawnPromoting.getPossibleMoves(move.getSource(), board)
                        .contains(new Move(move.getSource(), new Position(0, move.getDestination().getCol())))) {
            Move promotionMove = new Move(move.getSource(), new Position(0, move.getDestination().getCol()));
            Move thisLastMove = board.getLastMove();
            String lastSpecialMove = board.getLastSpecialMove();
            Piece lastSourcePiece = board.getSourcePiece();
            Piece lastDestinationPiece = board.getDestinationPiece();
            switch (move.getDestination().getRow()) {
                case 2:
                    board.pawnPromotion(promotionMove, new Bishop(Colour.WHITE));
                    break;
                case 3:
                    board.pawnPromotion(promotionMove, new Knight(Colour.WHITE));
                    break;
                case 4:
                    board.pawnPromotion(promotionMove, new Rook(Colour.WHITE));
                    break;
                case 5:
                    board.pawnPromotion(promotionMove, new Queen(Colour.WHITE));
                    break;
            }
            if (kingInCheck(colour)) {
                clearInCheckVariables();
                board.undoMove();
                board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                return -3;
            } else {
                return 1;
            }
        } else if (colour == Colour.BLACK && pawnPromoting instanceof Pawn && pawnPromoting.getColour() == Colour.BLACK
                && (move.getDestination().getRow() >= 3 && move.getDestination().getRow() <= 6)
                && pawnPromoting.getPossibleMoves(move.getSource(), board)
                        .contains(new Move(move.getSource(), new Position(7, move.getDestination().getCol())))) {
            Move promotionMove = new Move(move.getSource(), new Position(7, move.getDestination().getCol()));
            Move thisLastMove = board.getLastMove();
            String lastSpecialMove = board.getLastSpecialMove();
            Piece lastSourcePiece = board.getSourcePiece();
            Piece lastDestinationPiece = board.getDestinationPiece();
            switch (move.getDestination().getRow()) {
                case 6:
                    board.pawnPromotion(promotionMove, new Bishop(Colour.BLACK));
                    break;
                case 5:
                    board.pawnPromotion(promotionMove, new Knight(Colour.BLACK));
                    break;
                case 4:
                    board.pawnPromotion(promotionMove, new Rook(Colour.BLACK));
                    break;
                case 3:
                    board.pawnPromotion(promotionMove, new Queen(Colour.BLACK));
                    break;
            }
            if (kingInCheck(colour)) {
                clearInCheckVariables();
                board.undoMove();
                board.replaceLastMove(thisLastMove, lastSpecialMove, lastSourcePiece, lastDestinationPiece);
                return -3;
            } else {
                return 1;
            }
        }
        return 0;
    }

    public void clearInCheckVariables() {
        inCheck = Boolean.FALSE;
        inCheckPositions.clear();
        blockPositions.clear();
        inCheckLegalMoves.clear();
        givingCheckPositions.clear();
        inCheckPawnPositions.clear();
    }

    public Position findKing(Colour colour) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece currentPiece = board.getPieceAt(new Position(i, j));
                if (currentPiece instanceof King && currentPiece.getColour() == colour) {
                    return new Position(i, j);
                }
            }
        }
        return null;
    }

    public Position findPosition(String input) {
        int col = input.charAt(0) - 'A';
        int row = input.charAt(1) - '1';
        return new Position(row, col);
    }

} // end ChessGame
