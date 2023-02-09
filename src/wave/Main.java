package wave;

import java.awt.Dimension;

import javax.swing.JFrame;

public class Main {
	
	public static JFrame frame;
	
	public static void main(String[] args) {
		
		frame = new JFrame("wave simulator");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		WaveCanvas wc = new WaveCanvas();
		wc.setPreferredSize(new Dimension(900, 900));
		
		frame.add(wc);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		wc.simStart();
		
	}
	
}
