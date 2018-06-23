package events;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public abstract class ZoomPane extends JPanel implements MouseWheelListener, ComponentListener {

	/**
	 * A zoomable JPanel which uses MouseScroll events to control it.
	 * MouseScroll events are declared final because the Panel relies on them.
	 * The componentResized method of ComponentListener is also final because the Panel relies of them.
	 * 
	 * Mouse scroll shifts up and down
	 * Ctrl + scroll zooms
	 * Shift + scroll shifts left and righta
	 */
	private static final long serialVersionUID = 1L;

	// The transform for zooming and shifting
	private AffineTransform at;
	// A custom JPanel that captures MouseEvents and transforms them before
	// passing them on to this
	private TransformGlassPanel glass;

	// Paint
	// Had to use Paint instead of PaintComponent. Not sure why.
	@Override
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		g2.transform(at);
		// call super paint method with the transformation applied
		super.paint(g2);
	}

	// Constructor
	public ZoomPane() {
		this.setLayout(new BorderLayout());
		addComponentListener(this);
		addMouseWheelListener(this);
		at = new AffineTransform();
		glass = new TransformGlassPanel(this);
		// Add the glass pane which captures mouse events
		super.add(glass, BorderLayout.CENTER);
	}

	// scaling methods
	public void setMagnification(double m) {
		double mag = at.getScaleX();// X and Y scale should always be the same
		if (m > 0)
			mag = m;
		else
			mag = 0.01;
		at.scale(mag * 1 / at.getScaleX(), mag * 1 / at.getScaleY());
	}

	public void adjustMagnification(double dm) {
		setMagnification(at.getScaleX() + dm);
	}

	public void setPosition(int x, int y) {
		shiftPosition(x - (int) at.getTranslateX(), y - (int) at.getTranslateY());
	}

	public void centerPosition() {
		JFrame topFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
		setLocation((topFrame.getWidth() - (int) this.getWidth()) / 2,
				(topFrame.getHeight() - (int) this.getHeight()) / 2);
	}

	public void shiftPosition(int dx, int dy) {
		double mag = at.getScaleX();
		setMagnification(1);
		at.translate(dx, dy);
		setMagnification(mag);
	}

	// Coordinate System Transforms

	// Apply transformation to Mouse Event
	private MouseEvent transformedMouseEvent(MouseEvent e) {
		Point2D np = getTransformedPoint(new Point(e.getX(), e.getY()));
		return new MouseEvent(this, e.getID(), e.getWhen(), e.getModifiers(), (int) np.getX(), (int) np.getY(),
				e.getClickCount(), e.isPopupTrigger(), e.getButton());
	}

	// Apply inverse transformation to Point2D
	public Point2D getTransformedPoint(Point2D p) {
		try {
			return at.inverseTransform(p, null);
		} catch (NoninvertibleTransformException e1) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	// Apply transformation to Point2D
	public Point2D getContainerPoint(Point2D p) {
		return at.transform(p, null);
	}

	/**
	 * Zooming and shifting is done with mouse wheel scroll shifts vertically
	 * Shift+scroll shifts horizontally Ctrl+scroll zooms
	 */
	@Override
	public final void mouseWheelMoved(MouseWheelEvent e) {
		if ((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
			Point2D pc = getTransformedPoint(e.getPoint());
			adjustMagnification(-e.getPreciseWheelRotation() / 10);
			Point2D pf = getContainerPoint(pc);
			shiftPosition(e.getX() - (int) pf.getX(), e.getY() - (int) pf.getY());
		} else if ((e.getModifiers() & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK) {
			shiftPosition(-10 * e.getWheelRotation(), 0);
		} else {
			shiftPosition(0, -10 * e.getWheelRotation());
		}

	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	// Need to resize glass panel if zoom panel changes size. Not sure why.
	@Override
	public final void componentResized(ComponentEvent arg0) {
		System.out.print("Resizing\n");
		glass.setSize(this.getSize());
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	// GlassPane class which captures incoming mouse events in the JPanel,
	// transforms them, and sends them to ZoomPanel
	private static class TransformGlassPanel extends JPanel implements MouseListener, MouseMotionListener {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		ZoomPane zoomPanel;

		// Constructor
		TransformGlassPanel(ZoomPane zp) {
			zoomPanel = zp;
			addMouseListener(this);
			addMouseMotionListener(this);
			// This JPanel doesn't display anything. It is a glass pane
			this.setOpaque(false);
		}

		// Capture incoming Mouse Event
		@Override
		public void mouseDragged(MouseEvent e) {
			// Dispatch the event to ZoomPanel after transforming the x and y
			// coordinates
			zoomPanel.dispatchEvent(zoomPanel.transformedMouseEvent(e));
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			zoomPanel.dispatchEvent(zoomPanel.transformedMouseEvent(e));
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			zoomPanel.dispatchEvent(zoomPanel.transformedMouseEvent(e));
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			zoomPanel.dispatchEvent(zoomPanel.transformedMouseEvent(e));
		}

		@Override
		public void mouseExited(MouseEvent e) {
			zoomPanel.dispatchEvent(zoomPanel.transformedMouseEvent(e));
		}

		@Override
		public void mousePressed(MouseEvent e) {
			zoomPanel.dispatchEvent(zoomPanel.transformedMouseEvent(e));
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			zoomPanel.dispatchEvent(zoomPanel.transformedMouseEvent(e));
		}

	}

}
