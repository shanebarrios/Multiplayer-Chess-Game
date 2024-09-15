package chess;

import java.net.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.*;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.Timer;
import chess.Position.GameState;



/**
 * This is the client side of the Chess game
 * It handles both the GUI and the connection
 * to the server
 */
public class Player {
	boolean isWhite;
	Board board;
	ObjectOutputStream oos;
	ObjectInputStream ois;
	GameFrame frame;
	long timeControl;
	String opponentName;
	String username;
	
	public Player(long timeControl, String username, String serverIP) {
		this.timeControl = timeControl;
		this.username = username;
		board = new Board(this);
		frame = new GameFrame(board);
		connectToServer(serverIP);
	}
	
	/**
	 * Establishes connection with the server
	 * Known issue: Does not inform the user if their connection fails
	 * and does not ask to retry
	 */
	private void connectToServer(String serverIP) {
		Socket socket;
		try {
			InetAddress host = InetAddress.getByName(serverIP);
            socket = new Socket(host, 9876);
            oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(username);
    		oos.writeObject(timeControl);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
		Read r = new Read(socket);
		r.start();
	}
	
	
	/**
	 * Sends the move to the server
	 * and updates the game with the move
	 * if legal
	 */
	public void sendMoveIfLegal(Move move) {
		for(Move m : board.position.allLegalMoves) {
			if(move.equals(m)) {
				try {
					oos.writeObject(move);
					//game.doMove(move);
					//board.repaint();
				}
				catch (Exception exception) {
					System.out.println("Failed to send move to the server");
				}
			}
		}
	}
	
	/**
	 * Plays a sound when move is performed, different
	 * if move was a capture
	 */
	private void playMoveSound(boolean capture) {
		String sound;
		if(capture) {
			sound = "capture";
		}
		else {
			sound = "move-sound";
		}
		try {
			AudioInputStream ais = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/"+sound+".wav"));
			Clip clip = AudioSystem.getClip();
			clip.open(ais);
			clip.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void sendMessage(String message) throws IOException {
		oos.writeObject(message);
	}

	/**
	 * Defines a thread that continuously reads the ObjectInputStream
	 * for moves from the server, and then
	 * updates the game
	 */
	private class Read extends Thread {
        Socket socket;
        Read(Socket socket) {
            this.socket = socket;
        }
        public void run() {
            try {
            	ois = new ObjectInputStream(socket.getInputStream());
            	//sets up the client side position and UI
            	ois.readObject();
                isWhite = (boolean) ois.readObject();
                String fen = (String) ois.readObject();
                opponentName = (String) ois.readObject();
                board.loadPositionFromFen(fen);
                frame.initTimers(timeControl, isWhite);
                frame.initUsernameLabels(username, opponentName);
                frame.updateStatus();
                //constantly reads from the server
                while (board.position.gameState == GameState.IN_PROGRESS) {
                	Object o = ois.readObject();
                	if(o instanceof long[]) {
                		updateTimes((long[])o);
                		continue;
                	}
                	if(o instanceof String) {
                		frame.addChatMessage((String)o);
                		continue;
                	}
                	Move m = (Move)o;
                	boolean capture = board.position.doMove(m);
                	playMoveSound(capture);
                	board.repaint();
                }
                Thread.sleep(500);
                String sound;
                
                GameState finalState = board.position.gameState;
                // some funny sounds for winning or losing
                
                /*if(finalState == GameState.CHECKMATE_BLACK && isWhite || finalState == GameState.CHECKMATE_WHITE && !isWhite || finalState == GameState.TIME_OUT_BLACK && isWhite || finalState == GameState.TIME_OUT_WHITE && !isWhite) {
            		sound = "winning";
            	}
                if(finalState == GameState.CHECKMATE_BLACK || finalState == GameState.TIME_OUT_BLACK || finalState == GameState.DISCONNECT_BLACK) {
                	if(isWhite) {
                		sound = "winning";
                	}
                	else {
                		sound = "losing";
                	}
                }
                else if(finalState == GameState.CHECKMATE_WHITE || finalState == GameState.TIME_OUT_WHITE || finalState == GameState.DISCONNECT_WHITE) {
                	if(isWhite) {
                		sound = "losing";
                	}
                	else {
                		sound = "winning";
                	}
                }
                else {
                	sound = "stalemate";
                }
               
                
                AudioInputStream ais = AudioSystem.getAudioInputStream(getClass().getResource("/sounds/"+sound+".wav"));
    			Clip clip = AudioSystem.getClip();
    			clip.open(ais);
    			clip.start();*/
                frame.updateStatus();
            } catch (Exception e) {
            	
            }
        }
        
        /**
    	 * Updates the times for both players
    	 */
        private void updateTimes(long[] times) {
        	long playerTime;
    		long opponentTime;
    		if(isWhite) {
    			playerTime = times[0];
    			opponentTime = times[1];
    		}
    		else {
    			playerTime = times[1];
    			opponentTime = times[0];
    		}
    		frame.timerPlayer.updateTime(playerTime);
    		frame.timerOpponent.updateTime(opponentTime);
    		if(playerTime <= 0) {
    			if(isWhite) {
    				board.position.gameState = GameState.TIME_OUT_WHITE;
    			}
    			else {
    				board.position.gameState = GameState.TIME_OUT_BLACK;
    			}
    		}
    		if(opponentTime <= 0) {
    			if(isWhite) {
    				board.position.gameState = GameState.TIME_OUT_BLACK;
    			}
    			else {
    				board.position.gameState = GameState.TIME_OUT_WHITE;
    			}
    		}
        }
    }
	
}
