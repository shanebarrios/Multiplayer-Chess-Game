package chess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import chess.Move.SpecialMove;



public class Position {
	public Piece[][] board;
	ArrayList<Move> allLegalMoves;
	boolean whiteTurn;
	boolean whiteInCheck;
	boolean blackInCheck;
	boolean whiteShortCastlePrivilege;
	boolean whiteLongCastlePrivilege;
	boolean blackShortCastlePrivilege;
	boolean blackLongCastlePrivilege;
	GameState gameState;
	int numMoves = 0;
	Stack<Move> allMoves;
	Stack<Integer> halfmoveClock;
	//00000000-00001111
	//white short, white long, black short, black long
	Stack<Byte> castlingPrivilege;
	
	public enum GameState {
		CHECKMATE_WHITE,
		CHECKMATE_BLACK,
		DRAW_50_MOVE,
		STALEMATE,
		IN_PROGRESS,
		TIME_OUT_WHITE,
		TIME_OUT_BLACK,
		DISCONNECT_WHITE,
		DISCONNECT_BLACK,
		EMPTY_GAME;
	}
	
	public Position(String fen) {
		gameState = GameState.IN_PROGRESS;
		loadPositionFromFen(fen);
	}

	public Position() {
		gameState = GameState.EMPTY_GAME;
		board = new Piece[8][8];
		whiteShortCastlePrivilege = whiteLongCastlePrivilege = blackShortCastlePrivilege = blackLongCastlePrivilege = false;
		whiteInCheck = blackInCheck = false;
		allLegalMoves = new ArrayList<Move>();
		allMoves = new Stack<Move>();
	}
	
	public Piece getPiece(int x, int y) {
		return board[x][y];
	}
	
