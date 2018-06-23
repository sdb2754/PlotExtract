/*     */ package events;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
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
		ribbon.addItem("Reset", "File", "item");
		ribbon.addItem("Open", "File", "item");
		ribbon.addItem("Export", "File", "item");
		ribbon.addItem("Paste", "Edit", "item");
		ribbon.addItem("Copy", "Edit", "item");
		ribbon.addItem("About", "Help", "item");
		ribbon.addItem("Controls", "Help", "item");

		// Checkboxes
		ribbon.addItem("Original plot", "Layers", "check", true);
		ribbon.addItem("Origin", "Layers", "check", true);
		ribbon.addItem("X calibration", "Layers", "check", true);
		ribbon.addItem("Y calibration", "Layers", "check", true);
		ribbon.addItem("Data points", "Layers", "check", true);
		ribbon.addItem("Fit", "Layers", "check", true);
		ribbon.addItem("Axes", "Layers", "check", true);

		// radiobuttons
		ribbon.addItem("radios", "bar", "group");
		ribbon.addItem("Linear", "Fit type", "radio", "radios");
		ribbon.addItem("Interpolation", "Fit type", "radio", "radios");
		ribbon.addItem("Spline", "Fit type", "radio", "radios");
		ribbon.addItem("Connect", "Fit type", "radio", "radios");
		ribbon.addItem("Regression", "Fit type", "radio", "radios");
		ribbon.addItem("None", "Fit type", "radio", "radios", true);

		// actions
		ribbon.setAction("Reset", (y) -> {
			figure.set_step(0);
		});
		ribbon.setAction("Open", (y) -> {
			figure.addimage();
		});
		ribbon.setAction("Paste", (y) -> {
			if (tabbedPane.getSelectedIndex() == 0)
				figure.getImageFromClipboard();
		});
		ribbon.setAction("Copy", (y) -> {
			if (tabbedPane.getSelectedIndex() == 1)
				table.copytoClipboard();
		});
		ribbon.setAction("Export", (y) -> {
			figure.export();
		});
		ribbon.setAction("About", (y) -> {
			about();
		});
		ribbon.setAction("Controls", (y) -> {
			controls();
		});
		ribbon.setAction("Regression", (y) -> {
			if (y)
				figure.set_fit("regression");
		});

		// state changes
		ribbon.setStateChange("Original plot", (y) -> {
			figure.showplot = y;
		});
		ribbon.setStateChange("Fit", (y) -> {
			figure.showfit = y;
		});
		ribbon.setStateChange("Origin", (y) -> {
			figure.showorigin = y;
		});
		ribbon.setStateChange("X calibration", (y) -> {
			figure.showx = y;
		});
		ribbon.setStateChange("Y calibration", (y) -> {
			figure.showy = y;
		});
		ribbon.setStateChange("Data points", (y) -> {
			figure.showdata = y;
		});
		ribbon.setStateChange("Axes", (y) -> {
			figure.showaxes = y;
		});
		ribbon.setAction("Linear", (y) -> {
			if (y)
				figure.set_fit("linear");
		});
		ribbon.setAction("Interpolation", (y) -> {
			if (y)
				figure.set_fit("interp");
		});
		ribbon.setAction("Spline", (y) -> {
			if (y)
				figure.set_fit("spline");
		});
		ribbon.setAction("Connect", (y) -> {
			if (y)
				figure.set_fit("connect");
		});
		ribbon.setAction("None", (y) -> {
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
