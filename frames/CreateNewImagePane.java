package frames;

import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

public class CreateNewImagePane implements FocusListener
{	
	private final static JLabel indicationLabel = new JLabel("Select between presets and custom dimensions:");
	
	private final JRadioButton presetsRadio = new JRadioButton("Presets");
	private final JRadioButton customRadio = new JRadioButton("Custom dimensions");
	private final ButtonGroup buttonGroup = new ButtonGroup();
	
	private final static String[] presets = {"A4 (20x30 cm)",
											 "A4 (15x10 cm)"};		
	private final JComboBox<String> dimensionsBox = new JComboBox<String>(presets);
	
	private final JTextField widthTextField = new JTextField();
	private final static JLabel xLabel = new JLabel("x");
	private final JTextField heightTextField = new JTextField();	
	
	private final JPanel panel = new JPanel(null);
	
	private final static int WIDTH = 363;
	private final static int HEIGHT = 120;
	
	public CreateNewImagePane(short[] dimensions)
	{		
		indicationLabel.setBounds(0, 0, 300, 25);		
		presetsRadio.setBounds(0, 40, 100, 25);
		customRadio.setBounds(205, 40, 200, 25);		
		dimensionsBox.setBounds(5, 70, 160, 25);		
		widthTextField.setBounds(210, 70, 65, 25);
		heightTextField.setBounds(290, 70, 65, 25);
		xLabel.setBounds(279, 70, 25, 25);
		
		buttonGroup.add(presetsRadio);
		buttonGroup.add(customRadio);
		
		presetsRadio.setSelected(true);
		dimensionsBox.setEnabled(true);		
		customRadio.setSelected(false);
		widthTextField.setEnabled(false);
		heightTextField.setEnabled(false);
		xLabel.setEnabled(false);
		widthTextField.setText("");
		heightTextField.setText("");
		
		panel.add(indicationLabel);
		panel.add(presetsRadio);
		panel.add(customRadio);
		panel.add(dimensionsBox);		
		panel.add(widthTextField);
		panel.add(xLabel);
		panel.add(heightTextField);	
		
		panel.setPreferredSize(new Dimension(WIDTH, HEIGHT));
		
		presetsRadio.addFocusListener(this);
		customRadio.addFocusListener(this);		
		
		boolean successful = false;
		
		while(!successful)
		{			
			successful = true;
			
			int result = JOptionPane.showConfirmDialog(null, panel, "Create New Blank Image", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
			
			if(result == 0)
				if(presetsRadio.isSelected())
					switch(dimensionsBox.getSelectedIndex())
					{
						case 0: dimensions[0] = 2368;
								dimensions[1] = 3476;
								break;
						case 1: dimensions[0] = 3476 / 2;
								dimensions[1] = 2368 / 2;
					}
				else
					try
					{				
						String strWidth = widthTextField.getText();
						String strHeight = heightTextField.getText();
						
						if(!strWidth.isBlank() && !strHeight.isBlank())
						{					
							short width = Short.parseShort(strWidth);
							short height = Short.parseShort(strHeight);
							
							if(width > 0 && height > 0)
							{
								dimensions[0] = width;
								dimensions[1] = height;
							}
							else
							{
								JOptionPane.showMessageDialog(null, "Enter positive numbers!", "Input Error", JOptionPane.ERROR_MESSAGE);
								
								successful = false;
							}
						}
						else
						{
							JOptionPane.showMessageDialog(null, "Enter positive numbers!", "Input Error", JOptionPane.ERROR_MESSAGE);
							
							successful = false;
						}
					}
					catch(NumberFormatException e)
					{
						JOptionPane.showMessageDialog(null, "Enter positive numbers!", "Input Error", JOptionPane.ERROR_MESSAGE);
						
						widthTextField.setText("");
						heightTextField.setText("");
						
						successful = false;
					}
			else
				dimensions[0] = -1;
		}
	}

	public void focusGained(FocusEvent e)
	{
		if(e.getSource().equals(presetsRadio))
		{
			dimensionsBox.setEnabled(true);
			
			widthTextField.setEnabled(false);
			xLabel.setEnabled(false);
			heightTextField.setEnabled(false);		
		}
		
		if(e.getSource().equals(customRadio))
		{
			dimensionsBox.setEnabled(false);
			
			widthTextField.setEnabled(true);
			xLabel.setEnabled(true);
			heightTextField.setEnabled(true);		
			
		}
	}
	
	public void focusLost(FocusEvent e){}
}
