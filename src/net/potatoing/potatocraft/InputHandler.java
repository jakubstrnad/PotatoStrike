package net.potatoing.potatocraft;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class InputHandler implements KeyListener, MouseListener, MouseMotionListener{
	
	public boolean leftMouseDown = false, wDown = false, sDown = false, aDown = false, dDown = false, upDown = false, downDown = false, leftDown = false, rightDown = false, enterDown = false, spaceDown = false, oneDown = false, twoDown = false, threeDown = false;
	private boolean mouseWasDown = false, enterWasDown = false, spaceWasDown = false, leftWasDown = false, rightWasDown = false, upWasDown = false, downWasDown = false, leftMouseWasDown = false;
	public boolean mouseClicked = false, enterClicked = false, spaceClicked = false, leftClicked = false, rightClicked = false, upClicked = false, downClicked = false, leftMouseClicked = false;
	
	public int mouseX=0, mouseY=0;
	
	
	public InputHandler(Game game) {
		game.addKeyListener(this);
		game.addMouseListener(this);
		game.addMouseMotionListener(this);
	}
	
	public void tick() {
		mouseClicked = false;
		enterClicked = false;
		spaceClicked = false;
		leftClicked = false;
		rightClicked = false;
		upClicked = false;
		downClicked = false;
		leftMouseClicked = false;
		
		if(!leftMouseDown && mouseWasDown) {
			mouseClicked = true;
			mouseWasDown = false;
		}
		if(leftMouseDown)
			mouseWasDown = true;
		
		if(!enterDown && enterWasDown) {
			enterClicked = true;
			enterWasDown = false;
		}
		if(enterDown)
			enterWasDown = true;
		
		if(!spaceDown && spaceWasDown) {
			spaceClicked = true;
			spaceWasDown = false;
		}
		if(spaceDown)
			spaceWasDown = true;
		
		if(!leftDown && leftWasDown) {
			leftClicked = true;
			leftWasDown = false;
		}
		if(leftDown)
			leftWasDown = true;
		
		if(!rightDown && rightWasDown) {
			rightClicked = true;
			rightWasDown = false;
		}
		if(rightDown)
			rightWasDown = true;
		
		if(!upDown && upWasDown) {
			upClicked = true;
			upWasDown = false;
		}
		if(upDown)
			upWasDown = true;
		
		if(!downDown && downWasDown) {
			downClicked = true;
			downWasDown = false;
		}
		if(downDown)
			downWasDown = true;
		
		if(!leftMouseDown && leftMouseWasDown) {
			leftMouseClicked = true;
			leftMouseWasDown = false;
		}
		if(leftMouseDown)
			leftMouseWasDown = true;
	}

	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_W:
			wDown = true;
			break;
		case KeyEvent.VK_S:
			sDown = true;
			break;
		case KeyEvent.VK_A:
			aDown = true;
			break;
		case KeyEvent.VK_D:
			dDown = true;
			break;
		case KeyEvent.VK_LEFT:
			leftDown = true;
			break;
		case KeyEvent.VK_RIGHT:
			rightDown = true;
			break;
		case KeyEvent.VK_UP:
			upDown = true;
			break;
		case KeyEvent.VK_DOWN:
			downDown = true;
			break;
		case KeyEvent.VK_ENTER:
			enterDown = true;
			break;
		case KeyEvent.VK_SPACE:
			spaceDown = true;
			break;
		case KeyEvent.VK_1:
			oneDown = true;
			break;
		case KeyEvent.VK_2:
			twoDown = true;
			break;
		case KeyEvent.VK_3:
			threeDown = true;
			break;
		}
	}

	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_W:
			wDown = false;
			break;
		case KeyEvent.VK_S:
			sDown = false;
			break;
		case KeyEvent.VK_A:
			aDown = false;
			break;
		case KeyEvent.VK_D:
			dDown = false;
			break;
		case KeyEvent.VK_LEFT:
			leftDown = false;
			break;
		case KeyEvent.VK_RIGHT:
			rightDown = false;
			break;
		case KeyEvent.VK_UP:
			upDown = false;
			break;
		case KeyEvent.VK_DOWN:
			downDown = false;
			break;
		case KeyEvent.VK_ENTER:
			enterDown = false;
			break;
		case KeyEvent.VK_SPACE:
			spaceDown = false;
			break;
		case KeyEvent.VK_1:
			oneDown = false;
			break;
		case KeyEvent.VK_2:
			twoDown = false;
			break;
		case KeyEvent.VK_3:
			threeDown = false;
			break;
		}
	}

	public void keyTyped(KeyEvent e) {
		
	}

	public void mouseClicked(MouseEvent e) {
		
	}

	public void mouseEntered(MouseEvent e) {
		
	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1)
			leftMouseDown = true;
	}

	public void mouseReleased(MouseEvent e) {
		if(e.getButton() == MouseEvent.BUTTON1)
			leftMouseDown = false;
	}

	public void mouseDragged(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}

	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}
	

}
