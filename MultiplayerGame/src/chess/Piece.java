package chess;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * This class acts as a chess piece,
 * with variables for color, x, y,
 * and the type of piece
 */
public class Piece {
	public boolean isWhite;
	public int x;
	public int y;
	public String type;
	// changes how piece is drawn
	public boolean dragging;
	public Image img;
	
	public Piece(boolean isWhite, String type, int x, int y) {
		this.isWhite = isWhite;
		this.type = type;
		this.x = x;
		this.y = y;
		setImage();
	}
	
	public String toString() {
		String[] rows = {"a", "b", "c", "d", "e", "f", "g", "h"};
		String str = "";
		if(isWhite) {
			str += "White ";
		}
		else {
			str += "Black ";
		}
		str += type + " on " + rows[x] + (8-y);
		return str;
	}
	
	public boolean equals(Object other) {
		if(!(other instanceof Piece)) {
			return false;
		}
		Piece otherPiece = (Piece)other;
		if(otherPiece.isWhite == this.isWhite && otherPiece.x == this.x && otherPiece.y == this.y && otherPiece.type.equals(this.type)) {
			return true;
		}
		return false;
	}
	
	public void setImage() {
		BufferedImage image;
		try {
			if(isWhite) {
				image = ImageIO.read(getClass().getResource("/images/white/"+type+".png"));
			}
			else {
				image = ImageIO.read(getClass().getResource("/images/black/"+type+".png"));
			}
			img = image.getScaledInstance(Board.SQUARE_WIDTH, Board.SQUARE_WIDTH, Image.SCALE_DEFAULT);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

}
