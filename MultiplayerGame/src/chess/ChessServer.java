package chess;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.io.*;
import java.awt.event.*;
import javax.swing.Timer;

/**
 * This program continuously takes connection
 * requests on the LISTENING_PORT and forms
 * a chess game when 2 players have joined.
 */
public class ChessServer {
	
	public static final int LISTENING_PORT = 9876;
	private String startingPosition = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
	
	public ChessServer() {
		ServerSocket listener;
		Socket connection;
		ArrayList<ConnectionHandler> connections = new ArrayList<ConnectionHandler>();
		
		try {
            listener = new ServerSocket(LISTENING_PORT);
            System.out.println("Listening on port " + LISTENING_PORT);
            while (true) {
            	//looks for 2 connections to make a game
            	connection = listener.accept();
            	System.out.println("accepted connection");           	
            	ConnectionHandler handler = new ConnectionHandler(connection);
            	boolean startedGame = false;
            	for(int i = 0; i < connections.size(); i++) {
            		// finds 2 connections that match in time control
            		if(connections.get(i).timeControl == handler.timeControl) {
            			try {
            				connections.get(i).oos.writeObject("Start");
            				handler.oos.writeObject("Start");
            			}
            			catch(Exception e) {
            				connections.remove(i);
            				break;
            			}
            			GameHandler gh = new GameHandler(connections.get(i), handler, handler.timeControl);
            			connections.remove(i);
            			startedGame = true;
            			gh.start();
            			System.out.println("Started game");
            			break;
            		}
            	}
            	if(!startedGame) {
            		connections.add(handler);
            	}
            }
        } catch (Exception e) {
            System.out.println("Sorry, the server has shut down.");
            System.out.println("Error:  " + e);
            return;
        }
	}
	/**
	 * Stores important information about a single client
	 */
	private class ConnectionHandler {
		long timeControl;
		String username;
		ObjectInputStream ois;
		ObjectOutputStream oos;
		
		ConnectionHandler(Socket client) {
			try {
				ois = new ObjectInputStream(client.getInputStream());
				oos = new ObjectOutputStream(client.getOutputStream());
				this.username = (String)ois.readObject();
            	this.timeControl = (long)ois.readObject();
			} catch (Exception e) {
				e.printStackTrace();
				
			}
		}
	}
	
	/**
	 * Defines a thread that handles the connection between
	 * 2 clients, each part of the same game
	 */
	private class GameHandler extends Thread {
		ConnectionHandler client1;
		ConnectionHandler client2;
		long client1Time;
		long client2Time;
		boolean whiteTurn;
		volatile boolean inGame;
		GameHandler(ConnectionHandler client1, ConnectionHandler client2, long timeControl) {
			this.client1 = client1;
			this.client2 = client2;
			client1Time = timeControl;
			client2Time = timeControl;
			inGame = true;
			whiteTurn = true;
			
		}
		public void run() {
			try {
				//first person is white
				client1.oos.writeObject(true);
				client1.oos.writeObject(startingPosition);
				client1.oos.writeObject(client2.username);
				//second person is not white
				client2.oos.writeObject(false);
				client2.oos.writeObject(startingPosition);
				client2.oos.writeObject(client1.username);
				ClientReader r1 = new ClientReader(this, client1, client2);
				ClientReader r2 = new ClientReader(this, client2, client1);
				r1.start();
				r2.start();
				// handles the time remaining for both players
				Timer t = new Timer(100, new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						//decrement time by 100ms since this method is called every 100ms
						if(whiteTurn) {
							client1Time -= 100;
						}
						else {
							client2Time -= 100;
						}
						long[] timePair = {client1Time, client2Time};
						//communicates time between 2 clients
						try {
							client1.oos.writeObject(timePair);
							client2.oos.writeObject(timePair);
						}
						catch (Exception exception) {
							((Timer)e.getSource()).stop();
							System.out.println("Failed to write time to a client.");
						}
					}
				});
				t.start();
			}
			catch (Exception e) {
				inGame = false;
			}
		}
	}
	/**
	 * Reads data sent by client to the server and acts accordingly
	 */
	private class ClientReader extends Thread {
		GameHandler gh;
		ConnectionHandler clientRead;
		// client's opponent
		ConnectionHandler clientOther;
		
		ClientReader(GameHandler gh, ConnectionHandler clientRead, ConnectionHandler clientOther) {
			this.gh = gh;
			this.clientRead = clientRead;
			this.clientOther = clientOther;
		}
		
		public void run() {
			while(gh.inGame) {
				try {
					Object o = clientRead.ois.readObject();
					// communicates moves between clients
					if(o instanceof Move) {
						gh.whiteTurn = !gh.whiteTurn;
						clientRead.oos.writeObject(o);
						clientOther.oos.writeObject(o);
					}
					// communicates usernames between clients
					else if(o instanceof String) {
						clientRead.oos.writeObject(clientRead.username + ": " + (String)o);
						clientOther.oos.writeObject(clientRead.username + ": " + (String)o);
					}
				}
				catch (Exception e) {
					gh.inGame = false;
					System.out.println("Failed to read object from client");
				}
			}
		}
	}
	
	public static void main(String[] args) {
		new ChessServer();
	}
}
