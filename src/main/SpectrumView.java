package main;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import utilities.RollingAverage;
import utilities.Utilities;


public class SpectrumView extends JPanel implements SpectrumConsumer{
	
	private double[] spectrumData;
	
	private static final Color BG_COLOR = new Color(0, 0, 0, 255);
	private static final Color OUTLINE_COLOR = new Color(255, 255, 255, 255);
	private static final Color FILL_COLOR = new Color(0, 130, 170, 255);
	//private static final Color FILL_COLOR = new Color(32, 32, 32, 255);
	
	private static final Color BAR_COLOR = new Color(255, 255, 255, 255);
	private static final int DEFAULT_SIZE_W = 800;
	private static final int DEFAULT_SIZE_H = 400;
	
	RollingAverage totalLongAvg = new RollingAverage(10000/30);
	RollingAverage peakLongAvg = new RollingAverage(10000/30);
	
	boolean flip = false;

	
	public SpectrumView() {
		setPreferredSize(new Dimension(DEFAULT_SIZE_W, DEFAULT_SIZE_H));
		createEmptyImage();
	}
	
	public void enableFlip(boolean flip) {
		this.flip = flip;
	}
	
	public void updateSpectrum(double[] spectrumData) {
		this.spectrumData = spectrumData;
		totalLongAvg.update(Utilities.fftSum(0, 256, spectrumData));
		double max = 0;
		for (int i = 0; i<256; i++)
			max = Math.max(max, spectrumData[i]);
		peakLongAvg.update(max);

		//repaint();
		//paintComponent(this.getGraphics());
		paintVis(this.getGraphics());
	}
	
	private BufferedImage image;
	private Graphics2D g2d;
	
	private void createEmptyImage()
	{
		image = new BufferedImage(DEFAULT_SIZE_W, DEFAULT_SIZE_H, BufferedImage.TYPE_INT_ARGB);
		g2d = (Graphics2D)image.getGraphics();
    	g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    	g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
		g2d.setColor(Color.BLACK);
	    g2d.fillRect(0, 0, DEFAULT_SIZE_W, DEFAULT_SIZE_H);

	}
	
	@Override
	protected void paintComponent(Graphics graphics) {
	    super.paintComponent(graphics);
		graphics.drawImage(image, 0, 0, null);
	}
	
	private int[] xpoints = new int[258];
	private int[] ypoints = new int[258];
	{
		
	}

	private void dickingAround() {
	    Composite defaultComposite = g2d.getComposite();
	    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.05f);
		Graphics2D g = g2d;
	    g.setComposite(ac);
	    g.fillRect(0, 0, DEFAULT_SIZE_W, DEFAULT_SIZE_H);
	    g.drawImage(image, 0, 0, null);
	    g.setComposite(defaultComposite);
	}
	
	protected void paintVis(Graphics graphics) {
		Graphics2D g = g2d;
	    
	    g.setColor(BG_COLOR);
	    g.fillRect(0, 0, DEFAULT_SIZE_W, DEFAULT_SIZE_H);
	    
	    //g.setColor(BAR_COLOR);
	    synchronized (spectrumData) {
		    for (int i = 0; i < 256; i++) {
		    	int val = (int) (spectrumData[i] * getHeight() / (peakLongAvg.getPercentile(1.0) * 1.0));
		    	xpoints[i] = i*4;
		    	ypoints[i] = getHeight()-val;
		    	//g.fillRect(i*4, 500 - val, 4, val);
		    }
		    xpoints[256] = 256*4;
		    xpoints[257] = 0;
		    ypoints[256] = getHeight();
		    ypoints[257] = getHeight();
		    
		    if (flip) {
		    	for (int i = 0; i<xpoints.length; i++)
		    		ypoints[i] = getHeight() - ypoints[i];
		    }
		    
	    	Polygon p = new Polygon(xpoints, ypoints, 258);

	    	g.setColor(FILL_COLOR);
	    	g.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
	    	g.fillPolygon(p);
	    	g.setColor(OUTLINE_COLOR);
	    	g.drawPolygon(p);
	    }
	    
		graphics.drawImage(image, 0, 0, null);
	}
}