package chess;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.Cursor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;

import chess.Position.GameState;

/**
 * Game works by storing a 2 dimensional array
 * of type Piece and using various methods
 * to determine if any Move is legal given the Game
 * state
 */
public class Board extends JPanel implements MouseListener, MouseMotionListener{
	
	Piece pressedPiece;
	Player player;
	int dragX;
	int dragY;
	final static int BOARD_WIDTH = 680;
	final static int SQUARE_WIDTH = BOARD_WIDTH/8;
	final static int BOARD_OFFSET_X = 20;
	final static int BOARD_OFFSET_Y = 80;
	
	Position position;
	
	public Board(Player player) {
		this.player = player;
		setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_WIDTH));
        setMaximumSize(new Dimension(BOARD_WIDTH, BOARD_WIDTH));
        setMinimumSize(this.getPreferredSize());
        setSize(new Dimension(BOARD_WIDTH, BOARD_WIDTH));
		initGame();
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public void initGame() {
		position = new Position();
		repaint();
	}	
	
	public void loadPositionFromFen(String fen) {
		position.loadPositionFromFen(fen);
		repaint();
	}
	
	public void drawLegalMoves(Graphics g, Piece p) {
		g.setColor(new Color(65, 65, 65, 150));
		for(Move m : position.allLegalMoves) {
			if(m.startX == p.x && m.startY == p.y) {
				int x = m.moveX;
				int y = m.moveY;
				//not a capture
				if(position.getPiece(x, y) == null) {
					if(!player.isWhite) {
						x = 7-x;
						y = 7-y;
					}
					//centers the circle on the square
					double r = SQUARE_WIDTH/3;
					x = (int)((double)(x*SQUARE_WIDTH + (x+1)*SQUARE_WIDTH)/2 - r/2);
					y = (int)((double)(y*SQUARE_WIDTH + (y+1)*SQUARE_WIDTH)/2 - r/2);
					g.fillOval(x, y, (int)r, (int)r);
				}
				//capture
				else {
					if(!player.isWhite) {
						x = 7-x;
						y = 7-y;
					}
					Graphics2D g2 = (Graphics2D) g;
					g2.setStroke(new BasicStroke(8));
					g.drawOval(x*SQUARE_WIDTH+4, y*SQUARE_WIDTH+4, SQUARE_WIDTH-8, SQUARE_WIDTH-8);
				}
			}
		}
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		boolean whiteSquare = true;
		for(int x = 0; x < 8; x++) {
			for(int y = 0; y < 8; y++) {
				//alternates each square to make the checker pattern
				int currentX = x;
				int currentY = y;
				//black's view is flipped
				if(!player.isWhite) {
					currentX = 7-x;
					currentY = 7-y;
				}
				Piece current = position.getPiece(currentX, currentY);
				Move lastMove = null; 
				if(!position.allMoves.isEmpty()) {
					lastMove = position.allMoves.peek();
				}
				if(whiteSquare) {
					if(lastMove != null && ((lastMove.startX == currentX && lastMove.startY == currentY) || (lastMove.moveX == currentX && lastMove.moveY == currentY)) || (pressedPiece != null && pressedPiece == current)) {
						g.setColor(new Color(205, 214, 107));
					}
					else {
						g.setColor(new Color(222, 202, 189));
					}
				}
				else {
					//colors the square differently if the last move was there
					if(lastMove != null && ((lastMove.startX == currentX && lastMove.startY == currentY) || (lastMove.moveX == currentX && lastMove.moveY == currentY)) || (pressedPiece != null && pressedPiece == current)) {
						g.setColor(new Color(156, 152, 47));
					}
					else {
						g.setColor(new Color(133, 98, 76));
					}
				}
				g.fillRect(x*SQUARE_WIDTH, y*SQUARE_WIDTH, SQUARE_WIDTH, SQUARE_WIDTH);
				whiteSquare = !whiteSquare;
				
				if(current != null && current.type.equals("King") && ((position.whiteInCheck && current.isWhite) || (position.blackInCheck && !current.isWhite))) {
					try {
						BufferedImage img = ImageIO.read(getClass().getResource("/images/white/check-circle.png"));
						Image img2 = img.getScaledInstance(SQUARE_WIDTH, SQUARE_WIDTH, Image.SCALE_DEFAULT);
						g.drawImage(img2, x*SQUARE_WIDTH, y*SQUARE_WIDTH, this);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//draws each piece
				if(current != null && !current.dragging) {
					g.drawImage(current.img, x*SQUARE_WIDTH, y*SQUARE_WIDTH, null);
				}
			}
			whiteSquare = !whiteSquare;
		}
		g.setColor(Color.black);
		//g.drawRect(0, 0, BOARD_WIDTH, BOARD_WIDTH);
		if(pressedPiece != null) {
			drawLegalMoves(g, pressedPiece);
			if(pressedPiece.dragging) {
				g.drawImage(pressedPiece.img, dragX, dragY, null);
			}
		}
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		if(SwingUtilities.isRightMouseButton(e)) {
			if(pressedPiece != null) {
				pressedPiece.dragging = false;
				pressedPiece = null;
			}
			repaint();
			return;
		}
		int x = e.getX()/SQUARE_WIDTH;
		int y = e.getY()/SQUARE_WIDTH;
		setCursor(new Cursor(Cursor.HAND_CURSOR));
		if(!player.isWhite) {
			y = 7-y;
			x = 7-x;
		}
		if(pressedPiece != null && pressedPiece.isWhite == player.isWhite) {
			if (x != pressedPiece.x || y != pressedPiece.y) {
				Move possibleMove = new Move(pressedPiece, x, y);
				player.sendMoveIfLegal(possibleMove);
			}
			pressedPiece = null;
		}
		//sets a new pressedPiece if able
		if(Position.isInBounds(x, y) && position.getPiece(x,y) != null && position.getPiece(x, y).isWhite == player.isWhite) {
			pressedPiece = position.getPiece(x, y);
			dragX = e.getX()-(int)((double)SQUARE_WIDTH*0.5);
			dragY = e.getY()-(int)((double)SQUARE_WIDTH*0.5);
			pressedPiece.dragging = true;
			repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		if(SwingUtilities.isRightMouseButton(e)) {
			return;
		}
		int x = e.getX()/SQUARE_WIDTH;
		int y = e.getY()/SQUARE_WIDTH;
		setCursor(Cursor.getDefaultCursor());
		if(!player.isWhite) {
			y = 7-y;
			x = 7-x;
		}
		if(pressedPiece != null) {
			pressedPiece.dragging = false;
		}
		if(pressedPiece != null && (x != pressedPiece.x || y != pressedPiece.y) && pressedPiece.isWhite == player.isWhite) {
			Move possibleMove = new Move(pressedPiece, x, y);
			player.sendMoveIfLegal(possibleMove);
			pressedPiece = null;
		}
		repaint();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		if(SwingUtilities.isRightMouseButton(e)) {
			return;
		}
		if(pressedPiece != null && pressedPiece.isWhite == player.isWhite) {
			dragX = e.getX()-(int)((double)SQUARE_WIDTH*0.5);
			dragY = e.getY()-(int)((double)SQUARE_WIDTH*0.5);
			pressedPiece.dragging = true;
			setCursor(new Cursor(Cursor.HAND_CURSOR));
			repaint();
		}
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
}
