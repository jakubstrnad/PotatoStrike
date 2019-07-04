package net.potatoing.potatocraft;

import java.io.IOException;
import java.net.DatagramPacket;

public class Networking implements Runnable {
    
	Game game;
	public boolean networkingRunning = true;
	
	public Networking(Game game) {
		this.game = game;
	}
	
    public void start() {
    	
    }
    
	public void run() {
		byte[] messageBuffer;
		while(Game.running && networkingRunning) {
			try {  //yessss. It crashes? why bother fixing, just yeet it all into a try and catch block. heck yea
				messageBuffer =  new byte[Game.BUF_SIZE];
				DatagramPacket packet = new DatagramPacket(messageBuffer, messageBuffer.length);
				try {
					game.socketClient.receive(packet);
				} catch (IOException e) {
					System.out.println("Failed while recieving a packet");
					e.printStackTrace();
				}
				String mop = new String(packet.getData(), 0, packet.getLength());
				//System.out.println("Recieved message: " + mop);
				if(mop.startsWith("/usrs")) {
					String[] msgM = mop.split(":");
					int i = 1;
					while(i < msgM.length) {
						Player p = new Player(game);
						p.ip = msgM[i];
						i++;
						p.port = msgM[i];
						i++;
						p.setUsername(msgM[i]);
						i++;
						p.x = Integer.parseInt(msgM[i])-game.xOffset;
						i++;
						p.y = Integer.parseInt(msgM[i])-game.yOffset;
						i++;
						p.health = Integer.parseInt(msgM[i]);
						i++;
						if(p.ip != game.players.get(0).ip && !p.getUsername().equals(game.players.get(0).getUsername()))
								game.players.add(p);
					}
					game.waitForPlayersAdd = false;
				} else if(mop.startsWith("/blocks")) {
					String[] msgM = mop.split(":");
					int i = 1;
					while(i < msgM.length) {
						Block block = new Block(0, 0, false, game);
						block.x = Integer.parseInt(msgM[i]);
						i++;
						block.y = Integer.parseInt(msgM[i]);
						i++;
						block.health = Integer.parseInt(msgM[i]);
						i++;
						game.blocks.add(block);
					}
					game.waitForPlayersAdd = false;
				} else {
					String[] msgTmp;
					String[] msgTmp2;
					String ipM = "";
					String portM = "";
					String msgM = "";
					
					msgTmp = mop.split(":");
					msgTmp2 = msgTmp[1].split("!");
					ipM = msgTmp[0];
					portM = msgTmp2[0];
					msgM = msgTmp2[1];
					
					
					if(msgM.startsWith("/con", 0)) {
						Player p = new Player(game);
						p.ip = ipM;
						p.port = portM;
						p.setUsername(msgM.substring(4, msgM.length()));
						while(!game.canWorkWithPlayers) {}
						game.canWorkWithPlayers = false;
						p.x = Game.WIDTH/2-game.players.get(0).size/2;//-game.xOffset;
						p.y = Game.HEIGHT/2-game.players.get(0).size/2;//-game.yOffset;
						game.players.add(p);
						game.canWorkWithPlayers = true;
					} else if (msgM.startsWith("/chat", 0)) {
						String[] am = mop.split("/");
						System.out.println(am[1]);
						String[] msgChtTmp = am[1].split(";");
						System.out.println(msgChtTmp[1]);
						String[] msgCht = msgChtTmp[1].split(":");
						if(msgCht.length == 2)
							game.addServerMessage(msgCht[0] + ": " + msgCht[1]);
					} else if(msgM.startsWith("/shoot")) {
						//mop = ip:port-/shoot:x:y:dir
						String[] shootMeta = mop.split("!");
						String[] shootNfo = shootMeta[1].split(":");
						while(!game.canWorkWithBullets) {}
						game.canWorkWithBullets = false;
						game.bullets.add(new Bullet(Integer.parseInt(shootNfo[1]), Integer.parseInt(shootNfo[2]), Integer.parseInt(shootNfo[3]), Integer.parseInt(shootNfo[4]), game));
						game.canWorkWithBullets = true;
					} else if(msgM.startsWith("/mov")) {
						//todo: zaporna cisla nefunguji
						String[] m = msgM.split("P");
						String msgMM = m[1];
						String[] cords = msgMM.split(";");
						int x = Integer.parseInt(cords[0]);
						int y = Integer.parseInt(cords[1]);
						int index = 0;
						while(!game.canWorkWithPlayers) {}
						game.canWorkWithPlayers = false;
						for(Player poop : game.players) {
							if(poop.ip.equals(ipM) && poop.port.equals(portM)) {
								game.players.get(index).x = x;
								game.players.get(index).y = y;
							}
							index++;
						}
						game.canWorkWithPlayers = true;
					} else if(msgM.startsWith("/wall")) {
						String[] wallNfo = mop.split(";");
						String x = wallNfo[1];
						String y = wallNfo[2];
						while(!game.canWorkWithBlocks) {}
						game.canWorkWithBlocks = false;
						game.blocks.add(new Block(Integer.parseInt(x),Integer.parseInt(y),false,game));
						game.canWorkWithBlocks = true;
					}
				}
			} catch(Exception e) {
				System.out.println("TODO: better error reporting system");
			}
		}
		stop();
	}
	
    public void sendMsg(String msg) throws IOException {
    	if(game.socketClient == null)
    		return;
        game.bufClient = msg.getBytes();
        DatagramPacket packet 
          = new DatagramPacket(game.bufClient, game.bufClient.length, game.addressClient, game.portClientButNo);
        game.socketClient.send(packet);
    }
	
	public void stop() {
		game.socketClient.close();
	}

}
