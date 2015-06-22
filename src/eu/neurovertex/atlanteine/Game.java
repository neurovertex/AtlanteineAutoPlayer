package eu.neurovertex.atlanteine;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class Game {
	
	public static final Color BORDER_COLOR = new Color(78, 138, 143), PUMPKIN_HALO = new Color(255, 147, 0), BORDER_REPLACE_COLOR = new Color(77, 137, 142); // border replace color to avoid self-detection when scanning for the game area
	public static final int GRID_WIDTH = 15, GRID_HEIGHT = 15;
	
	public enum Elements {
		pumpkin(false, new Color(250, 157, 28), new Color(249, 114, 15)),
		box(true, new Color(179, 143, 71), new Color(171, 147, 92)),
		nether(false, BORDER_COLOR, new Color(77, 125, 128)),
		rock(true, new Color(97, 99, 80), new Color(83, 91, 85), new Color(55, 62, 56), new Color(69, 78, 71), new Color(110, 117, 72)),
		floor(true, new Color(175, 186, 175), new Color(182, 183, 151), new Color(158, 174, 171)),
		asshole(88, 118, 121, false),
		portal(255, 255, 255, false),
		ghost(233, 241, 233, false),
		none(0, 0, 0, false);
		
		private Color[] color;
		private boolean stop;
		Elements(boolean s, Color... c){ color = c; stop = s; }
		Elements(Color c, boolean s){ color = new Color[]{c}; stop = s; }
		Elements(int r, int g, int b, boolean s) { this(s, new Color(r, g, b));}
		public double colorDist(Color c) {
			double min = Double.MAX_VALUE;
			
			for (Color cl : color) {
				double dist = Math.hypot(Math.hypot(c.getRed() - cl.getRed(), c.getGreen() - cl.getGreen()), c.getBlue() - cl.getBlue());
				if (dist < min)
					min = dist;
			}
			
			return min;
		}
		public Color getColor() { return color[0]; }
		public boolean doStop() { return stop; }
		public static Elements getNearest(Color c) {
			Elements toi = null;
			for (Elements e : values()) {
				if (toi == null || e.colorDist(c) < toi.colorDist(c))
					toi = e;
			}
			return toi; // \o/
		}
		
	}
	
	private BufferedImage overview;
	private BufferedImage gameArea;
	private Elements[][] grid;
	private Pumpkin pump;
	private int boxes;
	private Path bestPath;
	
	public BufferedImage getOverview(boolean background) {
		if (! background || overview == null)
			return overview;
		else {
			BufferedImage ret = new BufferedImage(gameArea.getWidth(), gameArea.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = ret.createGraphics();
			g.drawImage(gameArea, 0, 0, null);
			g.drawImage(overview, 0, 0, null);
			for (int j = 0; j < gameArea.getWidth(); j ++)
				for (int i = 0; i < gameArea.getHeight(); i ++)
					if ((new Color(ret.getRGB(i, j))).equals(BORDER_COLOR))
						ret.setRGB(i, j, BORDER_REPLACE_COLOR.getRGB());
					
			return ret;
		}
	}
	
	public BufferedImage getOverview() {
		return getOverview(true);
	}
	
	public BufferedImage getGameArea() {
		return gameArea;
	}
	
	public Path getBestPath() {
		return bestPath;
	}
	
	public Pumpkin getPumpkin() {
		return pump;
	}
	
	public Game(Cheater c) {
		grid = new Elements[GRID_HEIGHT][GRID_WIDTH];
		gameArea = c.getGameArea();
		if (gameArea != null) {
			final int rad = 5;
			overview = new BufferedImage(gameArea.getWidth(), gameArea.getHeight(), BufferedImage.TYPE_INT_ARGB);
			Graphics g = overview.createGraphics();
			g.setColor(new Color(0, 0, 0, 0));
			g.fillRect(0, 0, overview.getWidth() - 1, overview.getHeight() - 1);
			double w = gameArea.getWidth() / GRID_WIDTH;
			double h = gameArea.getHeight() / GRID_HEIGHT;
			g.setColor(Color.black);
			
			for (int j = 1; j < GRID_HEIGHT; j ++) {
				g.drawLine(0, (int)(j * h), gameArea.getWidth()-1, (int)(j * h));
			}
			for (int i = 1; i < GRID_WIDTH; i ++) {
				g.drawLine((int)(i * w), 0, (int)(i * w), gameArea.getHeight()-1);
			}
			
			for (int j = 0; j < GRID_HEIGHT; j ++)
				for (int i = 0; i < GRID_WIDTH; i ++) {
					if (grid[j][i] == null) {
						Color pix = getAverageColor(gameArea, (int)((i + 0.5) * w), (int)((j + 0.5) * h), rad);
						Elements e = Elements.getNearest(pix);
						g.setColor(pix);
						g.fillOval((int)((i + 0.5) * w) - rad, (int)((j + 0.5) * h) - rad, 2*rad+1, 2*rad+1);
					
						if (i == GRID_WIDTH -2 && j == GRID_HEIGHT - 2) {
							if (Elements.pumpkin.colorDist(pix) < 42)
								pump = new Pumpkin(i, j);
							e = Elements.floor;
						}
						else if (i == GRID_WIDTH - 1 || i == 0 || j == GRID_HEIGHT - 1|| j == 0)
							e = Elements.nether;
						else {
							if (e.colorDist(pix) > 42) {
								e = Elements.none;
								System.out.println("Unreconized color : "+ i +":"+ j +" -> "+ pix.getRed() +":"+ pix.getGreen() +":"+ pix.getBlue());
							}
							if (e == Elements.pumpkin) {
								e = Elements.floor;
								pump = new Pumpkin(i, j);
								for (Direction d : Direction.values()) {
									int i2 = i + d.dx();
									int j2 = j + d.dy();
									Color pix2 = getAverageColor(gameArea, (int)((i2 + 0.5) * w), (int)((j2 + 0.5) * h), rad);
									double r, gr, b, o = 0.175;
									
									r = (pix2.getRed() - PUMPKIN_HALO.getRed() * o) / (1 - o);
									r = (r < 0) ? 0 : (r > 255) ? 255 : r;
									gr = (pix2.getGreen() - PUMPKIN_HALO.getGreen() * o) / (1 - o);
									gr = (gr < 0) ? 0 : (gr > 255) ? 255 : gr;
									b = (pix2.getBlue() - PUMPKIN_HALO.getBlue() * o) / (1 - o);
									b = (b < 0) ? 0 : (b > 255) ? 255 : b;
									
									Color result = new Color((int)r, (int)gr, (int)b);
									Elements nearest = Elements.getNearest(result);
									if (nearest.colorDist(result) < 21) {
										grid[j2][i2] = nearest;
									}
								}
							}
							else if (e == Elements.ghost) {
								boolean portal = true;
								double xradius = w * 2/5, yradius = h * 2/5;
								for (int agl = 0; agl < 16; agl ++) {
									int x = (int)((i + 0.5) * w + xradius*Math.cos(agl*Math.PI/8)), y = (int)((j + 0.5) * h + yradius*Math.sin(agl*Math.PI/8));
									Color co = new Color(gameArea.getRGB(x, y));
									double dist = Elements.floor.colorDist(co);
									if (dist > 42) {
										portal = false;
										break;
									}
								}
								if (! portal) { // IT'S A TRAP !
									System.out.println("Ghost on "+ i +":"+j);
									e = Elements.floor;
								} else {
									System.out.println("Portal on "+ i +":"+j);
									g.drawOval((int)((i + 0.1) * w), (int)((j + 0.1) * h), (int)(w*0.8), (int)(h*0.8));
									e = Elements.portal;
								}
							}
						}
						g.setColor(Color.black);
						if (e == Elements.box)
							g.drawRect((int)((i + 0.1) * w), (int)((j + 0.1) * h), (int)(w*0.8), (int)(h*0.8));
						else if (e == Elements.rock) {
							g.drawLine((int)((i + 0.1) * w), (int)((j + 0.1) * h), (int)((i + 0.9) * w), (int)((j + 0.9) * h));
							g.drawLine((int)((i + 0.1) * w), (int)((j + 0.9) * h), (int)((i + 0.1) * w), (int)((j + 0.9) * h));
						}
						grid[j][i] = e;
					}
				}
			if (pump == null)
				throw new PumpkinNotFoundException();
			bestPath = findBestPath();
			if (bestPath != null) {
				bestPath.display(g);
				if (c.getPlayer() != null)
					c.getPlayer().newGame(this);
			} else
				System.out.println("OMAGAD ! PATH NOT FOUND !");
		} else
			throw new GameAreaNotFoundException();
	}
	
	public Color getAverageColor(BufferedImage img, int x, int y, int rad) {
		int r = 0, g = 0, b = 0, p = 0;
		
		for (int i = x-rad; i <= x+rad; i ++)
			for (int j = y-rad; j <= y+rad; j ++)
				if (i >= 0 && i < img.getWidth() && j >= 0 && j < img.getHeight() && Math.hypot(i - x, j - y) <= rad) {
					Color c = new Color(img.getRGB(i, j));
					r += c.getRed();
					g += c.getGreen();
					b += c.getBlue();
					p ++;
				}
		p += (p == 0) ? 1 : 0;
		r /= p;
		g /= p;
		b /= p;
		return new Color(r, g, b);
	}
	
	public Point getOtherPortal(Point p) {
		for (int j = 1; j < GRID_HEIGHT -1; j ++)
			for (int i = 1; i < GRID_WIDTH -1; i ++)
				if (getCaseAt(i, j) == Elements.portal && (i != p.x || j != p.y))
					return new Point(i, j);
		System.out.println("ONOES ONLY 1 PORTAL");
		return p;
	}
	
	public Elements getCaseAt(int i, int j) {
		try {
			return grid[j][i];
		} catch (ArrayIndexOutOfBoundsException e) {
			return Elements.none;
		}
	}
	
	public Elements getCaseAt(Point p) { return getCaseAt(p.x, p.y); }
	
	
	public Path findBestPath() {
		Path best = null;
		for (Direction d : Direction.values()) {
			Point stop = getPointAt(pump.getPos(), d);
			if (stop != null) {
				Path p = new Path();
				p.addPoint(pump.getPos());
				p.addDir(d);
				p = findPath(new Pumpkin(stop), p);
				if (p != null && p.isBetterThan(best))
					best = p;
			}
		}
		return best;
	}
	
	private Path findPath(Pumpkin pum, Path pa) {
		Path best = null;
		pa.addPoint(pum.getPos());
		for (Direction d : Direction.getSideDirs(pa.getLastDir())) {
			Point stop = getPointAt(pum.getPos(), d);
			if (stop != null && ! pa.containsPoint(stop) && ! stop.equals(pum.getPos())) {
				Path p = new Path(pa);
				p.addDir(d);
				if (getCaseAt(stop) == Elements.asshole) {
					p.addPoint(stop);
					return p;
				}
				else {
					Pumpkin newPum = new Pumpkin(stop);
					Path r = findPath(newPum, p);
					if (r != null && r.isBetterThan(best))
						best = r;
				}
			}
		}
		
		if (! pa.hasMovedBox())
			for (Direction d : Direction.values())
				if (getCaseAt(pum.x + d.dx(), pum.y + d.dy()) == Elements.box && getCaseAt(pum.x + d.dx()*2, pum.y + d.dy()*2) == Elements.floor) {
					Path p = new Path(pa);
					p.setBoxMoved();
					grid[pum.y + d.dy()*2][pum.x + d.dx()*2] = Elements.box;
					grid[pum.y + d.dy()][pum.x + d.dx()] = Elements.floor;
					p.addDir(d);
					Pumpkin newPum = new Pumpkin(new Point(pum.x + d.dx(), pum.y + d.dy()));
					p = findPath(newPum, p);
					if (p != null && (best == null || p.isBetterThan(best)))
						best = p;
					grid[pum.y + d.dy()][pum.x + d.dx()] = Elements.box;
					grid[pum.y + d.dy()*2][pum.x + d.dx()*2] = Elements.floor;
				}
		return best;
	}
	
	public Point getPointAt(Point start, Direction d) {
		Point p = new Point(start);
		int dx = d.dx(), dy = d.dy();
		while (getCaseAt(p.x + dx, p.y + dy) == Elements.floor) {
			p.translate(dx, dy);
		}
		
		if (getCaseAt(p.x + dx, p.y + dy) == Elements.portal) {
			Point port = getOtherPortal(new Point(p.x + dx, p.y + dy));
			Point ret;
			if (! port.equals(start))
				ret = getPointAt(port, d);
			else
				ret = null;
			return ret;
		} else if (getCaseAt(p.x + dx, p.y + dy) == Elements.asshole) {
			return new Point(p.x + dx, p.y + dy);
		} else if (getCaseAt(p.x + dx, p.y + dy).doStop())
			return p;
		else
			return null;
	}
	
	public int getDistanceTo(Point start, Direction d) {
		Point p = new Point(start);
		int dist = 0;
		int dx = d.dx(), dy = d.dy();
		while (getCaseAt(p.x + dx, p.y + dy) == Elements.floor) {
			p.translate(dx, dy);
			dist ++;
		}
		
		if (getCaseAt(p.x + dx, p.y + dy) == Elements.portal)
			return dist + getDistanceTo(getOtherPortal(new Point(p.x + dx, p.y + dy)), d) + 1;
		else if (getCaseAt(p.x + dx, p.y + dy).doStop() || getCaseAt(p.x + dx, p.y + dy) == Elements.asshole)
			return dist;
		else
			return -1;
	}
	
	
	public enum Direction {
		east(0, KeyEvent.VK_RIGHT), south(1, KeyEvent.VK_DOWN), west(2, KeyEvent.VK_LEFT), north(3, KeyEvent.VK_UP);
		
		private final int[] ARROW_X = new int[]{9, 6, 6, 2, 2, 6, 6};
		private final int[] ARROW_Y = new int[]{5, 2, 4, 4, 6, 6, 8};
		
		private int angle;
		private int keyCode;
		Direction(int d, int c) { angle = d; keyCode = c; }
		public double getAngle() { return Math.PI/2 * angle;}
		public int getKeyCode() { return keyCode;}
		public int dx() { return cos(); }
		public int dy() { return sin(); }
		public int cos() { return (int) Math.cos(getAngle()); }
		public int sin() { return (int) Math.sin(getAngle()); }
		
		public Polygon getArrowPolygon(int w, int h) {
			int n = ARROW_X.length;
			int polyx[] = new int[n], polyy[] = new int[n];
			for (int j = 0; j < n; j ++) {
				int px, py;
				px = (ARROW_X[j] - 5) * cos() - (ARROW_Y[j] - 5) * sin() + 5;
				py = (ARROW_Y[j] - 5) * cos() + (ARROW_X[j] - 5) * sin() + 5;
				polyx[j] = px * w / 10;
				polyy[j] = py * h / 10;
			}
			return new Polygon(polyx, polyy, n);
		}
		
		public static Direction[] getSideDirs(Direction d) {
			return (d.angle % 2 == 0 ? new Direction[]{north, south} : new Direction[]{east, west});
		}
	}
	
	public class Pumpkin {
		private int x, y;
		
		public Pumpkin(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public Pumpkin(Point p) {
			this.x = p.x;
			this.y = p.y;
		}
		
		public Point getPos() { return new Point(x, y); }
		
	}
	
	public class Path {
		private final Color ARROW_COLOR = Color.black;
		private ArrayList<Direction> path;
		private ArrayList<Point> points;
		private boolean boxmoved;
		
		public Path() { path = new ArrayList<>(); points = new ArrayList<>(); boxmoved = false; }
		
		public Path(ArrayList<Direction> ds, ArrayList<Point> a, boolean b) {
			this();
			points.addAll(a.stream().collect(Collectors.toList()));
			path.addAll(ds.stream().collect(Collectors.toList()));
			boxmoved = b;
		}
		
		public Path(Path p) { this(p.path, p.points, p.boxmoved); }
		
		public Point getLastPoint() { return (points.size() > 0) ? points.get(points.size()-1) : null; }
		public Direction getLastDir() { return (path.size() > 0) ? path.get(path.size()-1) : null; }
		public ArrayList<Point> getPoints() { return points; }
		public ArrayList<Direction> getPath() { return path; }
		public boolean hasMovedBox() { return boxmoved; }
		public void setBoxMoved() { boxmoved = true; }
		public void addPoint(Point p) { points.add(p); }
		public void addDir(Direction d) { path.add(d); }
		
		public boolean containsPoint(Point pt) {
			for (Point p : points)
				if (p.equals(pt))
					return true;
			return false;
		}
		
		public void display(Graphics g) {
			g.setColor(Color.red);
			int w = gameArea.getWidth() / Game.GRID_WIDTH, h = gameArea.getHeight() / Game.GRID_HEIGHT;
			for (int i = 0; i < path.size(); i ++) {
				Point p = points.get(i);
				Direction d = path.get(i);
				int x = p.x * w, y = p.y * h;
				Polygon poly = d.getArrowPolygon(w, h);
				poly.translate(x, y);
				g.setColor(ARROW_COLOR);
				g.fillPolygon(poly);
			}
		}
		
		public int getDist() {
			return 0;
		}
		
		public Direction getDirectionAt(Point p) {
			for (int i = 0; i < path.size(); i ++)
				if (points.get(i).equals(p))
					return path.get(i);
			return null;
		}
		
		public boolean isBetterThan(Path p) {
			if (p == null)
				return true;
			else if (boxmoved != p.boxmoved)
				return ! boxmoved;
			else if (getDist() != p.getDist())
				return getDist() < p.getDist();
			else
				return path.size() < p.path.size();
		}
	}
	
	public class PumpkinNotFoundException extends RuntimeException {
		public PumpkinNotFoundException() {
			super("Pumpkin not detected in game area.");
		}
	}
	
	public class GameAreaNotFoundException extends RuntimeException {
		public GameAreaNotFoundException() {
			super("Game area not detected.");
		}
	}
	
}
