package net.potatoing.potatocraft;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class Game extends Canvas implements Runnable {

	private static final long serialVersionUID = 1L;
	
	public static final String TITLE = "iwantdie";
	public static final int WIDTH = 600;
	public static final int HEIGHT = WIDTH * 9 / 16;
	public static final Dimension gameDim = new Dimension(WIDTH, HEIGHT);
	BufferedImage image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
	JFrame frame;
	public static boolean running = false;
	public static boolean paused = false; //idk how to use this

	private final double TICK_CAP = 1.0/60.0;
	
	InputHandler input;
	Resources res;

	BufferedImage mainMenu, settings;

	List<Player> players;
	List<Block> blocks;
	List<Bullet> bullets;
	
	int port = 25565;
	String[] serverMessages = new String[22];
	
	private DatagramSocket socket;
	private byte[] messageBuffer = new byte[BUF_SIZE];
	
	private boolean isServer = false;
	
	List<InetAddress> connectedClientsIps = new ArrayList<InetAddress>();
	List<Integer> connectedClientsPorts = new ArrayList<Integer>();
	
	boolean firstTime = true;
	
	Networking networking;
	Thread networkingThread;
	
    public DatagramSocket socketClient;
    public InetAddress addressClient;
 
    public byte[] bufClient;
    
    int portClientButNo;

    boolean waitForPlayersAdd = true;
    public boolean canWorkWithBullets = true;
    public boolean canWorkWithPlayers = true;
    public boolean canWorkWithBlocks = true;
    
    public static final int BUF_SIZE = 1024;
    
    int xOffset = 0;
    int yOffset = 0;
    
	public Game() {
		input = new InputHandler(this);
		res = new Resources();

		mainMenu = res.getBufferedImage("state_mainMenu.png");
		settings = res.getBufferedImage("state_settings.png");
		
		players = new ArrayList<Player>();
		players.add(new Player(true, this));
		players.get(0).x = WIDTH/2-players.get(0).size/2;
		players.get(0).y = HEIGHT/2-players.get(0).size/2;
		
		blocks = new ArrayList<Block>();
		bullets = new ArrayList<Bullet>();
		
		setMinimumSize(gameDim);
		setMaximumSize(gameDim);
		setPreferredSize(gameDim);
		
		frame = new JFrame(TITLE);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.add(this, BorderLayout.CENTER);
		frame.pack();
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		requestFocus();
	}
	
	public void start() {
		running = true;
		new Thread(this).start();
	}
	
	public void stop() {
		System.out.println("test");
		running = false;
		if(isServer)
			socket.close();
		System.exit(0);
	}
	
	public void run() {
		boolean render = false;
		double firstTime = 0;
		double lastTime = System.nanoTime() / 1000000000.0;
		double passedTime = 0;
		double unprocessedTime = 0;
		
		double frameTime = 0;
		int frames = 0;
		int fps = 0;
		
		while(running && !isServer) {
			render = false;
			
			firstTime = System.nanoTime() / 1000000000.0;
			passedTime = firstTime - lastTime;
			lastTime = firstTime;
			
			unprocessedTime += passedTime;
			frameTime += passedTime;
			
			while(unprocessedTime >= TICK_CAP) {
				unprocessedTime -= TICK_CAP;
				render = true;
				tick();
				if(frameTime >= 1.0) {
					frameTime = 0;
					fps = frames;
					frames = 0;
					//System.out.println("FPS: " + fps);
					frame.setTitle(TITLE + " | FPS: " + fps);
				}
			}
			if(render) {
				frames++;
				render();
			}
			else {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if(isServer) {
			while(running) {
				tick();
				render();
			}
		}
		stop();
	}
	
	private void host() {
		if(JOptionPane.showConfirmDialog(frame, "Are you sure?\nThis is some complex shit") == 0) {
			String ipS = JOptionPane.showInputDialog("Enter IP to run the server on (leave blank for localhost): ");
			if(ipS == null)
				return;
			if(ipS.length() == 0)
				ipS = "127.0.0.1";
			
			String portS = JOptionPane.showInputDialog("Enter Port to run the server on (leave blank for 25565): ");
			if(portS == null)
				return;
			if(portS.length() != 0)
				port = strToInt(portS);
			
			addServerMessage("Starting server at " + ipS + ":" + port);
			try {
				socket = new DatagramSocket(null);
				InetSocketAddress address = new InetSocketAddress(ipS, port);
				socket.bind(address);
			} catch (SocketException e) {
				System.out.println("Failed to start the server");
				e.printStackTrace();
			}
			isServer = true;
			addServerMessage("Server started!");
			GameStates.gameState = GameStates.SERVER;
		}
	}
	
	public void addServerMessage(String message) {
		if(serverMessages[serverMessages.length-1] != null) {
			for(int i = 0; i < serverMessages.length-1; i++) {
				serverMessages[i] = serverMessages[i+1];
			}
			serverMessages[serverMessages.length-1] = message;
		} else {
			int whereToAdd = 0;
			while(serverMessages[whereToAdd] != null) {
				whereToAdd++;
			}
			serverMessages[whereToAdd] = message;
		}
	}
	
	private int strToInt(String s) {
		int prd = -1;
		try {
			prd = Integer.parseInt(s);
		} catch (NumberFormatException nfe) {
			System.out.println("Unable to convert string to int at port assigment!");
			nfe.printStackTrace();
		}
		return prd;
	}
	
	private void join() {
		String ip = JOptionPane.showInputDialog("Ënter server ip (leave blank for localhost)");
		if(ip == null) {
			return;
		}
		if(ip.length() == 0) {
			ip = "127.0.0.1";
		}
		String portSS = JOptionPane.showInputDialog("Enter server port(leave blank for 25565): ");
		portClientButNo = 25565;
		if(portSS == null) {
			return;
		}
		if(portSS.length() != 0)
			portClientButNo = strToInt(portSS);
		
        try {
			socketClient = new DatagramSocket();
		} catch (SocketException e2) {
			e2.printStackTrace();
		}
        try {
			addressClient = InetAddress.getByName(ip);
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
        networking = new Networking(this);
		networkingThread = new Thread(new Networking(this));
		networkingThread.start();
		
		try {
			networking.sendMsg("/con" + players.get(0).getUsername());
		} catch (IOException e) {
			e.printStackTrace();
		}
		//todo
		GameStates.gameState = GameStates.GAME;
	}
	
	public void tick() {
		input.tick();
		
		if(input.mouseClicked && GameStates.gameState != GameStates.GAME && GameStates.gameState != GameStates.SERVER) {
			int mouseX = input.mouseX;
			int mouseY = input.mouseY;
			
			switch(GameStates.gameState) {
			case GameStates.MAIN_MENU:
				if(mouseX > 181 && mouseX < 424) {
					if(mouseY > 88 && mouseY < 144) { //host
						host();
					} else if(mouseY > 149 && mouseY < 203) { //join
						join();
					} else if(mouseY > 209 && mouseY < 265) { //settings
						GameStates.gameState = GameStates.SETTINGS;
					} else if(mouseY > 271 && mouseY < 326) { //stop
						stop();
					}
				}
				break;
			case GameStates.SETTINGS:
				if(mouseX > 11 && mouseY > 81 && mouseX < 590 && mouseY < 127) { //selected username typing box
					String usrnm = JOptionPane.showInputDialog("Enter a username:");
					if(usrnm.length() > 101)
						usrnm = usrnm.substring(0, 101);
					if(usrnm != null && usrnm.length() > 0)
						players.get(0).setUsername(usrnm);
				} else if(mouseX > 177 && mouseY > 246 && mouseX < 419 && mouseY < 301) { //save
					GameStates.gameState = GameStates.MAIN_MENU;
				}
				break;
			}
		}
		if(GameStates.gameState == GameStates.GAME) { //why not just use else? future proof. trust me, it'll be worth it
			long canWeStart = System.currentTimeMillis();
			while(waitForPlayersAdd) {
				if(System.currentTimeMillis() - canWeStart > 2500) {
					System.out.println("Connection timed out.. Retrying (this may take a while)");
					System.out.println("Clearing connected players arraylist");
					Player p = players.get(0);
					players.clear();
					players.add(p);
					System.out.println("Asking server for connected users");
					try {
						networking.sendMsg("/uzrfail");
					} catch (IOException e) {
						e.printStackTrace();
					}
					canWeStart = System.currentTimeMillis();
					/*try {
						System.out.println("Sending info about reconnecting to the server");
						networking.sendMsg("/hp:0");
						System.out.println("Stopping network thread");
						networking.networkingRunning = false;
						System.out.println("Waiting for network thread to stop");
						Thread.sleep(500);
						System.out.println("Creating new datagram socket");
						socketClient = new DatagramSocket();
						System.out.println("Updating local player port");
						players.get(0).port = String.valueOf(socketClient.getPort());
						System.out.println("Reinitializing network object");
				        networking = new Networking(this);
						System.out.println("Reinitializing network thread");
						networkingThread = new Thread(new Networking(this));
						System.out.println("Starting network thread");
						networkingThread.start();
						System.out.println("Connecting to server");
						networking.sendMsg("/con" + players.get(0).getUsername());
						canWeStart = System.currentTimeMillis();
						System.out.println("Waiting for server response..");
					} catch (InterruptedException | IOException e1) {
						System.out.println("Failed to connect, please restart the game");
						e1.printStackTrace();
					}*/
				}
			}
			while(!canWorkWithPlayers) {
				
			}
			canWorkWithPlayers = false;
			for(Player player : players)
				player.tick();
			canWorkWithPlayers = true;
			while(!canWorkWithBlocks) {}
			canWorkWithBlocks = false;
			for(Block block : blocks) //hopefully fixed
				block.tick();
			canWorkWithBlocks = true;
			
			while(!canWorkWithBullets) {}
			canWorkWithBullets = false;
			boolean removedBullets = true;
			while(removedBullets) {
				removedBullets = false;
				for(Bullet bullet : bullets) {
					bullet.tick();
					if(bullet.die) {
						bullets.remove(bullet);
						removedBullets = true;
						break;
					}
				}
			}
			canWorkWithBullets = true;
		} else if(GameStates.gameState == GameStates.SERVER) {
			if(firstTime) {
				firstTime = false;
				players.remove(0);
				return;
			}
			messageBuffer =  new byte[BUF_SIZE];
			DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				System.out.println("Failed while recieving a packet");
				e.printStackTrace();
			}
			InetAddress addressPkg = packet.getAddress();
			int portPkg = packet.getPort();
			String mop = new String(packet.getData(), 0, packet.getLength());
			String betterMop = addressPkg.getHostAddress() + ":" + Integer.toString(portPkg) + "!" + mop;
			//check if mop is /connect, if so, save ip and port
			boolean send = true;
			if(mop.startsWith("/con", 0)) {
				connectedClientsIps.add(addressPkg);
				connectedClientsPorts.add(portPkg);
				addServerMessage("A new client has connected");
				addServerMessage("Sending connected client to newly connected clients");
				Player p = new Player(this);
				p.ip = addressPkg.getHostAddress();
				p.port = Integer.toString(portPkg);
				p.setUsername(mop.substring(4, mop.length()));
				players.add(p);
				addServerMessage("Connected users:");
				for(Player pl : players) {
					addServerMessage(pl.getUsername());
				}
				String connectedUsers = "/usrs:";
				for(Player pl : players) {
					connectedUsers += pl.ip + ":" + pl.port + ":" + pl.getUsername() + ":" + pl.x + ":" + pl.y + ":" + pl.health + ":";
				}
				//System.out.println(connectedUsers);
				packet = new DatagramPacket(connectedUsers.getBytes(), connectedUsers.getBytes().length, addressPkg, portPkg);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
				

				String walls = "/blocks:";
				for(Block block : blocks) {
					walls += block.x + ":" + block.y + ":" + block.health + ":";
				}
				packet = new DatagramPacket(walls.getBytes(), walls.getBytes().length, addressPkg, portPkg);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			} else if(mop.startsWith("/mov", 0)) {
				String[] mpp = mop.split("P");
				String mopPos = mpp[1];
				
				String ipM = addressPkg.getHostAddress();
				String portM = String.valueOf(portPkg);
				
				int x = Integer.parseInt(mopPos.split(";")[0]);
				int y = Integer.parseInt(mopPos.split(";")[1]);
				int index = 0;
				for(Player poop : players) {
					if(poop.ip.equals(ipM) && poop.port.equals(portM)) {
						players.get(index).x = x;
						players.get(index).y = y;
					}
					index++;
				}
				
			} else if(mop.startsWith("/hp")) {
				send = false;
				String[] p = mop.split(":");
				int hp = Integer.parseInt(p[1]);
				int iterator = 0;
				int remove = -1;
				String saddressPkg = addressPkg.getHostAddress();
				for(Player pp : players) {
					int portT = Integer.parseInt(pp.port);
					String ipT = pp.ip;
					if(saddressPkg.equals(ipT) && portPkg == portT) {
						if(hp == 0) {
							remove = iterator;
						} else {
							players.get(iterator).health = hp;
						}
					}
					iterator++;
				}
				if(remove != -1) {
					//addServerMessage("Player " + players.get(remove).getUsername() + " has diconnected");
					//players.remove(remove);
					players.get(iterator).x = 0;
					players.get(iterator).y = 0;
					players.get(iterator).health = 100;
				}
			} else if(mop.startsWith("uzrfail")) {
				send = false;
				String connectedUsers = "/usrs:";
				for(Player pl : players) {
					connectedUsers += pl.ip + ":" + pl.port + ":" + pl.getUsername() + ":" + pl.x + ":" + pl.y + ":" + pl.health + ":";
				}
				//System.out.println(connectedUsers);
				packet = new DatagramPacket(connectedUsers.getBytes(), connectedUsers.getBytes().length, addressPkg, portPkg);
				try {
					socket.send(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else if(mop.startsWith("/wall")) {
				String[] wallNfo = mop.split(";");
				String x = wallNfo[1];
				String y = wallNfo[2];
				blocks.add(new Block(Integer.parseInt(x),Integer.parseInt(y),false,this));
				//addServerMessage("Wall placed at " + x + ", " + y);
			} else if(mop.startsWith("/blockHp")) {
				String[] blockNfo = mop.split(":");
				int bx = Integer.parseInt(blockNfo[1]);
				int by = Integer.parseInt(blockNfo[2]);
				int bhp = Integer.parseInt(blockNfo[3]);
				for(Block bl : blocks) {
					if(bl.x == bx && bl.y == by) {
						bl.health = bhp;
						if(bl.health <= 0) {
							blocks.remove(bl);
							return;
						}
					}
				}
			}
			
			if(send) {
				int iterator = 0;
				for(InetAddress ipT : connectedClientsIps) {
					int portT = connectedClientsPorts.get(iterator);
					if(addressPkg != ipT && portPkg != portT) {
						packet = new DatagramPacket(betterMop.getBytes(), betterMop.getBytes().length, ipT, portT);
						try {
							socket.send(packet);
						} catch (IOException e) {
							System.out.println("rip");
							e.printStackTrace();
						}
					}
					iterator++;
				}
			}
		}
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, getWidth(), getHeight(), null);

		//System.out.println(System.currentTimeMillis());
		//render stuff here
		switch(GameStates.gameState) {
			case GameStates.MAIN_MENU:
				g.drawImage(mainMenu, 0, 0, getWidth(), getHeight(), null);
				break;
			case GameStates.SETTINGS:
				g.drawImage(settings, 0, 0, getWidth(), getHeight(), null);
				g.setColor(Color.WHITE);
				g.drawString(players.get(0).getUsername(), 11, 111);
				break;
			case GameStates.GAME:
				while(waitForPlayersAdd) {
					
				}
				while(!canWorkWithPlayers) {}
				canWorkWithPlayers =  false;
				for(Player player : players) {
					player.render(g);
					g.setColor(Color.WHITE);
					g.drawRect(player.x-10-xOffset, player.y-8-yOffset, 50, 5);
					g.setColor(Color.GREEN);
					float barFill = (player.health/100f)*100f/2f-1f;
					g.fillRect((player.x-9) - xOffset, (player.y-7) - yOffset, (int)barFill, 4);
				}
				canWorkWithPlayers = true;
				while(!canWorkWithBlocks) {}
				canWorkWithBlocks =  false;
				for(Block block : blocks) {
					block.render(g);
					g.setColor(Color.WHITE);
					g.drawRect(block.x-10-xOffset, block.y-8-yOffset, 50, 5);
					g.setColor(Color.GREEN);
					float barFill = (block.health/100f)*100f/2f-1f;
					g.fillRect((block.x-9) - xOffset, (block.y-7) - yOffset, (int)barFill, 4);
				}
				canWorkWithBlocks =  true;
				while(!canWorkWithBullets) { }
				canWorkWithBullets = false;
				for(Bullet bullet : bullets)
					bullet.render(g);
				canWorkWithBullets = true;
				g.setColor(Color.WHITE);
				for(int y = 0; y < serverMessages.length; y++) {
					if(serverMessages[y] == null)
						break;
					g.drawString(serverMessages[y], 3, (y+1)*15);
				}
				break;
			case GameStates.SERVER:
				g.setColor(Color.WHITE);
				for(int y = 0; y < serverMessages.length; y++) {
					if(serverMessages[y] == null)
						break;
					g.drawString(serverMessages[y], 3, (y+1)*15);
				}
				break;
		}
		
		g.dispose();
		bs.show();
	}

}
