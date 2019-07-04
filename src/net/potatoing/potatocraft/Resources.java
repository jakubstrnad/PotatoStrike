package net.potatoing.potatocraft;

import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Resources {

	BufferedImage spriteSheet;
	BufferedImage sprites[] = new BufferedImage[2];
	
	public Resources() {
		/*try {
			spriteSheet = ImageIO.read(getClass().getResource("/spriteSheet.png"));
		} catch (IOException e) {
			System.out.println("Couldn't find sprite sheet!");
			e.printStackTrace();
		}
		for(int i = 0; i < sprites.length; i++) {
			sprites[i] = spriteSheet.getSubimage(i*32, 0, 32, 32);
		}*/
	}
	
	public BufferedImage getBufferedImage(String pathWithinRes) {
		try {
			return ImageIO.read(getClass().getResource("/" + pathWithinRes));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
