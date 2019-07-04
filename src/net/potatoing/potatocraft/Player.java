package net.potatoing.potatocraft;

import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;

import javax.swing.JOptionPane;

public class Player {

	private String username = "iwantdie" + System.currentTimeMillis();
	private boolean localPlayer = false;
	
	public int x=0, y=0;
	int size = 32;
	private int bulletSize = 8;
	Game game;
	
	String ip = "-1";
	String port = "127.0.0.1";
	
	public int health = 100;
	
	private long lastShot = System.currentTimeMillis();
	private long shootInterval = 300l;
	
	int gunType = 0;
	
	long startTime = System.currentTimeMillis();
	
	public Player(Game game) {
		this(false, game);
	}
	
	public Player(boolean localPlayer, Game game) {
		this.localPlayer = localPlayer;
		this.game = game;
		bulletSize = Bullet.sizeS;
	}
	
	public void tick() {
		if(!localPlayer)
			return;
		
		if(health < 0)
			health = 0;
		
		if(health == 0) {		
			x = Game.WIDTH/2-game.players.get(0).size/2;
			y = Game.HEIGHT/2-game.players.get(0).size/2;
			game.xOffset = 0;
			game.yOffset = 0;
			health = 100;
			String moi = "/movP" + String.valueOf(x) + ";" + String.valueOf(y);
			try {
				game.networking.sendMsg(moi);
			} catch (IOException e) {
				System.out.println("failed to send a message");
				e.printStackTrace();
			}
			moi = "/hp:100";
			try {
				game.networking.sendMsg(moi);
			} catch (IOException e) {
				System.out.println("failed to send a message");
				e.printStackTrace();
			}
			//System.exit(0);
		}
		
		boolean moved = false;
		if(game.input.wDown) {
			moved = true;
			y--;
			game.yOffset--;
		}
		if(game.input.sDown) {
			moved = true;
			y++;
			game.yOffset++;
		}
		if(game.input.dDown) {
			moved = true;
			x++;
			game.xOffset++;
		}
		if(game.input.aDown) {
			moved = true;
			x--;
			game.xOffset--;
		}
		if(game.input.oneDown) {
			gunType = 0;
			bulletSize = 8;
		}
		if(game.input.twoDown) {
			gunType = 1;
			bulletSize = 16;
		}
		if(game.input.threeDown) {
			gunType = 2;
			bulletSize = 4;
		}
		
		//walls
		if(game.input.leftMouseClicked) { //fix this
			int wallX = game.input.mouseX+x-Game.WIDTH/2;
			int wallY = game.input.mouseY+y-Game.HEIGHT/2;
			game.blocks.add(new Block(wallX, wallY, true, game));
			try {
				game.networking.sendMsg("/wall;"+wallX+";"+wallY);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(game.input.leftDown && System.currentTimeMillis() - lastShot > shootInterval) {
			lastShot = System.currentTimeMillis();
			int bulX = x-bulletSize-1;
			int bulY = y+size/2-bulletSize/2;
			game.bullets.add(new Bullet(bulX, bulY, 2, gunType, game));
			try {
				game.networking.sendMsg("/shoot:"+bulX+":"+bulY+":2:"+gunType);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(game.input.rightDown && System.currentTimeMillis() - lastShot > shootInterval) {
			lastShot = System.currentTimeMillis();
			int bulX = x+size+1;
			int bulY = y+size/2-bulletSize/2;
			game.bullets.add(new Bullet(bulX, bulY, 3, gunType, game));
			try {
				game.networking.sendMsg("/shoot:"+bulX+":"+bulY+":3:"+gunType);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(game.input.upDown && System.currentTimeMillis() - lastShot > shootInterval) {
			lastShot = System.currentTimeMillis();
			int bulX = x+size/2-bulletSize/2;
			int bulY = y-bulletSize-1;
			game.bullets.add(new Bullet(bulX, bulY, 0, gunType, game));
			try {
				game.networking.sendMsg("/shoot:"+bulX+":"+bulY+":0:"+gunType);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(game.input.downDown && System.currentTimeMillis() - lastShot > shootInterval) {
			lastShot = System.currentTimeMillis();
			int bulX = x+size/2-bulletSize/2;
			int bulY = y+size+1;
			game.bullets.add(new Bullet(bulX, bulY, 1, gunType, game));
			try {
				game.networking.sendMsg("/shoot:"+bulX+":"+bulY+":1:"+gunType);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(game.input.enterClicked) {
			//not rendering while waiting for input
			String msg = JOptionPane.showInputDialog("Zadejte zpravu: ");
			game.addServerMessage(username + ": " + msg);
			msg = "/chat;" + username + ":" + msg;
			try {
				game.networking.sendMsg(msg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!moved || game.networking == null)
			return;
		String moi = "/movP" + String.valueOf(x) + ";" + String.valueOf(y);
		try {
			game.networking.sendMsg(moi);
		} catch (IOException e) {
			System.out.println("failed to send a message");
			e.printStackTrace();
		}
	}
	
	public void render(Graphics g) {
		g.setColor(Color.BLUE);
		if(!localPlayer) {
			g.setColor(Color.WHITE);
			g.drawString(username, x-g.getFontMetrics().stringWidth(username)/2+size/2-game.xOffset, y-10-game.yOffset);
		}
		g.fillRect(x-game.xOffset, y-game.yOffset, size, size);
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean islocalPlayer() {
		return localPlayer;
	}
	
}
