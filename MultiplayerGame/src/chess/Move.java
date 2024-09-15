package chess;
import java.io.*;

/**
 * This class acts as a chess move,
 * with a piece that is to be moved, and coordinates
 * for the x and y
 */
public class Move implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public boolean isWhite;
	public String type;
	public int startX;
	public int startY;
	public int moveX;
	public int moveY;
	public SpecialMove specialMove = SpecialMove.DEFAULT;
	public Piece captured;
	
	public enum SpecialMove {
		DEFAULT,
		PROMOTION,
		CASTLE_SHORT,
		CASTLE_LONG,
		EN_PASSANT,
		NO_CASTLE_SHORT,
		NO_CASTLE_LONG,
		NO_CASTLE_BOTH;
	}
	
	public Move(Piece toMove, int moveX, int moveY) {
		isWhite = toMove.isWhite;
		type = toMove.type;
		startX = toMove.x;
		startY = toMove.y;
		this.moveX = moveX;
		this.moveY = moveY;
	}
	
	public Move(boolean isWhite, String type, int startX, int startY, int moveX, int moveY) {
		this.isWhite = isWhite;
		this.type = type;
		this.startX = startX;
		this.startY = startY;
		this.moveX = moveX;
		this.moveY = moveY;
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof Move)) {
			return false;
		}
		Move otherMove = (Move) other;
		if(isWhite == otherMove.isWhite && startX == otherMove.startX && startY == otherMove.startY && type.equals(otherMove.type) && moveX == otherMove.moveX && moveY == otherMove.moveY) {
			return true;
		}
		return false;
	}
	
	public String toString() {
		String[] rows = {"a", "b", "c", "d", "e", "f", "g", "h"};
		return type + " on " + rows[startX] + (8-startY) + " to " + rows[moveX] + (8-moveY);
	}

}
