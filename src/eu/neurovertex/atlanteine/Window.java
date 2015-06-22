package eu.neurovertex.atlanteine;

import javax.swing.*;
import java.awt.*;

public class Window extends JFrame {
	private Cheater cheater;
	private GameComponent comp;
	
	public Window(Cheater c) {
		super("Atlanteine resolver - by Neurovertex");
		
		cheater = c;
		comp = new GameComponent(c);
		add(comp);
		
		JToolBar buttons = new JToolBar(JToolBar.HORIZONTAL);
		
		JButton update = new JButton("update");
		update.addActionListener(e -> cheater.update());
		buttons.add(update);
		
		JButton play = new JButton("play");
		play.addActionListener(e -> {
			cheater.update();
			cheater.getPlayer().setPlaying(! cheater.getPlayer().isPlaying());
		});
		buttons.add(play);
		
		JButton autoplay = new JButton("autoplay");
		autoplay.addActionListener(e -> {
			cheater.update();
			cheater.setAutoplay(true);
		});
		buttons.add(autoplay);
		
		JButton toggleOverview = new JButton("view original");
		toggleOverview.addActionListener(e -> {
			comp.changeOverview((JButton) e.getSource());
			repaint();
		});
		buttons.add(toggleOverview);
		
		add(buttons, BorderLayout.NORTH);
		
		pack();
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setVisible(true);
	}
	
	public void update() {
		repaint();
	}
}
