package atlanteine;

import java.awt.Robot;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Color;

public class Cheater {
	private Robot robot;
	private AutoPlayer player;
	private Game game;
	private boolean found = false, autoplay = false;
	private Window window;
	private int areax, areay;
	public static final Dimension areaSize = new Dimension(300, 300);
	
	public static void main(String[] args) {new Cheater();}
	
	public Cheater() {
		try {
			robot = new Robot();
			robot.setAutoDelay(20);
		} catch(AWTException e) {
			System.out.println("Votre système n'autorise pas la prise de contrôle de la souris. Vous l'avez dans l'OS ");
			System.exit(0);
		}
		player = new AutoPlayer(this);
		window = new Window(this);
	}
	
	public Robot getRobot() {
		return robot;
	}
	
	public AutoPlayer getPlayer() {
		return player;
	}
	
	public Game getGame() {
		return game;
	}
	
	public Window getWindow() {
		return window;
	}
	
	/*public PathDisplayer getPathDisplayer() {
		return pathDisplayer;
	}*/
	
	public void update() {
		game = null;
		for (int i = 0; i < 5 && (game == null || game.getBestPath() == null); i ++)
			try {
				game = new Game(this);
			} catch (Exception ex) {
				System.out.println("Exception in game analysis : ");
				if (ex instanceof Game.PumpkinNotFoundException)
					System.out.println("Pumpkin not found at attempt "+ i +".");
				else if (ex instanceof Game.GameAreaNotFoundException)
					System.out.println("Game area not found at attempt "+ i +".");
				else
					ex.printStackTrace();
				try {
					Thread.sleep(250);
				} catch (InterruptedException ignored) {}
			}
		window.update();
	}
	
	public Point getAreaCorner() {
		return new Point(areax, areay);
	}
	
	public void setAutoplay(boolean b) {
		autoplay = b;
		if (b && ! player.isPlaying())
			player.setPlaying(true);
	}
	
	public boolean isAutoplaying() {
		return autoplay;
	}
	
	public void waitForArea() {
		while (getGameArea() == null) {
			try {
				Thread.sleep(100);
			} catch(InterruptedException ignored) { }
		}
		try {
			Thread.sleep(1000);
		} catch(InterruptedException ignored) { }
	}
	
	public BufferedImage getGameArea() {
		Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
        BufferedImage bi = robot.createScreenCapture(new Rectangle(size.width, size.height));
        
		areax = areay = -1;
		for (int i = 0; i < size.width - (areaSize.width -1) && areax == -1; i ++)
			for (int j = 0; j < size.height - (areaSize.height -1) && areay == -1; j ++)
				if ((new Color(bi.getRGB(i, j))).equals(Game.BORDER_COLOR)) {
					areax = i;
					areay = j;
				}
		
		if (areax != -1 && areay != -1) {
			System.out.println("Game area found at : "+ areax +","+ areay);
			return robot.createScreenCapture(new Rectangle(areax, areay, areaSize.width, areaSize.height));
		} else {
			System.out.println("Game area not found");
			return null;
		}
	}
}
