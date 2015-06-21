package atlanteine;

import atlanteine.Game.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class AutoPlayer {
	private Cheater cheater;
	private Tracker tracker; // Pumpkin Tracker
	private Game current;
	private volatile int pumx, pumy;
	private Point pumpPos;
	private int w, h;
	private static final Color[] pumpkinColors = new Color[]{new Color(255, 106, 0),new Color(255, 222, 21),  new Color(255, 255, 63), new Color(255, 144, 0), new Color(195, 105, 21), new Color(246, 117, 19), new Color(255, 176, 15)};
	private static final int radius = 11;
	private Path path;
	private boolean play;
	
	public AutoPlayer(Cheater c) {
		cheater = c;
		w = cheater.areaSize.width / Game.GRID_WIDTH;
		h = cheater.areaSize.height / Game.GRID_HEIGHT;
		play = false;
		tracker = new Tracker();
		tracker.start();
		pumpPos = new Point();
	}
	
	public void newGame(Game g) {
		System.out.println("Game set");
		current = g;
		path = g.getBestPath();
		Point p = g.getPumpkin().getPos();
		synchronized (this) {
			pumx = p.x * w + w/2;
			pumy = p.y * h + h/2;
			pumpPos.setLocation(pumx, pumy);
			if (cheater.isAutoplaying()) {
				play = true;
				notify();
			}
		}
	}
	
	public synchronized void setPlaying(boolean b) {
		play = b;
		notify();
	}
	
	public boolean isPlaying() {
		return play;
	}
	
	private boolean isPumpkin(Color co) {
		for (Color c : pumpkinColors)
			if (Math.hypot(Math.hypot(co.getRed() - c.getRed(), co.getGreen() - c.getGreen()), co.getBlue() - c.getBlue()) < 42)
				return true;
		return false;
	}
	
	private class Tracker extends Thread {
		public Tracker() {
			setDaemon(true);
			setName("PumpkinTracker");
		}
		
		@Override
		public synchronized void run() {
			onEnterFrame();
		}
	}
	
	public void onEnterFrame() {
		int wait = 100;
		Robot r = cheater.getRobot();
		while (true) {
			try {
				Thread.sleep(100);
				synchronized (this) {
					while (! play)
						wait();
				}
				
				System.out.println("Starting playing");
				
				Point[] points = path.getPoints().toArray(new Point[path.getPoints().size()]);
				Direction[] dirs = path.getPath().toArray(new Direction[path.getPath().size()]);
				focusOnGame();
				int i;
				for (i = 0; i < dirs.length; i ++) {
					try {
						Thread.sleep(wait);
					} catch(InterruptedException e) {}
					if (r.getPixelColor(cheater.getAreaCorner().x, cheater.getAreaCorner().y).equals(Game.BORDER_COLOR)) {
						Point pos = points[i], screenpos = gridToArea(pos);
						Direction d = dirs[i];
						if (isPumpkin(r.getPixelColor(cheater.getAreaCorner().x + screenpos.x, cheater.getAreaCorner().y + screenpos.y))) {
							pumpPos.setLocation(pos);
							System.out.println("Going "+ d.name());
							r.keyPress(d.getKeyCode());
							r.delay(50);
							r.keyRelease(d.getKeyCode());
							wait = 100 * current.getDistanceTo(pos, d);
							cheater.getWindow().repaint();
						} else {
							System.out.println("Not at the right place. Ghost ? (or Bruce Willis). Color : ("+ cheater.getAreaCorner() +" + "+ screenpos +") : +"+ r.getPixelColor(cheater.getAreaCorner().x + screenpos.x, cheater.getAreaCorner().y + screenpos.y));
							play = false;
							break;
						}
					} else {
						break;
					}
				}
				if (play) {
					play = false;
					r.delay(500);
					cheater.waitForArea();
					r.delay(250);
				}
				r.delay(500);
				cheater.update();
				
			} catch (InterruptedException e) { }
		}
	}
	
	public Point areaToGrid(Point p) {
		return new Point(p.x / w, p.y / h);
	}
	
	public Point gridToArea(Point p) {
		return new Point(p.x * w + w/2, p.y * h + h/2);
	}
	
	public synchronized Point getPumpkinPosition() {
		return pumpPos;
	}
	
	public int onPumpkinStopped() {
		Point pos = areaToGrid(pumpPos);
		if (path.containsPoint(pos)) {
			Direction d = path.getDirectionAt(pos);
			focusOnGame();
			Robot r = cheater.getRobot();
			r.keyPress(d.getKeyCode());
			r.keyRelease(d.getKeyCode());
			Point stop = gridToArea(current.getPointAt(pos, d));
			pumx = stop.x;
			pumy = stop.y;	
			return 100 * current.getDistanceTo(pos, d);
		} else {
			play = false;
			cheater.update();
			return 100;
		}
	}
	
	public Point findPumpkin() {
		Robot r = cheater.getRobot();
		int px = pumx, py = pumy, dx = -1, dy = -1, n = 0;
		Point corner = cheater.getAreaCorner();
		while ((dx != 0 || dy != 0) && n < 10) {
			dx = dy = 0;
			if (r.getPixelColor(corner.x, corner.y).equals(Game.BORDER_COLOR) && isPumpkin(r.getPixelColor(corner.x + pumx, corner.y + pumy))) {
				for (int i = 0; i < 4; i ++) {
					int cos = (int) Math.cos(Math.PI * i / 2), sin = (int) Math.sin(Math.PI * i / 2);
				
					if (isPumpkin(r.getPixelColor(corner.x + pumx + cos * radius, corner.y + pumy + sin * radius))) {
						dx += cos * 5;
						dy += sin * 5;
					}
				
				}
			} else {
				System.out.println("Pumpkin not at its normal place.");
				BufferedImage area = cheater.getGameArea();
				if (area != null) {
					boolean found = false;
					for (int i = w * 3 / 2; i < w * Game.GRID_WIDTH - w/2 && ! found; i += 10)
						for (int j = h * 3 / 2; j < h * Game.GRID_HEIGHT - h/2; j += 10)
							if (isPumpkin(new Color(area.getRGB(i, j)))) {
								dx = i - px;
								dy = i - py;
								found = true;
								break;
							}
					if (! found) {
						System.out.println("Interrupting autoplaying (pumpkin not found)");
						return null;
					}
						
				} else {
					System.out.println("Interrupting autoplaying (area not found)");
					return null;
				}
			}
			px += dx;
			py += dy;
			n ++;
		}
		if (n == 10)
			throw new RuntimeException("Infinite pumpkin-finding loop ! Problem ? =D");
		System.out.println("Pumpkin found on : "+ px +":"+ py +", area is on : "+ corner.x +","+ corner.y);
		return new Point(px, py);
	}
	
	public void focusOnGame() {
		Point p = cheater.getAreaCorner();
		Robot r = cheater.getRobot();
		int button = java.awt.event.InputEvent.BUTTON1_MASK;
		
		r.mouseMove(p.x + 20, p.y + 20);
		r.mousePress(button);
		r.mouseRelease(button);
		r.mouseMove(p.x - 20, p.y - 20);
	}
	
}

