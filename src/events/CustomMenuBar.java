package events;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

public class CustomMenuBar extends JMenuBar implements ActionListener, ItemListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	ButtonGroup group = new ButtonGroup();
	
	   JMenu file;
	   JMenu edit;
	   JMenu help;
	   JMenu layers;
	   JMenu fittype;
	   
	   JMenuItem new_plot;
	   JMenuItem open;
	   JMenuItem paste;
	   JMenuItem copy;
	   JMenuItem export;
	   JMenuItem about;
	   JMenuItem controls;
	   
	   JCheckBoxMenuItem plot;
	   JCheckBoxMenuItem origin;
	   JCheckBoxMenuItem x_cal;
	   JCheckBoxMenuItem y_cal;
	   JCheckBoxMenuItem data;
	   JCheckBoxMenuItem fit;
	   
	   JRadioButtonMenuItem linear;
	   JRadioButtonMenuItem interp;
	   JRadioButtonMenuItem spline;
	   JRadioButtonMenuItem connect;
	   JRadioButtonMenuItem regression;
	   JRadioButtonMenuItem none;
	   
	   CustomMenuBar(){
		     file = new JMenu("File");
		     edit = new JMenu("Edit");
		     help = new JMenu("Help");
		     layers = new JMenu("Layers");
		     fittype = new JMenu("Fit");
		     add(file);
		     add(edit);
		     add(fittype);
		     add(layers);
		     add(help);
		     file.addActionListener(this);
		     edit.addActionListener(this);
		     help.addActionListener(this);
		     layers.addActionListener(this);
		     fittype.addActionListener(this);
		     
		     new_plot = new JMenuItem("New");
		     open = new JMenuItem("Open");
		     export = new JMenuItem("Export");
		     file.add(new_plot);
		     file.add(open);
		     file.add(export);
		     new_plot.addActionListener(this);
		     open.addActionListener(this);
		     export.addActionListener(this);
		     
		     paste = new JMenuItem("Paste");
		     copy = new JMenuItem("Copy");
		     edit.add(paste);
		     edit.add(copy);
		     paste.addActionListener(this);
		     copy.addActionListener(this);
		     
		     about = new JMenuItem("About");
		     controls = new JMenuItem("Controls");
		     help.add(about);
		     help.add(controls);
		     about.addActionListener(this);
		     controls.addActionListener(this);
		     
		     plot = new JCheckBoxMenuItem("Original Plot",true);
		     origin = new JCheckBoxMenuItem("Origin",true);
		     x_cal = new JCheckBoxMenuItem("X Calibration",true);
		     y_cal = new JCheckBoxMenuItem("Y Calibration",true);
		     data = new JCheckBoxMenuItem("Data Points",true);
		     fit = new JCheckBoxMenuItem("Curve Fit",true);
		     layers.add(plot);
		     layers.add(origin);
		     layers.add(x_cal);
		     layers.add(y_cal);
		     layers.add(data);
		     layers.add(fit);
		     plot.addItemListener(this);
		     origin.addItemListener(this);
		     x_cal.addItemListener(this);
		     y_cal.addItemListener(this);
		     data.addItemListener(this);
		     fit.addItemListener(this);
		     
		     linear = new JRadioButtonMenuItem("Linear",false);
		     interp = new JRadioButtonMenuItem("Interpolation",false);
		     spline = new JRadioButtonMenuItem("Spline",false);
		     connect = new JRadioButtonMenuItem("Connect",false);
		     regression = new JRadioButtonMenuItem("Regression",false);
		     none = new JRadioButtonMenuItem("None",true);
		     fittype.add(linear);
		     fittype.add(interp);
		     fittype.add(spline);
		     fittype.add(connect);
		     fittype.add(regression);
		     fittype.add(none);
		     linear.addItemListener(this);
		     interp.addItemListener(this);
		     spline.addItemListener(this);
		     connect.addItemListener(this);
		     regression.addActionListener(this);
		     none.addItemListener(this);
		     group.add(linear);
		     group.add(interp);
		     group.add(spline);
		     group.add(connect);
		     group.add(regression);
		     group.add(none);
	   }

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		String cmd = e.getActionCommand();
		switch(cmd){
		
		case "New":
			Board.figure.set_step(0);
			break;
		case "Open":
			Board.figure.addimage();
			Board.figure.set_step(1);
			break;
		case "Paste":
			if(Board.tabbedPane.getSelectedIndex()==0){
				Board.figure.getImageFromClipboard();
				Board.figure.set_step(1);
			}
			break;
		case "Copy":
			if(Board.tabbedPane.getSelectedIndex()==1){
				Board.table.copytoClipboard();
			}
			break;
		case "Export":
			Board.figure.export();
			break;
		case "About":
			JOptionPane.showMessageDialog(Board.frame, "PlotExtract\nBy Seth Berry.\nvs. 1.6\nExtracts data from plots and figures.");
			break;
		case "Controls":
			JOptionPane.showMessageDialog(Board.frame, 
					"To begin, add a plot, graph, figure, etc.\n"
					+ "You can use File+Paste, cntrl+v,\n"
					+ " or click the plot area to add a picture from a file.\n\n"
					+ "Follow the instructions at the bottom of the window\n"
					+ "to add calibration points. Click an axis calibration\n"
					+ "point to see an option tochange that axis to a log axis.\n\n"
					+ "Rightclick a point to see a list of actions you can perform on it.\n"
					+ "Leftclick a point enable moving it.\n"
					+ "Move a point using the arrow keys.\n\n"
					+ "Change the fit type by clicking a datapoint and selecting 'fit'\n"
					+ "Export by using File+Export or clicking a datapoint\n"
					+ "and selecting 'Export'.\n\n"
					+ "Datapoints can be moved or deleted.");
			break;
	    case "Regression":
	    	Board.figure.set_fit("regression");
	    	break;
			
		}
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		// TODO Auto-generated method stub
		AbstractButton button = (AbstractButton) e.getItem();
		boolean state = button.isSelected();
	    String cmd = button.getText();
	    switch(cmd){
	    
	    case "Original Plot":
	    	Board.figure.showplot=state;
	    	break;
	    	
	    case "Curve Fit":
	    	Board.figure.showfit=state;
	    	break;
	    		
	    case "Origin":
	    	Board.figure.showorigin=state;
	    	break;
	    	
	    case "X Calibration":
	    	Board.figure.showx=state;
	    	break;
	    	
	    case "Y Calibration":
	    	Board.figure.showy=state;
	    	break;
	    	
	    case "Data Points":
	    	Board.figure.showdata=state;
	    	break;
	    	
	    case "Linear":
	    	Board.figure.set_fit("linear");
	    	break;
	    	
	    case "Interpolation":
	    	Board.figure.set_fit("interp");
	    	break;
	    	
	    case "Spline":
	    	Board.figure.set_fit("spline");
	    	break;
	    	
	    case "Connect":
	    	if(state)
	    		Board.figure.set_fit("connect");
	    	break;
	    	
	    case "None":
	    	Board.figure.set_fit("none");
	    	break;

	    }
	}
	   

}
