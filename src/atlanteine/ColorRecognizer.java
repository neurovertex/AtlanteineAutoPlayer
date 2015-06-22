package atlanteine;

import javax.swing.*;
import java.awt.*;
import java.text.NumberFormat;

/**
 * I have no fucking clue what that class was for exactly. All I know is that "ColorDecrypter" couldn't have been a
 * good name for it.
 */
public class ColorRecognizer extends JFrame {
	
	private JTextField red, green, blue, oraRed, oraGreen, oraBlue;
	private JSlider opacity;
	private JPanel original, orange, result;
	
	public static void main(String[] args) {
		new ColorRecognizer();
	}
	
	public ColorRecognizer() {
		
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = gbc.weighty = gbc.gridwidth = gbc.gridheight = 1;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		red = new JFormattedTextField(NumberFormat.getIntegerInstance());
		red.setMargin(new Insets(5, 5, 5, 5));
		green = new JFormattedTextField(NumberFormat.getIntegerInstance());
		green.setMargin(new Insets(5, 5, 5, 5));
		blue = new JFormattedTextField(NumberFormat.getIntegerInstance());
		blue.setMargin(new Insets(5, 5, 5, 5));
		oraRed = new JFormattedTextField(NumberFormat.getIntegerInstance());
		oraRed.setMargin(new Insets(5, 5, 5, 5));
		oraGreen = new JFormattedTextField(NumberFormat.getIntegerInstance());
		oraGreen.setMargin(new Insets(5, 5, 5, 5));
		oraBlue = new JFormattedTextField(NumberFormat.getIntegerInstance());
		oraBlue.setMargin(new Insets(5, 5, 5, 5));
		
		gbc.gridx = 0;
		gbc.gridy = 1;
		add(red, gbc);
		
		gbc.gridx = 1;
		add(green, gbc);
		
		gbc.gridx = 2;
		add(blue, gbc);
		
		gbc.gridx = 0;
		gbc.gridy = 2;
		add(oraRed, gbc);
		
		gbc.gridx = 1;
		add(oraGreen, gbc);
		
		gbc.gridx = 2;
		add(oraBlue, gbc);
		
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridy = 0;
		
		original = new JPanel();
		gbc.gridx = 0;
		add(original, gbc);
		
		orange = new JPanel();
		gbc.gridx = 1;
		add(orange, gbc);
		
		result = new JPanel();
		gbc.gridx = 2;
		add(result, gbc);
		
		opacity = new JSlider(JSlider.HORIZONTAL, 10, 90, 50);
		opacity.setMajorTickSpacing(10);
		opacity.setMinorTickSpacing(1);
		opacity.setPaintTicks(true);
		opacity.setPaintLabels(true);
		opacity.addChangeListener(e -> update());
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		add(opacity, gbc);
		
		JButton up = new JButton("update");
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridy = 4;
		gbc.gridx = 0;
		gbc.gridwidth = 3;
		up.addActionListener(e -> update());
		add(up, gbc);
		
		setPreferredSize(new Dimension(300, 300));
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		pack();
		setVisible(true);
		
	}
	
	private void update() {
		int newR, newG, newB, oraR, oraG, oraB;
		double r, g, b;
		newR = getValue(red);
		newG = getValue(green);
		newB = getValue(blue);
		oraR = getValue(oraRed);
		oraG = getValue(oraGreen);
		oraB = getValue(oraBlue);
		double o = opacity.getValue() / 100.0;
		
		r = (newR - oraR * o) / (1 - o);
		r = (r < 0) ? 0 : r;
		
		g = (newG - oraG * o) / (1 - o);
		g = (g < 0) ? 0 : g;
		
		b = (newB - oraB * o) / (1 - o);
		b = (b < 0) ? 0 : b;
		
		System.out.println(newR +":"+ newG +":"+ newB +" - "+ oraR +":"+ oraG +":"+ oraB +" * "+ o +" -> "+ r +":"+ g +":"+ b);
		
		original.setBackground(new Color(newR, newG, newB));
		orange.setBackground(new Color(oraR, oraG, oraB));
		result.setBackground(new Color((int)r, (int)g, (int)b));
	}
	
	public int getValue(JTextField f) {
		try {
			return Integer.parseInt(f.getText());
		} catch (NumberFormatException e) {
			f.setText("0");
			return 0;
		}
	}
	
}
