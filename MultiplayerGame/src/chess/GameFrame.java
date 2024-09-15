package chess;

import java.awt.Color;
import java.awt.Font;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

import chess.Position.GameState;

public class GameFrame extends JFrame  {
	Board board;
	Clock timerPlayer;
	Clock timerOpponent;
	boolean playerWhite;
	Timer t;
	JTextArea chatArea;
	JTextField chatInput;
	JButton enterButton;
	JLabel statusLabel;
	
	public GameFrame(Board board) {
		this.board = board;
		setTitle("Chess");
		setLayout(null);
		board.setLocation(Board.BOARD_OFFSET_X,Board.BOARD_OFFSET_Y);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		add(board);
		getContentPane().setBackground(new Color(50, 50, 50));
		int frameWidth = Board.BOARD_WIDTH + Board.BOARD_OFFSET_X*2+415;
		int frameHeight = Board.BOARD_WIDTH+ + Board.BOARD_OFFSET_Y*2+40;
		initChatArea();
		setSize(frameWidth, frameHeight);
		initStatusLabel();
		setVisible(true);
	}
	
	/**
	 * Initiates the chat area, allowing players to converse with another during the game
	 * Also handles logic for sending messages
	 */
	private void initChatArea() {
		chatArea = new JTextArea();
		chatArea.setFont(new Font("Serif", Font.PLAIN, 15));
		chatInput = new JTextField();
		JScrollPane scrollArea = new JScrollPane(chatArea);
		chatArea.setLineWrap(true);
		chatArea.setWrapStyleWord(false);
		DefaultCaret caret = (DefaultCaret)chatArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		enterButton = new JButton("Send");
		chatInput.setSize(250, Board.BOARD_WIDTH/20);
		chatInput.setLocation(Board.BOARD_WIDTH+Board.BOARD_OFFSET_X+50, Board.BOARD_WIDTH+Board.BOARD_OFFSET_Y-(chatInput.getHeight()));
		scrollArea.setSize(320, Board.BOARD_WIDTH/2);
		scrollArea.setLocation(Board.BOARD_WIDTH+Board.BOARD_OFFSET_X+50, chatInput.getY() - scrollArea.getHeight());
		enterButton.setSize(320-chatInput.getWidth(), chatInput.getHeight());
		enterButton.setLocation(chatInput.getX()+chatInput.getWidth(), chatInput.getY());
		enterButton.setBackground(Color.GREEN);
		
		ActionListener chatListener = new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(chatInput.getText() != " ") {
					try {
						board.player.sendMessage(chatInput.getText());
						chatInput.setText("");
					}
					catch(Exception exception) {
						exception.printStackTrace();
					}
					
				}
			}
		};
		
		enterButton.addActionListener(chatListener);
		chatInput.addActionListener(chatListener);
		
		add(scrollArea);
		add(chatInput);
		add(enterButton);
	}
	
	public void addChatMessage(String message) {
		chatArea.append(message+"\n");
	}
	
	/**
	 * Sets up the UI for the timers
	 */
	public void initTimers(long timeControl, boolean playerWhite) {
		this.playerWhite = playerWhite;
		timerPlayer = new Clock(timeControl);
		timerOpponent = new Clock(timeControl);
		timerPlayer.setLocation(Board.BOARD_WIDTH+Board.BOARD_OFFSET_X*2-timerPlayer.getWidth()-20, Board.BOARD_OFFSET_Y+Board.BOARD_WIDTH+timerPlayer.getHeight()/2);
		timerOpponent.setLocation(Board.BOARD_WIDTH+Board.BOARD_OFFSET_X*2-timerPlayer.getWidth()-20, Board.BOARD_OFFSET_Y-(timerPlayer.getHeight()*3/2));
		add(timerPlayer);
		add(timerOpponent);
		repaint();
	}
	
	/**
	 * Sets up the UI for the two usernames
	 */
	public void initUsernameLabels(String playerUsername, String opponentUsername) {
		JLabel playerLabel = new JLabel(playerUsername);
		JLabel opponentLabel = new JLabel(opponentUsername);
		playerLabel.setSize(Board.BOARD_WIDTH/4, Board.BOARD_OFFSET_Y/4);
		opponentLabel.setSize(Board.BOARD_WIDTH/4,  Board.BOARD_OFFSET_Y/4);
		Font userFont = new Font("Courier", Font.PLAIN, Board.BOARD_OFFSET_Y/4);
		playerLabel.setFont(userFont);
		opponentLabel.setFont(userFont);
		playerLabel.setLocation(Board.BOARD_OFFSET_X, Board.BOARD_OFFSET_Y+Board.BOARD_WIDTH+playerLabel.getHeight()/2);
		opponentLabel.setLocation(Board.BOARD_OFFSET_X, Board.BOARD_OFFSET_Y-(opponentLabel.getHeight()*3/2));
		playerLabel.setForeground(Color.white);
		opponentLabel.setForeground(Color.white);

		add(playerLabel);
		add(opponentLabel);
		repaint();
	}
	
	/**
	 * Sets up the UI for the status label
	 * For now, status label only displays when a player has won or when
	 * searching for a game
	 */
	private void initStatusLabel() {
		statusLabel = new JLabel("", SwingConstants.CENTER);
		statusLabel.setForeground(Color.white);
		statusLabel.setSize(320, 100);
		statusLabel.setFont(new Font("SansSerif", Font.BOLD, 50));
		statusLabel.setLocation(Board.BOARD_WIDTH+Board.BOARD_OFFSET_X+50, Board.BOARD_OFFSET_Y);
		updateStatus();
		add(statusLabel);
	}
	
	/**
	 * Updates status label
	 * Note: Currently, disconnects are not properly implemented
	 */
	public void updateStatus() {
		String str = "";
		GameState status = board.position.gameState;
		switch(status) {
		case CHECKMATE_WHITE:
			str = "Black Wins!";
			break;
		case CHECKMATE_BLACK:
			str = "White Wins!";
			break;
		case DRAW_50_MOVE:
			str = "Draw!";
			break;
		case STALEMATE:
			str = "Draw!";
			break;
		case IN_PROGRESS:
			str = "";
			break;
		case TIME_OUT_WHITE:
			str = "Black Wins!";
			break;
		case TIME_OUT_BLACK:
			str = "White Wins!";
			break;
		case DISCONNECT_BLACK:
			str = "User Disconnect";
			break;
		case DISCONNECT_WHITE:
			str = "User Disconnect";
			break;
		case EMPTY_GAME:
			str = "Searching...";
			break;
		}
		statusLabel.setText(str);
	}
	
}
