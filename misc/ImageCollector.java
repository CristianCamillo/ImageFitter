package misc;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class ImageCollector
{
	private ArrayList<BufferedImage> images;
	private ArrayList<short[]> positions;
	
	public ImageCollector()
	{
		images = new ArrayList<BufferedImage>();
		positions = new ArrayList<short[]>();
	}
	
	public void add(BufferedImage img, short x, short y)
	{		
		images.add(img);
		positions.add(new short[] {x, y});
	}
	
	public BufferedImage getImage(int i)
	{				
		return images.get(i);
	}
	
	public short[] getImagePosition(int i)
	{
		return positions.get(i);
	}
	
	public synchronized void setNewCollection(ArrayList<BufferedImage> images, ArrayList<short[]> positions)
	{
		this.images = images;
		this.positions = positions;
	}
	
	public int size()
	{
		return images.size();
	}
}
