package net.potatoing.potatocraft;

import java.awt.Color;
import java.awt.Graphics;

public class Block {
	
	int x=0, y=0, size = 32, health = 100;
	Game game;
	boolean localPlayerIsOwner = false;
	
	public Block(int x, int y, boolean localPlayerIsOwner, Game game) {
		this.x = x;
		this.y = y;
		this.game = game;
		this.localPlayerIsOwner = localPlayerIsOwner;
	}
	
	public void tick() {
		
	}
	
	public void render(Graphics g) {
		g.setColor(Color.GRAY);
		g.fillRect(x-game.xOffset, y-game.yOffset, size, size);
	}
}
