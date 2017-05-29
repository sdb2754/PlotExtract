/*     */ package events;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

public class Board extends JPanel implements MouseMotionListener, MouseListener, KeyListener {
	private static final long serialVersionUID = 1L;
	static JFrame frame;
	static long dt;
	static Board newContentPane;

	AutoMenuBar ribbon;
	static Figure figure;
	static Table table;

	static JTabbedPane tabbedPane = new JTabbedPane();

	public static void main(String[] args) {
		frame = new JFrame("Plot Extractor");
		frame.setDefaultCloseOperation(3);

		newContentPane = new Board();
		newContentPane.setOpaque(true);
		frame.setContentPane(newContentPane);
		frame.setPreferredSize(new Dimension(1400, 1000));
		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);

		dt = 10;
		while (true) {
			update((int) dt);

			newContentPane.repaint();
			try {
				Thread.sleep(dt);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public Board() {
		setLayout(new BorderLayout());
		setFocusable(true);
		addKeyListener(this);

		figure = new Figure();
		table = new Table(50, 20);

		add(tabbedPane, BorderLayout.CENTER);

		tabbedPane.add("Figure", figure);
		tabbedPane.add("Table", table);

		figure.addMouseListener(this);
		figure.addMouseMotionListener(this);

		configure_menu();
	}

	private static void update(int dt) {

	}

	private void configure_menu() {
	
		ribbon = new AutoMenuBar(new String[] { "File", "Edit", "Table", "Layers", "Fit type", "Help" });
		add(ribbon, BorderLayout.NORTH);
		// Items
		ribbon.add_item("Reset", "File", "item");
		ribbon.add_item("Open", "File", "item");
		ribbon.add_item("Export", "File", "item");
		ribbon.add_item("Paste", "Edit", "item");
		ribbon.add_item("Copy", "Edit", "item");
		ribbon.add_item("About", "Help", "item");
		ribbon.add_item("Controls", "Help", "item");

		// Checkboxes
		ribbon.add_item("Original plot", "Layers", "check", true);
		ribbon.add_item("Origin", "Layers", "check", true);
		ribbon.add_item("X calibration", "Layers", "check", true);
		ribbon.add_item("Y calibration", "Layers", "check", true);
		ribbon.add_item("Data points", "Layers", "check", true);
		ribbon.add_item("Fit", "Layers", "check", true);
		ribbon.add_item("Axes", "Layers", "check", true);
		
		// radiobuttons
		ribbon.add_item("radios", "bar", "group");
		ribbon.add_item("Linear", "Fit type", "radio", "radios");
		ribbon.add_item("Interpolation", "Fit type", "radio", "radios");
		ribbon.add_item("Spline", "Fit type", "radio", "radios");
		ribbon.add_item("Connect", "Fit type", "radio", "radios");
		ribbon.add_item("Regression", "Fit type", "radio", "radios");
		ribbon.add_item("None", "Fit type", "radio", "radios", true);
		
		

		// actions
		ribbon.set_action("Reset", (y) -> {
			figure.set_step(0);
		});
		ribbon.set_action("Open", (y) -> {
			figure.addimage();
		});
		ribbon.set_action("Paste", (y) -> {
			if (tabbedPane.getSelectedIndex() == 0)
				figure.getImageFromClipboard();
		});
		ribbon.set_action("Copy", (y) -> {
			if (tabbedPane.getSelectedIndex() == 1)
				table.copytoClipboard();
		});
		ribbon.set_action("Export", (y) -> {
			figure.export();
		});
		ribbon.set_action("About", (y) -> {
			about();
		});
		ribbon.set_action("Controls", (y) -> {
			controls();
		});
		ribbon.set_action("Regression", (y) -> {
			if (y)
				figure.set_fit("regression");
		});

		// state changes
		ribbon.set_statechange("Original plot", (y) -> {
			figure.showplot = y;
		});
		ribbon.set_statechange("Fit", (y) -> {
			figure.showfit = y;
		});
		ribbon.set_statechange("Origin", (y) -> {
			figure.showorigin = y;
		});
		ribbon.set_statechange("X calibration", (y) -> {
			figure.showx = y;
		});
		ribbon.set_statechange("Y calibration", (y) -> {
			figure.showy = y;
		});
		ribbon.set_statechange("Data points", (y) -> {
			figure.showdata = y;
		});
		ribbon.set_statechange("Axes", (y) -> {
			figure.showaxes = y;
		});
		ribbon.set_action("Linear", (y) -> {
			if (y)
				figure.set_fit("linear");
		});
		ribbon.set_action("Interpolation", (y) -> {
			if (y)
				figure.set_fit("interp");
		});
		ribbon.set_action("Spline", (y) -> {
			if (y)
				figure.set_fit("spline");
		});
		ribbon.set_action("Connect", (y) -> {
			if (y)
				figure.set_fit("connect");
		});
		ribbon.set_action("None", (y) -> {
			if (y)
				figure.set_fit("none");
		});
		
	}

	public void about() {
		JOptionPane.showMessageDialog(Board.frame,
				"PlotExtract\nBy Seth Berry.\nvs. 1.6\nExtracts data from plots and figures.");
	}

	public void controls() {
		JOptionPane.showMessageDialog(Board.frame,
				"To begin, add a plot, graph, figure, etc.\n" + "You can use File+Paste, cntrl+v,\n"
						+ " or click the plot area to add a picture from a file.\n\n"
						+ "Follow the instructions at the bottom of the window\n"
						+ "to add calibration points. Click an axis calibration\n"
						+ "point to see an option tochange that axis to a log axis.\n\n"
						+ "Rightclick a point to see a list of actions you can perform on it.\n"
						+ "Leftclick a point enable moving it.\n" + "Move a point using the arrow keys.\n\n"
						+ "Change the fit type by clicking a datapoint and selecting 'fit'\n"
						+ "Export by using File+Export or clicking a datapoint\n" + "and selecting 'Export'.\n\n"
						+ "Datapoints can be moved or deleted.");
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		if ((e.getKeyCode() == KeyEvent.VK_V)
				&& ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0 && tabbedPane.getSelectedIndex() == 0)) {
			figure.getImageFromClipboard();
			return;
		}
		switch (e.getKeyCode()) {

		case KeyEvent.VK_UP:
			figure.movepoint(0, -1);
			break;
		case KeyEvent.VK_DOWN:
			figure.movepoint(0, 1);
			break;
		case KeyEvent.VK_LEFT:
			figure.movepoint(-1, 0);
			break;
		case KeyEvent.VK_RIGHT:
			figure.movepoint(1, 0);
			break;

		}
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		boolean isrightbutton = (e.getModifiers() & InputEvent.BUTTON3_MASK) == InputEvent.BUTTON3_MASK;
		figure.addpoint(e.getX(), e.getY(), isrightbutton);
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseDragged(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
	}

}
