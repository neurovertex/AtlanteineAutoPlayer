package atlanteine;

import javax.swing.*;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.*;
import java.awt.event.*;

public class GameComponent extends JPanel implements MouseListener, MouseMotionListener {
	private int overx = -1, overy = -1;
	private Cheater cheater;
	private boolean showOriginal;
	private Point pumpPos;
	
	public GameComponent(Cheater c) {
		cheater = c;
		setPreferredSize(new Dimension(300, 300));
		addMouseListener(this);
		addMouseMotionListener(this);
		pumpPos = c.getPlayer().getPumpkinPosition();
	}
	
	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		if (cheater.getGame() != null) {
			BufferedImage img = (showOriginal) ? cheater.getGame().getGameArea() : cheater.getGame().getOverview();
			g.drawImage(img, 0, 0, null);
			if (! showOriginal) {
				g.setColor(Color.orange);
				g.fillOval(pumpPos.x - 11, pumpPos.y - 11, 22, 22);
			}
		} else {
			g.setColor(Color.red);
			g.drawLine(0, 0, getWidth()-1, getHeight()-1);
			g.drawLine(0, getHeight()-1, getWidth()-1, 0);
			g.setColor(Color.black);
			g.drawString("No game area found", 75, 50);
		}
	}
	
	@Override
	public void mouseMoved(MouseEvent e) {
		overx = e.getX();
		overy = e.getY();
		repaint();
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		overx = e.getX();
		overy = e.getY();
		repaint();
	}
	
	@Override
	public void mouseEntered(MouseEvent e) {
		overx = e.getX();
		overy = e.getY();
		repaint();
	}
	
	@Override
	public void mouseExited(MouseEvent e) {
		overx = overy = -1;
		repaint();
	}
	
	public void changeOverview(JButton b) {
		if (! showOriginal) {
			b.setText("Overview");
			showOriginal = true;
		} else {
			b.setText("Original");
			showOriginal = false;
		}
	}
	
	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			int x = (int) ((double)e.getX() * Game.GRID_WIDTH / cheater.getGame().getGameArea().getWidth());
			int y = (int) ((double)e.getY() * Game.GRID_HEIGHT / cheater.getGame().getGameArea().getHeight());
			System.out.println(x +":"+ y +":"+ cheater.getGame().getCaseAt(x, y).name());
		} else if (e.getButton() == MouseEvent.BUTTON3) {
			Color c = new Color(cheater.getGame().getGameArea().getRGB(e.getX(), e.getY()));
			System.out.println(e.getX() +":"+ e.getY() +" -> "+ c.getRed() +":"+ c.getGreen() +":"+ c.getBlue());
		} else {
			int x = (int) ((double)e.getX() * Game.GRID_WIDTH / cheater.getGame().getGameArea().getWidth()) * cheater.getGame().getGameArea().getWidth() / Game.GRID_WIDTH;
			int y = (int) ((double)e.getY() * Game.GRID_HEIGHT / cheater.getGame().getGameArea().getHeight()) * cheater.getGame().getGameArea().getHeight() / Game.GRID_HEIGHT;
			Color c = new Color(cheater.getGame().getOverview().getRGB(e.getX(), e.getY()));
			System.out.println(e.getX() +":"+ e.getY() +" -> "+ c.getRed() +":"+ c.getGreen() +":"+ c.getBlue());
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {}
	@Override
	public void mouseClicked(MouseEvent e) {}
	
}
