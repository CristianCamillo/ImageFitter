package frames;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileNameExtensionFilter;

import misc.CanvasUpdater;
import misc.ImageCollector;

public class MainFrame extends JFrame implements ActionListener
{	
	private static final long serialVersionUID = 1L;
	
	// frame elements
		
	private final JMenuItem createNew;
	private final JMenuItem importImg;
	private final JMenuItem save;
	private final JMenuItem saveAs;
	private final JMenuItem exit;
	private final JMenuItem info;
	
	private Canvas canvas;
	
	//////////////////	
	
	private String savePath = ".";
	private String importPath = ".";
	
	private ImageCollector collector;
	
	private short[] dimensions = new short[2]; // dimension[0] = width, dimension[1] = height
	
	private CanvasUpdater canvasUpdater;
	private boolean saved = true;
	
	private final Object lock = new Object();
	
	//////////////////
	
	private final static int WIDTH = 700;
	private final static int HEIGHT = 700;
	
	public MainFrame()
	{		
		// setup frame
		
		setTitle("Image Fitter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(WIDTH + 16, HEIGHT + 62);
		setLocationRelativeTo(null);
		setResizable(false);
		
		
		// create menuBar to add to the frame
		
		JMenuBar menuBar = new JMenuBar();
		
		
		// create menu "File"		
	
		JMenu fileMenu = new JMenu("File");
	
		
		// create items to add to "File" menu (if the icons are not present, they just wont be shown)
		
		createNew = new JMenuItem("Create new" 	 );//  new ImageIcon("icons/new.png"));
		importImg = new JMenuItem("Import images    ");//, new ImageIcon("icons/import.gif"));
		save 	  = new JMenuItem("Save" 			 ); // new ImageIcon("icons/save.png"));
		saveAs 	  = new JMenuItem("Save as..." 	 ); // new ImageIcon("icons/saveAs.png"));
		exit 	  = new JMenuItem("Exit" 			 );//  new ImageIcon("icons/exit.gif"));
		info 	  = new JMenuItem("Info");
		
		
		// add shortcuts to items
		
		createNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
		importImg.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
		save	 .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
		saveAs	 .setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK));
		exit	 .setAccelerator(KeyStroke.getKeyStroke((char)KeyEvent.VK_ESCAPE));
		
		
		// add items to their menu
		
		fileMenu.add(createNew);
		fileMenu.addSeparator();
		fileMenu.add(importImg);
		fileMenu.addSeparator();
		fileMenu.add(save);
		fileMenu.add(saveAs);
		fileMenu.addSeparator();
		fileMenu.add(exit);
		
		
		// add menu and "Info" item to menuBar, then set it to the frame
		
		menuBar.add(fileMenu);
		menuBar.add(info);
		setJMenuBar(menuBar);
		
		
		// disable some items
		
		importImg.setEnabled(false);
		save	 .setEnabled(false);
		saveAs   .setEnabled(false);
		
		
		// add actionListener to the items (implemented in this class)
		
		createNew.addActionListener(this);
		importImg.addActionListener(this);
		save	 .addActionListener(this);
		saveAs	 .addActionListener(this);
		exit	 .addActionListener(this);
		info	 .addActionListener(this);
		
		
		// create and add canvas to the frame
		
		canvas = new Canvas();
		add(canvas);
		canvas.setSize(new Dimension(WIDTH, HEIGHT));		
		
		
		// show frame
		
