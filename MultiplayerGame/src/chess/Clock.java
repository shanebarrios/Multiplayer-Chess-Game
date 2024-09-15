package chess;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.*;

/**
 * Displays the UI for a single player's clock
 */
public class Clock extends JLabel {
	private long time;
	
	public Clock(long timeControl) {
		setSize(new Dimension(Board.BOARD_WIDTH/3, Board.BOARD_OFFSET_Y/2));
		setFont(new Font("Courier", Font.PLAIN, Board.BOARD_OFFSET_Y/2));
		setForeground(Color.white);
		setHorizontalAlignment(SwingConstants.RIGHT);
		time = timeControl;
		updateText();
	}
	
	private void updateText() {
		long timeSeconds = time/1000%60;
		long timeMinutes = time/60000; 
		long timeDeciseconds = time%1000/100;
		
		String text = Long.toString(timeMinutes) + ":";
		
		if(timeSeconds < 10) {
			text += "0";
		}
		text += Long.toString(timeSeconds);
		
		// adds further precision if time is low
		if(time < 60000) {
			text += "." + Long.toString(timeDeciseconds);
		}
		setText(text);
	}
	
	public long getTime() {
		return time;
	}
	
	/**
	 * Allows for updating the time label when needed given the inputted time
	 */
	public void updateTime(long time) {
		if(time <= 0) {
			this.time = 0;
		}
		else {
			this.time = time;
		}
		updateText();
	}
	
}
