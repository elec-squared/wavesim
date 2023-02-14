package wave;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
//import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

import javax.swing.JComponent;

public class WaveCanvas extends JComponent {

	private static final long serialVersionUID = -1607505678996020221L;
	
	// TODO: UPDATE GITHUB
//	public float[][] dotCoords = new float[8000][2];
//	public float[] dotOpacity = new float[8000];
//	public float[][] dotVelocity = new float[8000][2];
	public ArrayList<WavePoint> points = new ArrayList<WavePoint>();
	final int maxRender = 30000;
	final int deploy = 1000;
	public int dotSize = 15;
	public int dotMove = 2;
	
	public boolean shouldRun = true;
	public boolean isDrawingLimited = false;
	public Thread currentThr;
	public final Runnable thrRun = new Runnable() {
		public void run() {
			while (shouldRun) {
				
				long startMs = System.currentTimeMillis();
				
//				System.out.println("cycleCount = " + ++cycleCount);
				
				WavePoint wp;
				for (int i = 0; i < points.size(); i++) {
					if (points.size() <= 0) return;
					wp = points.get(i);
					if (wp == null) return;
					wp.xPos += wp.xVel;
					wp.yPos += wp.yVel;
					
//					for (int j = 0; j < 2; j++) {
//						if (dotCoords[i][j] < 0 && dotVelocity[i][j] < 0) {
//							dotVelocity[i][j] *= -0.8;
//							dotOpacity[i] -= 0.1;
//							hitCount++;
//						}
//						
//						int max = (j>0 ? getHeight() : getWidth());
//						
//						if (dotCoords[i][j] > max && dotVelocity[i][j] > 0) {
//							dotVelocity[i][j] *= -0.8;
//							dotOpacity[i] -= 0.1;
//							hitCount++;
//						}
//					}
					
					if ((wp.xPos > getWidth() && wp.xVel > 0)
							|| (wp.xPos < 0 && wp.xVel < 0)) {
						wp.xVel *= -0.8;
						wp.opacity -= 0.1;
						hitCount++;
					}
					if ((wp.yPos > getHeight() && wp.yVel > 0)
							|| (wp.yPos < 0 && wp.yVel < 0)) {
						wp.yVel *= -0.8;
						wp.opacity -= 0.1;
						hitCount++;
					}
					
				}
				repaint();
				cycleCount++;
				
				try {
					
					long elapsed = (System.currentTimeMillis() - startMs);
					long toWait = cycleMs - elapsed;
					if (toWait < 0) toWait = 0;
					Thread.sleep(toWait);
					
					elapsed = (System.currentTimeMillis() - startMs);
					elapsedAvg.add((double)elapsed);
					while (elapsedAvg.size() > 40) elapsedAvg.remove(0);
					
					double eAvg = 0;
					for (int i = 0; i < elapsedAvg.size(); i++) {
						eAvg += elapsedAvg.get(i);
					}
					eAvg /= elapsedAvg.size();
					
//					System.out.println(elapsedAvg);
					fps = 1000d / eAvg;
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			};
		};
	};
		
	public int cycleMs = 5;
	public long cycleCount = 0;
	public long restartCount = 0;
	
	// counters
	public int hitCount = 0;
	public double fps = 0d;
	public ArrayList<Double> elapsedAvg = new ArrayList<Double>(5);
	
	// physics
	public void generateAtPoint(int x, int y) {
		
//		hitCount = 0; Not since the infinite dot addition
		
		for (int i = 0; i < deploy; i++) {
			WavePoint newWp = new WavePoint(x, y);
			
			float rad = (float) (Math.random() * Math.PI*2);
			newWp.xVel = (float)(Math.cos(rad)*dotMove);
			newWp.yVel = (float)(Math.sin(rad)*dotMove);
			newWp.opacity = 1f;
			points.add(newWp);
		}
		
	}
	
	public void randomGen() {
		
//		hitCount = 0; Not since the infinite dot addition
		
		// generate random dots just to test dot functionality
		for (int i = 0; i < deploy; i++) {
			float x = (float)(Math.random() * getWidth());
			float y = (float)(Math.random() * getHeight());
			WavePoint wp = new WavePoint(x, y);
			
			float rad = (float) (Math.random() * Math.PI*2);
			wp.xVel = (float)(Math.cos(rad)*dotMove);
			wp.yVel = (float)(Math.sin(rad)*dotMove);
			
			wp.opacity = 1.0f;
			points.add(wp);
		}
		
	}
	
	public void simStart() {
		
		requestFocusInWindow();
		addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				
				Point loc = MouseInfo.getPointerInfo().getLocation();
				loc.move(
						loc.x-getLocationOnScreen().x, loc.y-getLocationOnScreen().y);
				
				if (e.getKeyCode() == KeyEvent.VK_R) randomGen();
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					points.clear();
					hitCount = 0;
				}
				if (e.getModifiersEx() == KeyEvent.SHIFT_DOWN_MASK) {
					loc.x = getWidth()/2;
					loc.y = getHeight()/2;
				}
				if (e.getKeyCode() == KeyEvent.VK_SPACE) generateAtPoint(loc.x, loc.y);
			}