		setVisible(true);		
		canvas.createBufferStrategy(2);
	}

	
	
	public void actionPerformed(ActionEvent e)
	{		
		// create new blank image
		
		if(e.getSource().equals(createNew))
		{
			if(!saved)
				if(JOptionPane.showConfirmDialog(null, "The current image hasn't been saved.\nDo you still want to continue?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != 0)
					return;
			
			new CreateNewImagePane(dimensions);
			
			if(dimensions[0] > 0)
			{
				if(canvasUpdater != null)
					canvasUpdater.arrest();

				importImg.setEnabled(true);
				save	 .setEnabled(false);
				saveAs	 .setEnabled(true);
				
				collector = new ImageCollector();
				
				BufferedImage blank = new BufferedImage(dimensions[0], dimensions[1], BufferedImage.TYPE_3BYTE_BGR);
				Graphics g = blank.createGraphics();
				g.setColor(Color.WHITE);
				g.fillRect(0, 0, dimensions[0], dimensions[1]);
				g.dispose();
				
				collector.add(blank, (short)0, (short)0);
				
				canvasUpdater = new CanvasUpdater(canvas, collector, lock);
				canvasUpdater.start();
			}
		}
		
		
		// import image
		
		if(e.getSource().equals(importImg))
		{
			JFileChooser fileSelector = new JFileChooser(importPath);
			
			fileSelector.setFileFilter(new FileNameExtensionFilter("PNG File", "png"));
			fileSelector.setAcceptAllFileFilterUsed(false);
			fileSelector.setDialogTitle("Import an image");
			
			if(fileSelector.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				try
				{
					BufferedImage image = ImageIO.read(new File(fileSelector.getSelectedFile().getAbsolutePath()));
					importPath = fileSelector.getSelectedFile().getParent();
					
					if(fitImage(image))
					{
						saved = false;
						JOptionPane.showMessageDialog(null, "The images have been added", "Information", JOptionPane.INFORMATION_MESSAGE);
					}
					else
						JOptionPane.showMessageDialog(null, "Cannot fit this image!", "Error", JOptionPane.ERROR_MESSAGE);		
				}
				catch (IOException e1)
				{
					JOptionPane.showMessageDialog(null, "Cannot import this image!", "Error", JOptionPane.ERROR_MESSAGE);	
				}
			}
		}
		
		
		// save
		
		if(e.getSource().equals(save))
			saveImage();
		
		
		// save as
		
		if(e.getSource().equals(saveAs))
		{
			JFileChooser fileSelector = new JFileChooser(savePath);
			
			fileSelector.setFileFilter(new FileNameExtensionFilter("PNG File", "png"));
			fileSelector.setAcceptAllFileFilterUsed(false);
			fileSelector.setDialogTitle("Save the image");
			
			if(fileSelector.showSaveDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				savePath = fileSelector.getSelectedFile().getAbsolutePath() + ".png";
				saveImage();
			}
		}
		
		
		// exit
		
		if(e.getSource().equals(exit))
		{
			if(!saved)
				if(JOptionPane.showConfirmDialog(null, "Do you want to exit without having saved the image?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != 0)
					return;
			
			System.exit(0);	
		}
		
		
		// info
		
		if(e.getSource().equals(info))
			JOptionPane.showMessageDialog(null, "Created by Cristian Camillo\nEmail: cristian.camillo@yahoo.it", "Information", JOptionPane.INFORMATION_MESSAGE);
	}
	
	private void saveImage()
	{
		File outputFile = new File(savePath);
		
		if(outputFile.exists())
			if(JOptionPane.showConfirmDialog(null, "Do you want to overwrite the existing image?", "Warning", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE) != 0)
				return;
		
		BufferedImage blank = (BufferedImage)collector.getImage(0);
		Graphics g = blank.getGraphics();
		g.fillRect(0, 0, blank.getWidth(), blank.getHeight());
		
		for(int i = 1; i < collector.size(); i++)
			g.drawImage(collector.getImage(i), collector.getImagePosition(i)[0], collector.getImagePosition(i)[1], null);			    
	   
	    try
	    {
			ImageIO.write(blank, "png", outputFile);
			JOptionPane.showMessageDialog(null, "The image has been exported", "Information", JOptionPane.INFORMATION_MESSAGE);
			save.setEnabled(true);
			saved = true;
		}
	    catch (IOException e1)
	    {
			JOptionPane.showMessageDialog(null, "Cannot save the image!", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	
	private boolean fitImage(BufferedImage image)
	{		
		// check if the image's area doesn't exceed to available blank area
		// (this stops from needlessly going into the rest of the function, thus improving performance)
		
		short width = (short)collector.getImage(0).getWidth(null);
		short height = (short)collector.getImage(0).getHeight(null);
		
		int area = width * height;
		
		int sum = image.getWidth(null) + image.getHeight(null);
		
		if(sum > area)
			return false;
		
		for(int i = 1; i < collector.size(); i++)
		{	
			sum += collector.getImage(i).getWidth(null) * collector.getImage(i).getHeight(null);
			if(sum > area)
				return false;		
		}
		
		
		// create data to work on
		
		ArrayList<BufferedImage> images = new ArrayList<BufferedImage>(); // list of the already present images
		ArrayList<short[]> 		 blanks = new ArrayList<short[]>(); 	  // list of rectangles representing the blank area (width, height, x, y)
		
		ArrayList<BufferedImage> result = new ArrayList<BufferedImage>(); // list of (re)inserted images
		ArrayList<short[]> 		 pos 	= new ArrayList<short[]>(); 	  // list of positions of the inserted images
		
		ArrayList<short[]> removed = new ArrayList<short[]>();			  // list where to temporary store blank areas
		
		
		// adding collector's images and the current image to the source list
		
		for(int i = 1; i < collector.size(); i++)
			images.add(collector.getImage(i));
		
		boolean added = false;
		for(int i = 0; i < images.size(); i++)
			if(image.getWidth() * image.getHeight() >= images.get(i).getWidth() * images.get(i).getHeight())
			{
				images.add(i, image);
				added = true;
				break;
			}
		
		if(!added)
			images.add(image);

		
		// setting the blank areas as the whole blank image
		
		blanks.add(new short[] {width, height, 0, 0});
		
		
		// while there are still images to add and space
				
		while(images.size() > 0 && blanks.size() > 0)
		{
			// getting the largest image
			// getting the smallest blank area
		
			int maxImg = maxImgArea(images);
			int minBlk = minBlkArea(blanks);
			
			BufferedImage curImg = images.get(maxImg);
			short[] curBlk = blanks.get(minBlk);
			
			
			// check if the image fits into the blank area (rotate the image if necessary)
			
			byte fits = fits(curImg, curBlk);

			if(fits > 0)		
			{
				if(fits == 2)
					curImg = rotate90(curImg);
				
				
				// add the current image to the result list, with its position equals at the top-left corner of the blank area
				
				result.add(curImg);
				pos.add(new short[] {curBlk[2], curBlk[3]});
				
				
				// remove the inserted image from the image list and the blank area
				
				images.remove(maxImg);
				
				
				// if images to add are not over (improve performance)
				
				if(images.size() > 0)
				{
					blanks.remove(minBlk);
					
					
					// add the blank areas left (0, 1 or 2)				
					
					if(curImg.getWidth(null) < curBlk[0] || curImg.getHeight(null) < curBlk[1])
					{	
						if(curImg.getWidth(null) == curBlk[0])
						{
							blanks.add(new short[] {curBlk[0], (short)(curBlk[1] - curImg.getHeight(null)),
												   curBlk[2], (short)(curBlk[3] + curImg.getHeight(null))});
						}
						else if(curImg.getHeight(null) == curBlk[1])
						{
							blanks.add(new short[] {(short)(curBlk[0] - curImg.getWidth(null)), curBlk[1],
									   			   (short)(curBlk[2] + curImg.getWidth(null)), curBlk[3]});
						}
						else
						{
							blanks.add(new short[] {(short)(curBlk[0] - curImg.getWidth(null)), (short)curImg.getHeight(null), // right side image
									 			   (short)(curBlk[2] + curImg.getWidth(null)), curBlk[3]});
							
							blanks.add(new short[] {curBlk[0], (short)(curBlk[1] - curImg.getHeight(null)), // bottom side image
						 			   			   curBlk[2], (short)(curBlk[3] + curImg.getHeight(null))});
						}
					}
					
					blanks.addAll(removed);
				}
			}
			else
				removed.add(blanks.remove(minBlk));
			
			// TODO: join horizontal areas
		}
		
		
		// set new collection
		
		if(images.size() == 0)
		{
			result.add(0, collector.getImage(0));
			pos.add(0, new short[] {0, 0});
			
			synchronized(lock)
			{				
				collector.setNewCollection(result, pos);
			}
			
			return true;
		}
		else
			return false;
	}
	
	
	private int maxImgArea(ArrayList<BufferedImage> images)
	{
		int max = images.get(0).getWidth(null) * images.get(0).getHeight(null);
		int index = 0;
		
		for(int i = 1; i < images.size(); i++)
			if(max < images.get(i).getWidth(null) * images.get(i).getHeight(null))
			{
				max = images.get(i).getWidth(null) * images.get(i).getHeight(null);
				index = i;
			}
		
		return index;
	}
	
	private int minBlkArea(ArrayList<short[]> blanks)
	{
		int min = blanks.get(0)[0] * blanks.get(0)[1];
		int index = 0;
		
		for(int i = 1; i < blanks.size(); i++)
			if(min > blanks.get(i)[0] * blanks.get(i)[1])
			{
				min = blanks.get(i)[0] * blanks.get(i)[1];
				index = i;
			}
		
		return index;
	}
	
	private byte fits(BufferedImage img, short[] blk)
	{
		if(blk[0] >= img.getWidth() && blk[1] >= img.getHeight())
			return 1;
		else if(blk[0] >= img.getHeight() && blk[1] >= img.getWidth())
			return 2;
		else
			return 0;
	}
	
	private BufferedImage rotate90(BufferedImage image)
	{
		int width = image.getWidth();
		int height = image.getHeight();
		
		int imgPixels[] = new int[width * height];
		image.getRGB(0, 0, width, height, imgPixels, 0, width);
		
		byte[] array = new byte[width * height * 4];
		for(int i = 0; i < width * height; i++)
		{
			int pixel = imgPixels[i];
	
			array[i * 4]     = (byte)(pixel >> 24); // A
			array[i * 4 + 1] = (byte)(pixel      ); // B
			array[i * 4 + 2] = (byte)(pixel >> 8 ); // G
			array[i * 4 + 3] = (byte)(pixel >> 16); // R
		}
		
		
		BufferedImage newImg = new BufferedImage(height, width, BufferedImage.TYPE_4BYTE_ABGR);
		byte[] newArray = ((DataBufferByte)newImg.getRaster().getDataBuffer()).getData();

		int i = 0;
		
		for(int x = 0; x < width; x++)
			for(int y = 0; y < height; y++)				
			{
				int index = (x + y * width) * 4;
				
				newArray[i] 	= array[index];
				newArray[i + 1] = array[index + 1];
				newArray[i + 2] = array[index + 2];
				newArray[i + 3] = array[index + 3];
				
				i += 4;
			}
		
		return newImg;
	}
}
