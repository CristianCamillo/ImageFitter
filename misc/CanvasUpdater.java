package misc;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferStrategy;

public class CanvasUpdater extends Thread
{
	private Canvas canvas;
	private BufferStrategy bs;
	private Graphics g;
	
	private ImageCollector images;
	
	private int FPSCap = 60;
	private long frameDuration = 0;
	private boolean running = true;
	
	private final Object lock;
	
	public CanvasUpdater(Canvas canvas, ImageCollector collector, Object lock)
	{
		this.canvas = canvas;
		bs = canvas.getBufferStrategy();
		g = bs.getDrawGraphics();
		
		images = collector;
		this.lock = lock;
		
		if(FPSCap > 0)
			frameDuration = (long)(1f / this.FPSCap * 1000000000);
	}
	
	public void arrest()
	{
		if(running)
		{
			running = false;
			g.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			bs.show();
		}
	}
	
	public void run()
	{
		int canvasWidth = canvas.getWidth();
		int canvasHeight = canvas.getHeight();
		
		int blankImageWidth = images.getImage(0).getWidth(null);
		int blankImageHeight = images.getImage(0).getHeight(null);
		
		int blankX = 0;
		int blankY = 0;
		int blankWidth;
		int blankHeight;
		
		if(blankImageWidth > blankImageHeight)
		{
			blankWidth = canvasWidth;
			blankHeight = (int)(blankImageHeight * 1.0 / blankImageWidth * canvasHeight);
		
			blankY = (700 - blankHeight) / 2;
		}
		else
		{
			blankHeight = canvasHeight;
			blankWidth = (int)(blankImageWidth * 1.0 / blankImageHeight * canvasWidth);
			
			blankX = (700 - blankWidth) / 2;
		}
		
		double widthRatio = blankWidth * 1.0 / blankImageWidth;
		double heightRatio = blankHeight * 1.0 / blankImageHeight;
		
		g.setColor(new Color(200, 200, 200));
		
		while(running)
		{
			long start = System.nanoTime();
			
			g.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
			
			synchronized(lock)
			{			
				for(int i = 0; i < images.size(); i++)
				{
					Image image = images.getImage(i);				
					
					int imageX = (int)(images.getImagePosition(i)[0] * widthRatio + blankX);
					int imageY = (int)(images.getImagePosition(i)[1] * heightRatio + blankY); 
					
					int imageWidth 	= (int)(image.getWidth(null)  * widthRatio  * 1.0);
					int imageHeight = (int)(image.getHeight(null) * heightRatio * 1.0);
					
					g.drawImage(image,
								imageX, imageY, imageWidth + imageX, imageHeight + imageY,
								0, 0, image.getWidth(null), image.getHeight(null),
								null);
				}			
			}
			
			bs.show();
							
			if(this.FPSCap > 0)
			{
				long timeLeft = frameDuration - (System.nanoTime() - start) - 1000000; // << sync delay
				
				if(timeLeft > 0)
					try{
						Thread.sleep(timeLeft / 1000000, (int)(timeLeft % 1000000));
					}catch(InterruptedException e) { System.err.println(e); System.exit(1); }
				
				while(frameDuration > System.nanoTime() - start);
			}	
		}
	}
}