			@Override
			public void keyReleased(KeyEvent e) {}
			
		});
		
		randomGen();
		
		currentThr = new Thread(thrRun);
		currentThr.setName("exec");
		currentThr.start();
		
		new Thread("checkAlive") {
			public void run() {
				while (true) {
					if (!currentThr.isAlive() || currentThr.isInterrupted()) {
						System.err.println("Thread restart " + ++restartCount);
						System.err.println(currentThr.getState().toString());
						
						currentThr = new Thread(thrRun);
						currentThr.setName("exec");
						currentThr.start();
					}
					try {
						Thread.sleep(cycleMs*20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
	}
	
	public void simUpdate() {
		
	}
	
	@Override
	public void paintComponent(Graphics g) {
		
		Graphics2D g2 = (Graphics2D)g;
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//				RenderingHints.VALUE_ANTIALIAS_ON);
//		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//				RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_HRGB);
		
		g2.setColor(isDrawingLimited ? new Color(1f, 0.5f, 0.5f) : getBackground());
		g2.fillRect(0,0,getWidth(),getHeight());
		
		// draw each dot (Separate from dot generation loop since that is just a placeholder)
		// in reverse order to draw latest dots and omit oldest dots
		isDrawingLimited = false;
		WavePoint wp;
		for (int i = points.size() - 1; i >= 0; i--) {
			wp = points.get(i);
			if (wp.opacity > 1f) wp.opacity = 1f;
			if (wp.opacity <= 0.1) {
				points.remove(i);
			}
			// put a limit to drawing BUT CALCULATE REMOVAL FIRST
			if (i < points.size() - maxRender) {
				isDrawingLimited = true;
				break;
			}
			g2.setColor(new Color(getForeground().getRed()/255f,getForeground().getGreen()/255f,
					getForeground().getBlue()/255f, wp.opacity));
			g2.fillOval((int)(wp.xPos - dotSize/2), (int)(wp.yPos - dotSize/2),
					dotSize, dotSize);
		}
		// instructions
		g2.setColor(new Color(getForeground().getRed()/255f,
				getForeground().getGreen()/255f, getForeground().getBlue()/255f, 0.5f));
		int height = g2.getFontMetrics().getHeight();
		g2.fillRect(0,0,g2.getFontMetrics().stringWidth("Press Shift+Space to generate wave at center")+15,
				height*(isDrawingLimited?12:11));
		
		g2.setColor(getBackground());
		g2.drawString("Press Space to generate wave at mouse", 5, 5+height);
		g2.drawString("Press Shift+Space to generate wave at center", 5, 5+height*2);
		g2.drawString("Press R to generate dots randomly", 5, 5+height*3);
		g2.drawString("Press Esc to clear all dots", 5, 5+height*4);
		
		g2.drawString("Hits: " + hitCount, 5, 5+height*6);
		g2.drawString("Dots: " + points.size(), 5, 5+height*7);
		g2.drawString("Sim-frames per sec: " + String.format("%.02f", fps), 5, 5+height*8);
		g2.drawString("Cycle count: " + cycleCount, 5, 5+height*9);
		g2.drawString("Thread restarts: " + restartCount, 5, 5+height*10);
		if (isDrawingLimited) g2.drawString("WARNING: Not all dots drawn. "
				+ points.size() + " of " + maxRender, 5, 5+height*11);
		
	}

}