	/**
	 * Loads a position into board based on the inputted fen
	 */	
	public void loadPositionFromFen(String fen){
		board = new Piece[8][8];
		whiteTurn = true;
		whiteShortCastlePrivilege = whiteLongCastlePrivilege = blackShortCastlePrivilege = blackLongCastlePrivilege = false;
		halfmoveClock = new Stack<Integer>();
		castlingPrivilege = new Stack<Byte>();

		HashMap<Character, String> pieces = new HashMap<Character, String>();
		pieces.put('p', "Pawn");
		pieces.put('r', "Rook");
		pieces.put('b', "Bishop");
		pieces.put('n', "Knight");
		pieces.put('k', "King");
		pieces.put('q', "Queen");
		
		int x = 0;
		int y = 0;
		int i = 0;
		while(fen.charAt(i) != ' ') {
			char current = fen.charAt(i);
			
			if(Character.isDigit(current)) {
				int currentInt = Character.getNumericValue(current);
				x+=currentInt;
			}
			else if(current == '/') {
				x = 0;
				y++;
			}
			else {
				String type = pieces.get(Character.toLowerCase(current));
				boolean isWhite = Character.isUpperCase(current);
				addPiece(isWhite, type, x, y);
				x++;
			}
			i++;
		}
		i++;
		whiteTurn = fen.charAt(i) == 'w' ? true : false;
		i+=2;
		char[] rows = {'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
		while(!Character.isDigit(fen.charAt(i))) {
			char current = fen.charAt(i);
			if(current == ' ') {
				i++;
			}
			else if(current == '-') {
				i+=2;
			}
			else if(current == 'K') {
				whiteShortCastlePrivilege = true;
				i++;
			}
			else if(current == 'Q') {
				whiteLongCastlePrivilege = true;
				i++;
			}
			else if(current == 'k') {
				blackShortCastlePrivilege = true;
				i++;
			}
			else if(current == 'q') {
				blackLongCastlePrivilege = true;
				i+=2;
			}
			else {
				int moveX = 0;
				for(int k = 0; k < rows.length; k++) {
					if (rows[k] == current) {
						moveX = k;
					}
				}
				i++;
				int moveY = 8-Character.getNumericValue(fen.charAt(i));
				int startY;
				if(whiteTurn) {
					moveY++;
					startY = moveY-2;
				}
				else {
					moveY--;
					startY = moveY+2;
				}
				allMoves.push(new Move(new Piece(!whiteTurn, "Pawn", moveX, startY), moveX, moveY));
				i+=2;
			}
		}
		int start = i;
		while(Character.isDigit(fen.charAt(i))) {
			i++;
		}
		halfmoveClock.push(Integer.parseInt(fen.substring(start, i)));
		i++;
		numMoves = Integer.parseInt(fen.substring(i, fen.length()));
		allMoves = new Stack<Move>();
		updateLegalMoves();
		updateGameState();
	}
	
	/**
	 * Returns a FEN representation of the position
	 */
	private String getPositionAsFen() {
		String fen = "";
		for(int y = 0; y < 8; y++) {
			int blankCount = 0;
			for(int x = 0; x < 8; x++) {
				if(board[x][y] == null) {
					blankCount++;
				}
				else {
					if(blankCount > 0) {
						
					}
					blankCount = 0;
					String charType = "";
					if(board[x][y].type.equals("Knight")) {
						charType = "K";
					}
					else {
						board[x][y].type.substring(0, 1).toLowerCase();
					}
				}
			}
		}
		return "";
	}
	
	private void addPiece(boolean isWhite, String type, int x, int y) {
		board[x][y] = new Piece(isWhite, type, x, y);
	}
	
	/**
	 * Checks if pieces on two squares belong to the same color
	 */
	private boolean isSameColor(int x1, int y1, int x2, int y2) {
		if(board[x1][y1] == null || board[x2][y2] == null) {
			return false;
		}
		if(board[x1][y1].isWhite == board[x2][y2].isWhite) {
			return true;
		}
		return false;
	}
	
	
	/**
	 * Checks if x and y are within the bounds of the board
	 */
	public static boolean isInBounds(int x, int y) {
		if(x >= 0 && x < 8 && y >= 0 && y < 8) {
			return true;
		}
		return false;
	}
	
	/**
	 * Adds the move to the list of allLegalMoves if 
	 * it is valid, and in addition updates the values of blackInCheck
	 * or whiteInCheck if they are in check 
	 */
	private boolean addMoveIfValid(Piece toMove, int moveX, int moveY) {
		if(isInBounds(moveX, moveY) && !isSameColor(toMove.x, toMove.y, moveX, moveY)) {
			if(board[moveX][moveY] != null && board[moveX][moveY].type.equals("King")) {
				if(toMove.isWhite) {
					blackInCheck = true;
				}
				else {
					whiteInCheck = true;
				}
			}
			if(whiteTurn == toMove.isWhite) {
				allLegalMoves.add(new Move(toMove, moveX, moveY));
			}
			return true;
		}
		return false;
	}
	/**
	 * Updates allLegalMoves to include all legal moves
	 * with the addition of moves that place the mover in check
	 */
	private void updateLegalMovesNoChecks() {
		allLegalMoves = new ArrayList<Move>();
		whiteInCheck = false;
		blackInCheck = false;
		for(int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				Piece current = board[x][y];
				if(current == null) {
					continue;
				}
				if(current.type.equals("Pawn")) {
					if(current.isWhite && y-1 >= 0) {
						//forward 1 movement
						if(board[x][y-1] == null) {
							addMoveIfValid(current, x, y-1);
						}
						//capturing to the left
						if(x-1 >= 0 && board[x-1][y-1] != null) {
							addMoveIfValid(current, x-1, y-1);
						}
						//capturing to the right
						if(x+1 < 8 && board[x+1][y-1] != null) {
							addMoveIfValid(current, x+1, y-1);
						}
						//forward 2 on first move
						if(y == 6 && board[x][y-1] == null && board[x][y-2] == null) {
							addMoveIfValid(current, x, y-2);
						}
						//en passant
						Move lastMove = null;
						if(!allMoves.isEmpty()) {
							lastMove = allMoves.peek();
						}
						if(lastMove != null && lastMove.type.equals("Pawn") && lastMove.moveY - lastMove.startY == 2 && y == lastMove.moveY) {
							if(lastMove.moveX == x+1) {
								addMoveIfValid(current, x+1, y-1);
							}
							if(lastMove.moveX == x-1) {
								addMoveIfValid(current, x-1, y-1);
							}
						}
					}
					//same rules as white, but opposite direction
					if(!current.isWhite && y+1 < 8) {
						if(board[x][y+1] == null) {
							addMoveIfValid(current, x, y+1);
						}
						if(x-1 >= 0 && board[x-1][y+1] != null) {
							addMoveIfValid(current, x-1, y+1);
						}
						if(x+1 < 8 && board[x+1][y+1] != null) {
							addMoveIfValid(current, x+1, y+1);
						}
						if(y == 1 && board[x][y+1] == null && board[x][y+2] == null) {
							addMoveIfValid(current, x, y+2);
						}
						Move lastMove = null;
						if(!allMoves.isEmpty()) {
							lastMove = allMoves.peek();
						}
						if(lastMove != null && lastMove.type.equals("Pawn") && lastMove.startY - lastMove.moveY == 2 && y == lastMove.moveY) {
							if(lastMove.moveX == x+1) {
								addMoveIfValid(current, x+1, y+1);
							}
							if(lastMove.moveX == x-1) {
								addMoveIfValid(current, x-1, y+1);
							}
						}
					}
				}
				else if(current.type.equals("Knight")) {
					//all 2 and 1 movements
					int[] possibleX = {x+2, x+1, x+2, x+1, x-2, x-1,x-1, x-2};
					int[] possibleY = {y+1, y+2, y-1, y-2, y+1, y+2, y-2, y-1};
					for(int i = 0; i < 8; i++) {
						addMoveIfValid(current, possibleX[i], possibleY[i]);
					}
				}
				else if(current.type.equals("Rook")) {
					int possibleX = x+1;
					//vertical and horizontal in all directions until it is blocked
					while(addMoveIfValid(current, possibleX, y) && board[possibleX][y] == null) {
						possibleX++;
						continue;
					} 
					
					possibleX = x-1;
					while(addMoveIfValid(current, possibleX, y) && board[possibleX][y] == null) {
						possibleX--;
						continue;
					} 
					
					int possibleY = y+1;
					while(addMoveIfValid(current, x, possibleY) && board[x][possibleY] == null) {
						possibleY++;
						continue;
					} 
					
					possibleY = y-1;
					while(addMoveIfValid(current, x, possibleY) && board[x][possibleY] == null) {
						possibleY--;
						continue;
					} 
				}
				else if(current.type.equals("Bishop")) {
					int possibleX = x+1;
					int possibleY = y+1;
					//diagonal in all directions until it is blocked
					while(addMoveIfValid(current, possibleX, possibleY) && board[possibleX][possibleY] == null) {
						possibleX++;
						possibleY++;
						continue;
					} 
					
					possibleX = x+1;
					possibleY = y-1;
					while(addMoveIfValid(current, possibleX, possibleY) && board[possibleX][possibleY] == null) {
						possibleX++;
						possibleY--;
						continue;
					} 
					
					possibleX = x-1;
					possibleY = y+1;
					while(addMoveIfValid(current, possibleX, possibleY) && board[possibleX][possibleY] == null) {
						possibleX--;
						possibleY++;
						continue;
					} 
					
					possibleX = x-1;
					possibleY = y-1;
					while(addMoveIfValid(current, possibleX, possibleY) && board[possibleX][possibleY] == null) {
						possibleX--;
						possibleY--;
						continue;
					} 
				}
				else if(current.type.equals("Queen")) {
					//combined movement of rook and bishop
					int possibleX = x+1;
					while(addMoveIfValid(current, possibleX, y) && board[possibleX][y] == null) {
						possibleX++;
						continue;
					} 
					
					possibleX = x-1;
					while(addMoveIfValid(current, possibleX, y) && board[possibleX][y] == null) {
						possibleX--;
						continue;
					} 
					
					int possibleY = y+1;
					while(addMoveIfValid(current, x, possibleY) && board[x][possibleY] == null) {
						possibleY++;
						continue;
					} 
					
					possibleY = y-1;
					while(addMoveIfValid(current, x, possibleY) && board[x][possibleY] == null) {
						possibleY--;
						continue;
					} 
					
					possibleX = x+1;
					possibleY = y+1;
					while(addMoveIfValid(current, possibleX, possibleY) && board[possibleX][possibleY] == null) {
						possibleX++;
						possibleY++;
						continue;
					} 
					
					possibleX = x+1;
					possibleY = y-1;
					while(addMoveIfValid(current, possibleX, possibleY) && board[possibleX][possibleY] == null) {
						possibleX++;
						possibleY--;
						continue;
					} 
					
					possibleX = x-1;
					possibleY = y+1;
					while(addMoveIfValid(current, possibleX, possibleY) && board[possibleX][possibleY] == null) {
						possibleX--;
						possibleY++;
						continue;
					} 
					
					possibleX = x-1;
					possibleY = y-1;
					while(addMoveIfValid(current, possibleX, possibleY) && board[possibleX][possibleY] == null) {
						possibleX--;
						possibleY--;
						continue;
					} 
				}
				else if(current.type.equals("King")) {
					//1 in each direction
					int[] possibleX = {x, x, x+1, x+1, x+1, x-1, x-1, x-1};
					int[] possibleY = {y+1, y-1, y, y+1, y-1, y, y+1, y-1};
					for(int i = 0; i < 8; i++) {
						addMoveIfValid(current, possibleX[i], possibleY[i]);
					}
					//castling short
					if(whiteShortCastlePrivilege && current.isWhite || (blackShortCastlePrivilege && !current.isWhite)) {
						if(board[x+1][y] == null && board[x+2][y] == null) {
							addMoveIfValid(current, x+2, y);
						}
					}
					//castling long
					if(whiteLongCastlePrivilege && current.isWhite || (blackLongCastlePrivilege && !current.isWhite)) {
						if(board[x-1][y] == null && board[x-2][y] == null && board[x-3][y] == null) {
							addMoveIfValid(current, x-2, y);
						}
					}
				}
			}
		}
	}
	
