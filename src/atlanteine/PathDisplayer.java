package atlanteine;

import java.awt.*;
import javax.swing.*;
import java.util.ArrayList;
import atlanteine.Game.Direction;
import atlanteine.Game.Path;

public class PathDisplayer extends JFrame {
		
	private Cheater cheater;
	private static GraphicsConfiguration config = getTranslucentCapableConfig();
	/*private JFrame[][] frames;
	private int w, h, areax, areay;*/

	public PathDisplayer(Cheater c) {
		super(config);
		System.out.println(config +":"+ AWTUtilitiesWrapper.isTranslucencyCapable(config));
		cheater = c;
		setUndecorated(true);
		setAlwaysOnTop(true);
		setContentPane(new PathPanel());
		setBackground(new Color(0f, 0f, 0f, 0.5f));
		
		/*if (config != null)
		frames = new JFrame[Game.GRID_HEIGHT][Game.GRID_WIDTH];
		for (int j = 0; j < Game.GRID_HEIGHT; j ++)
			for (int i = 0; i < Game.GRID_WIDTH; i ++) {
				frames[j][i] = new PathWindow(i, j, config);
			}*/
		
//		AWTUtilitiesWrapper.setWindowOpaque(this, false);
		AWTUtilitiesWrapper.setWindowOpacity(this, 0.5f);
		pack();
	}
	
	private static GraphicsConfiguration getTranslucentCapableConfig() {
		GraphicsConfiguration config = null;
		for (GraphicsConfiguration gc : GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getConfigurations())
			if (AWTUtilitiesWrapper.isTranslucencyCapable(gc))
				config = gc;
		return config;
	}
	
	public void showWindow(Path path) {
		Point pos = cheater.getAreaCorner();
		repaint();
		setVisible(true);
		/*if (AWTUtilitiesWrapper.isTranslucencySupported(AWTUtilitiesWrapper.TRANSLUCENT)) {
			for (int j = 0; j < frames.length; j ++)
				for (int i = 0; i < frames[0].length; i ++)
					frames[j][i].hideWindow();
			for (int i = 0; i < path.getPath().size(); i ++) {
				Point point = path.getPoints().get(i);
				PathWindow win = frames[point.y][point.x];
				win.setShape(path.getPath().get(i));
				win.showWindow();
			}
		} else {
			System.out.println("Transparency not supported !");
		}*/
	}

	public void hideWindow() {
		setVisible(false);
	}
	/*public class PathWindow extends JFrame {
		private int x, y;
		
		public PathWindow(int x, int y, GraphicsConfiguration config) {
			super(config);
			this.x = x;
			this.y = y;
			setAlwaysOnTop(true);
			setUndecorated(true);
			setBackground(new Color(255, 255, 255, 0));
		}
		
		public void showWindow() {
			setLocation(x * w + areax, y * h + areay);
			setVisible(true);
		}
		
		public void hideWindow() {
			setVisible(false);
		}
		
		public setShape(Direction d) {
			Polygon p = d.getArrowPolygon();
			AWTUtilitiesWrapper.setWindowShape(this, p);
		}
		*/
		private class PathPanel extends JPanel {
			public PathPanel() {
				setBackground(new Color(0f, 0f, 0f));
				setPreferredSize(new Dimension(302, 302));
			}
		
			public void paintComponent(Graphics g) {
				System.out.println("Repainting");
				g.setColor(Color.black);
				if (cheater.getGame() != null)
					cheater.getGame().getBestPath().display(g);
				else
					hideWindow();
			}
		}
	
	//}
}
