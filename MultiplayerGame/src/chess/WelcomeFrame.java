package chess;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class WelcomeFrame extends JFrame implements ActionListener {
	private JTextField usernameInput;
	private JTextField ipInput;
	private JComboBox timeSelection;
	private JButton connectOnline;
	private JButton connectOffline;
	
	private long timeControl;
	
	public WelcomeFrame() {
		
		// sets up window
		timeControl = 60000;
		Color bgColor = new Color(50, 50, 50);
		setTitle("Chess");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().setBackground(bgColor);
		setResizable(false);
		
		// sets up basic UI elements
		JPanel titlePanel = new JPanel();
		titlePanel.setLayout(new FlowLayout());
		titlePanel.setBackground(bgColor);
		Piece pawn = new Piece(true, "Pawn", -1, -1);
		ImageIcon pawnIcon = new ImageIcon(pawn.img);
		JLabel pawnLabel1 = new JLabel(pawnIcon);
		titlePanel.add(pawnLabel1);
		JLabel titleLabel = new JLabel("CHESS");
		titleLabel.setFont(new Font("SansSerif", Font.BOLD, 100));
		titleLabel.setForeground(new Color(150, 75, 0));
		titlePanel.add(titleLabel);
		JLabel pawnLabel2 = new JLabel(pawnIcon);
		titlePanel.add(pawnLabel2);
		add(titlePanel);
		
		// input for username
		JPanel usernamePanel = new JPanel();
		usernamePanel.setLayout(new FlowLayout());
		usernamePanel.setBackground(bgColor);
		JLabel usernameLabel = new JLabel("Username:");
		usernameLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
		usernameLabel.setForeground(Color.WHITE);
		usernameInput = new JTextField();
		usernameInput.setColumns(10);
		usernameInput.setFont(new Font("SansSerif", Font.PLAIN, 15));
		usernamePanel.add(usernameLabel);
		usernamePanel.add(usernameInput);
		add(usernamePanel);
		
		//input for IP
		JPanel ipPanel = new JPanel();
		ipPanel.setLayout(new FlowLayout());
		ipPanel.setBackground(bgColor);
		JLabel ipLabel = new JLabel("Server IP:");
		ipLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
		ipLabel.setForeground(Color.WHITE);
		ipInput = new JTextField("localhost");
		ipInput.setColumns(10);
		ipInput.setFont(new Font("SansSerif", Font.PLAIN, 15));
		ipPanel.add(ipLabel);
		ipPanel.add(ipInput);
		add(ipPanel);
		
		
		// input for time control
		JPanel timePanel = new JPanel();
		timePanel.setLayout(new FlowLayout());
		timePanel.setBackground(bgColor);
		JLabel timeLabel = new JLabel("Time control:");
		timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 30));
		timeLabel.setForeground(Color.WHITE);
		String[] timeControls = {"1 min", "3 min", "5 min", "10 min", "20 min"};
		timeSelection = new JComboBox(timeControls);
		timeSelection.addActionListener(this);
		timePanel.add(timeLabel);
		timePanel.add(timeSelection);
		add(timePanel);
		
		// input to connect
		JPanel connectPanel = new JPanel();
		connectPanel.setLayout(new FlowLayout());
		connectPanel.setBackground(bgColor);
		connectOnline = new JButton("Connect");
		connectOnline.setPreferredSize(new Dimension(200, 100));
		connectOnline.setBackground(Color.GREEN);
		connectOnline.setFont(new Font("SansSerif", Font.BOLD, 30));
		connectOnline.addActionListener(this);
		connectPanel.add(connectOnline);
		add(connectPanel);
		
		setSize(600, 600);
		setVisible(true);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == timeSelection) {
			String choice = (String)(timeSelection.getSelectedItem());
			switch(choice) {
			case "1 min":
				timeControl = 60000;
				break;
			case "3 min":
				timeControl = 180000;
				break;
			case "5 min":
				timeControl = 300000;
				break;
			case "10 min":
				timeControl = 600000;
				break;
			case "20 min":
				timeControl = 1200000;
				break;
			}
		}
		if(e.getSource() == connectOnline) {
			if(usernameInput.getText().length() > 2 && usernameInput.getText().length() <= 20 &&
					ipInput.getText().length() > 2) {
				setVisible(false);
				dispose();
				new Player(timeControl, usernameInput.getText(),ipInput.getText());
			}
		}
	}
	
	public static void main(String[] args) {
		new WelcomeFrame();
	}
}