	/**
	 * Updates the game state, such that 0 is the base state,
	 * 1 is stalemate, 2 is checkmate for white, and 3 is checkmate for
	 * black
	 */
	private void updateGameState() {
		if(gameState == GameState.TIME_OUT_WHITE || gameState == GameState.TIME_OUT_BLACK) {
			allLegalMoves = new ArrayList<Move>();
		}
		else if(allLegalMoves.size() == 0) {
			if(blackInCheck && !whiteTurn) {
				gameState = GameState.CHECKMATE_BLACK;
			}
			else if(whiteInCheck && whiteTurn) {
				gameState = GameState.CHECKMATE_WHITE;
			}
			else {
				gameState = GameState.STALEMATE;
			}
		}
		else if(halfmoveClock.peek() >= 50) {
			gameState = GameState.DRAW_50_MOVE;
			allLegalMoves = new ArrayList<Move>();
		}
		else {
			gameState = GameState.IN_PROGRESS;
		}
	}
	
	/**
	 * Updates allLegalMoves by removing any moves
	 * that would put the mover in check
	 */
	public void updateLegalMoves() {
		updateLegalMovesNoChecks();
		ArrayList<Move> newMoves = new ArrayList<Move>();
		for(Move m : allLegalMoves) {
			if(!putsInCheck(m)) {
				//can't castle while in check
				if (m.type.equals("King") && Math.abs(m.moveX - m.startX) == 2) {
					if(((whiteInCheck && m.isWhite) || (blackInCheck && !m.isWhite))) {
						continue;
					}
					if(m.moveX - m.startX == 2 && putsInCheck(new Move(m.isWhite, m.type, m.startX, m.startY, m.moveX-1, m.moveY))) {
						continue;
					}
					if(m.moveX - m.startX == -2 && putsInCheck(new Move(m.isWhite, m.type, m.startX, m.startY, m.moveX+1, m.moveY))) {
						continue;
					}
				}
				newMoves.add(m);
			}
		}
		allLegalMoves = newMoves;
		updateGameState();
	}
	
