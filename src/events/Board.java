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
import javax.swing.JPanel;

public class Board extends JPanel implements MouseMotionListener, MouseListener, KeyListener
 {
   private static final long serialVersionUID = 1L;
   static JFrame frame;
   static long dt;
   static Board newContentPane;
   
   CustomMenuBar ribbon;
   static Figure figure;
 
   public static void main(String[] args)
   {
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
     while (true)
     {
       update((int)dt);
 
       newContentPane.repaint();
      try
      {
        Thread.sleep(dt);
      }
       catch (InterruptedException e) {
         e.printStackTrace();
      }
    }
  }

   public Board()
   {
     setLayout(new BorderLayout());
     setFocusable(true);
     addKeyListener(this);
     
     figure = new Figure();
     
     add(figure, BorderLayout.CENTER);
     figure.addMouseListener(this);
     figure.addMouseMotionListener(this);
     
     ribbon = new CustomMenuBar();
     add(ribbon,BorderLayout.NORTH);

   }

  private static void update(int dt)
  {

  }

@Override
public void keyPressed(KeyEvent e) {
	// TODO Auto-generated method stub
	// TODO Auto-generated method stub
	if ((e.getKeyCode() == KeyEvent.VK_V) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
        figure.getImageFromClipboard();
        return;
    }
	switch(e.getKeyCode()){
	
	case KeyEvent.VK_UP: 
		figure.movepoint("up");
		break;
	case KeyEvent.VK_DOWN: 
		figure.movepoint("down");
		break;
	case KeyEvent.VK_LEFT: 
		figure.movepoint("left");
		break;
	case KeyEvent.VK_RIGHT: 
		figure.movepoint("right");
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
	boolean isrightbutton=(e.getModifiers() & InputEvent.BUTTON3_MASK)
			== InputEvent.BUTTON3_MASK;
	figure.addpoint(e.getX(), e.getY(),isrightbutton);
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
