package net.potatoing.potatocraft;

import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;

public class Bullet {

	int x, y, dir;
	/*
	 * 0=up
	 * 1=down
	 * 2=left
	 * 3=right
	 * */
	int size = 8;
	static int sizeS = 8;
	int speed = 5;
	int damage = 10;
	public boolean die = false;
	Game game;
	long created = 0l;
	long timeToLive = 3000l;
	
	public Bullet(int x, int y, int dir, int type, Game game) {
		this.x = x;
		this.y = y;
		this.dir = dir;
		switch(type) {
		case 0:
			speed = 5;
			size = 8;
			damage = 10;
			break;
		case 1:
			speed = 2;
			size = 16;
			damage = 24;
			break;
		case 2:
			speed = 10;
			size = 4;
			damage = 5;
			break;
		}
		created = System.currentTimeMillis();
		this.game = game;
	}
	
	public void tick() {
		switch(dir) {
		case 0:
			y-=speed;
			break;
		case 1:
			y+=speed;
			break;
		case 2:
			x-=speed;
			break;
		case 3:
			x+=speed;
			break;
		}
		
		if(System.currentTimeMillis() - created > timeToLive)
			die = true;
		
		int iterator = 0;
		while(!game.canWorkWithPlayers) {}
		game.canWorkWithPlayers = false;
		for(Player player : game.players) {
			if(player.x < x+size && player.x+player.size > x && player.y<y+size && player.y+player.size > y && !die) {
				game.players.get(iterator).health -= damage;
				int hp = game.players.get(iterator).health;
				if(game.players.get(iterator).islocalPlayer()) {
					try {
						game.networking.sendMsg("/hp:" + hp);
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else if(hp <= 0) {
					game.players.get(iterator).health = 100;
				}
				die = true;
			}
			iterator++;
		}
		game.canWorkWithPlayers = true;

		iterator = 0;
		while(!game.canWorkWithBlocks) {}
		game.canWorkWithBlocks = false;
		for(Block block : game.blocks) {
			if(block.x < x+size && block.x+block.size > x && block.y<y+size && block.y+block.size > y && !die) {
				game.blocks.get(iterator).health -= damage;
				int hp = game.blocks.get(iterator).health;
				if(game.blocks.get(iterator).localPlayerIsOwner) {
					try {
						game.networking.sendMsg("/blockHp:" + block.x + ":" + block.y + ":" + hp); //TODO: change ownership if player disconnects
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				if(hp <= 0) {
					die = true;
					game.blocks.remove(block);
					game.canWorkWithBlocks = true;
					return;
				}
				die = true;
			}
			iterator++;
		}
		game.canWorkWithBlocks = true;
	}
	
	public void render(Graphics g) {
		g.setColor(Color.RED);
		g.fillRect(x-game.xOffset, y-game.yOffset, size, size);
	}
	
}