	/**
	 * Checks whether a move would put oneself in check (not allowed)
	 */
	private boolean putsInCheck(Move move) {
		boolean bool;
		//stores the original information so that the move can be unchanged at the end
		Piece tempPiece = board[move.moveX][move.moveY];
		Piece toMove = board[move.startX][move.startY];
		board[move.startX][move.startY] = null;
		toMove.x = move.moveX;
		toMove.y = move.moveY;
		board[move.moveX][move.moveY] = toMove;
		updateLegalMovesNoChecks();
		if(whiteInCheck && whiteTurn || blackInCheck && !whiteTurn) {
			bool = true;
		}
		else {
			bool = false;
		}
		//undoes the movement on the board
		board[move.moveX][move.moveY] = tempPiece;
		board[move.startX][move.startY] = toMove;
		toMove.x = move.startX;
		toMove.y = move.startY;
		updateLegalMovesNoChecks();
		return bool;
	}
	
	/**
	 * Does the move on the board, updates castling privileges 
	 * accordingly, and plays a sound for the move
	 */
	public boolean doMove(Move move){
		boolean capture = false;
		//castlingPrivilege.push(castlingPrivilege.peek());
		if(board[move.moveX][move.moveY] !=  null) {
			capture = true;
			move.captured = board[move.moveX][move.moveY];
		}
		if(move.type.equals("King")) {
			//castling short
			if (move.moveX - move.startX == 2) {
				Piece temp = board[move.startX + 3][move.startY];
				board[move.startX +3][move.startY] = null;
				board[move.startX+1][move.startY] = temp;
				temp.x = move.startX+1;
				move.specialMove = SpecialMove.CASTLE_SHORT;
			}
			//castling long
			else if(move.startX - move.moveX == 2) {
				Piece temp = board[move.startX - 4][move.startY];
				board[move.startX - 4][move.startY] = null;
				board[move.startX - 1][move.startY] = temp;
				temp.x = move.startX-1;
				move.specialMove = SpecialMove.CASTLE_LONG;
			}
			
			if(whiteShortCastlePrivilege && whiteLongCastlePrivilege) {
				move.specialMove = SpecialMove.NO_CASTLE_BOTH;
			}
			else if(whiteShortCastlePrivilege) {
				move.specialMove = SpecialMove.NO_CASTLE_SHORT;
			}
			else if(whiteLongCastlePrivilege) {
				move.specialMove = SpecialMove.NO_CASTLE_LONG;
			}
			
			if(move.isWhite) {
				whiteShortCastlePrivilege = false;
				whiteLongCastlePrivilege = false;
			}
			else {
				blackShortCastlePrivilege = false;
				blackLongCastlePrivilege = false;
			}
		}
		if(move.type.equals("Rook")) {
			if(move.startX == 0) {
				if(move.isWhite && whiteLongCastlePrivilege) {
					move.specialMove = SpecialMove.NO_CASTLE_LONG;
					whiteLongCastlePrivilege = false;
				}
				if(!move.isWhite && blackLongCastlePrivilege) {
					move.specialMove = SpecialMove.NO_CASTLE_LONG;
					blackLongCastlePrivilege = false;
				}
			}
			if(move.startX == 7) {
				if(move.isWhite && whiteShortCastlePrivilege) {
					move.specialMove = SpecialMove.NO_CASTLE_SHORT;
					whiteShortCastlePrivilege = false;
				}
				if(!move.isWhite && blackShortCastlePrivilege) {
					move.specialMove = SpecialMove.NO_CASTLE_SHORT;
					blackShortCastlePrivilege = false;
				}
			}
		}
		if(move.type.equals("Pawn")) {
			//auto promotion to queen
			if( (move.moveY == 0 && move.isWhite) || (move.moveY == 7 && !move.isWhite) ) {
				board[move.startX][move.startY].type = "Queen";
				board[move.startX][move.startY].setImage();
				move.specialMove = SpecialMove.PROMOTION;
			}
			//en passant
			if(Math.abs(move.moveX - move.startX) == 1 && board[move.moveX][move.moveY] == null) {
				if(move.isWhite) {
					move.captured = board[move.moveX][move.moveY+1];
					board[move.moveX][move.moveY+1] = null;
				}
				else {
					move.captured = board[move.moveX][move.moveY-1];
					board[move.moveX][move.moveY-1] = null;
				}
				capture = true;
				move.specialMove = SpecialMove.EN_PASSANT;
			}
			
		}
		
		if(move.type.equals("Pawn") || capture) {
			halfmoveClock.push(0);
		}
		else {
			halfmoveClock.push(halfmoveClock.peek()+1);
		}
		
		numMoves++;
		Piece toMove = board[move.startX][move.startY];
		board[move.startX][move.startY] = null;
		toMove.x = move.moveX;
		toMove.y = move.moveY;
		board[move.moveX][move.moveY] = toMove;
		
		whiteTurn = !whiteTurn;
		allMoves.push(move);
		updateLegalMoves();
		
		return capture;
	}
}
