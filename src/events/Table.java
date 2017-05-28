package events;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class Table extends JPanel implements MouseMotionListener, MouseListener, KeyListener, DocumentListener{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int x_dim = 1400;
	public static final int y_dim = 1000;
	   
	public static int nrows;
	public static int ncols;
	
	JTextArea[][] cells;
	JTextArea[] rows;
	JTextArea[] columns;
	
	Font default_font = new Font("Times", Font.PLAIN, 20);
	
	int[] row_height;
	int[] col_width;
	int[] row_pos;
	int[] col_pos;
	
	int default_width = 100;
	int default_height = 20;
	
	Color sel = new Color(240,248,255);
	
	JScrollPane scrollPane;
	JPanel sheet,col,row;
	
	int begin_drag;
	boolean drag_col=false;
	boolean drag_row=false;
	int drag_ind;
	int[] select1;
	boolean selecting=false;
	
	boolean[][] selected;

	public Table(int r, int c){
		nrows = r;
		ncols = c;
		
		addMouseMotionListener(this);
		addMouseListener(this);
		addKeyListener(this);
		
		create_sheet();
	}
	
	private void create_sheet(){
		
		sheet = new JPanel();
		col = new JPanel();
		row = new JPanel();
		sheet.setPreferredSize(new Dimension(ncols*100,nrows*20));
		col.setPreferredSize(new Dimension(ncols*100,20));
		row.setPreferredSize(new Dimension(30,nrows*20));
	    scrollPane = new JScrollPane(sheet);
	    scrollPane.setPreferredSize(new Dimension(1200,800));
	    scrollPane.setRowHeaderView(row);
	    scrollPane.setColumnHeaderView(col);
	     
		setSize(new Dimension(x_dim, y_dim));
        add(scrollPane, BorderLayout.CENTER);
		
		sheet.setLayout(null);
		row.setLayout(null);
		col.setLayout(null);
		cells = new JTextArea[nrows][ncols];
		rows = new JTextArea[nrows];
		columns = new JTextArea[ncols];
		
		row_height = new int[nrows];
		col_width = new int[ncols];
		row_pos = new int[nrows];
		col_pos = new int[ncols];
		Arrays.fill(row_height, default_height);
		Arrays.fill(col_width, default_width);
		
		selected = new boolean[nrows][ncols];
		
		for(int r=0; r<nrows;r++)
			for(int c=0; c<ncols;c++){
				cells[r][c] = new JTextArea();
				cells[r][c].setEditable(true);
				cells[r][c].setLineWrap(false);
				cells[r][c].setBorder(BorderFactory.createLineBorder(Color.black));
				sheet.add(cells[r][c]);
				cells[r][c].addMouseMotionListener(this);
				cells[r][c].addMouseListener(this);
				cells[r][c].addKeyListener(this);
				cells[r][c].getDocument().addDocumentListener(this);
			}
		for(int r=0;r<nrows;r++){
			rows[r] = new JTextArea();
			rows[r].setEditable(false);
			rows[r].setText(r+"");
			rows[r].setLineWrap(false);
			rows[r].setBorder(BorderFactory.createLineBorder(Color.black));
			row.add(rows[r]);
			rows[r].setBackground(Color.LIGHT_GRAY);
			rows[r].addMouseMotionListener(this);
			rows[r].addMouseListener(this);
			rows[r].setHighlighter(null);
		}
		for(int c=0;c<ncols;c++){
			columns[c] = new JTextArea();
			columns[c].setEditable(false);
			columns[c].setText(c+"");
			columns[c].setLineWrap(false);
			columns[c].setBorder(BorderFactory.createLineBorder(Color.black));
			col.add(columns[c]);
			columns[c].setBackground(Color.LIGHT_GRAY);
			columns[c].addMouseMotionListener(this);
			columns[c].addMouseListener(this);
			columns[c].setHighlighter(null);
		}
		align();
	}
	
	public void align(){
		
		for(int r=0; r<nrows;r++){
			if(row_height[r]<2)
				row_height[r]=2;
			if(r>=1)
				row_pos[r] = row_pos[r-1]+row_height[r-1];
			else
				row_pos[r] = 0;
			rows[r].setBounds(0, row_pos[r], 30, row_height[r]);
		}
		for(int c=0; c<ncols;c++){
			if(col_width[c]<1)
				col_width[c]=1;
			if(c>=1)
				col_pos[c] = col_pos[c-1]+col_width[c-1];
			else
				col_pos[c]=0;
			columns[c].setBounds(col_pos[c], 0, col_width[c], 20);
		}
		
		for(int r=0; r<nrows;r++)
			for(int c=0; c<ncols;c++){
				cells[r][c].setBounds(col_pos[c], row_pos[r], col_width[c], row_height[r]);
			}
		
	}
	
	public void deselect(){
		for(int r=0; r<nrows;r++)
			for(int c=0; c<ncols;c++){
				cells[r][c].setBackground(Color.WHITE);
				selected[r][c]=false;
			}
	}
	
	public void select(int r, int c){
		cells[r][c].setBackground(sel);
		selected[r][c]=true;
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		// TODO Auto-generated method stub
		if(drag_row){
			row_height[drag_ind]+=(e.getY()-begin_drag);
			begin_drag=e.getY();
			align();
		}
		if(drag_col){
			col_width[drag_ind]+=(e.getX()-begin_drag);
			begin_drag=e.getX();
			align();
		}
		if(selecting){
			deselect();
			Rectangle rect = new Rectangle(col_pos[select1[1]],row_pos[select1[0]],
					e.getX(),e.getY());
			for(int r=0; r<nrows;r++)
				for(int c=0; c<ncols;c++){
					if(rect.intersects(cells[r][c].getBounds())){
						select(r,c);
					}
				}
		}
	}
	
	public void copytoClipboard(){
		String csv = "";
		boolean rowhasdata=false;
		boolean firstelem=true;
		for(int r=0; r<nrows;r++){
			for(int c=0; c<ncols;c++){
				if(selected[r][c]){
					if(!firstelem)
						csv+="\t";
					firstelem=false;
					csv+= cells[r][c].getText();
					rowhasdata=true;
				}
			}
			if(rowhasdata)
				csv+="\n";
			rowhasdata=false;
			firstelem=true;
		}
		
		StringSelection selection = new StringSelection(csv);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	    clipboard.setContents(selection, selection);
		
	}
	
	public void input(String s, int r, int c){
		if(r>=nrows||c>=ncols)
			return;
		cells[r][c].setText(s);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		if(e.getClickCount()>1){
			int len = 0;
		    FontMetrics metrics;
			for(int c=0;c<ncols;c++){
				if(e.getSource().equals(columns[c])){
					for(int r=0;r<nrows;r++){
						metrics = cells[r][c].getFontMetrics(cells[r][c].getFont());
						len = Math.max(len,  metrics.stringWidth(cells[r][c].getText()));
					}
					col_width[c] = len+4;
					align();
					return;
				}
			}
		
			for(int r=0;r<nrows;r++){
				if(e.getSource().equals(rows[r])){
					for(int c=0;c<ncols;c++){
						metrics = cells[r][c].getFontMetrics(cells[r][c].getFont());
						len = Math.max(len,  metrics.getHeight());
					}
					row_height[r] = len+4;
					align();
					return;
				}
			}
		}
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
	public void mousePressed(MouseEvent e) {
		
		for(int i=0;i<ncols;i++){
			if(e.getSource().equals(columns[i])){
				begin_drag = e.getX();
				drag_col = true;
				drag_ind = i;
				return;
			}
		}
		
		for(int i=0;i<nrows;i++){
			if(e.getSource().equals(rows[i])){
				begin_drag = e.getY();
				drag_row = true;
				drag_ind = i;
				return;
			}
		}
		
		deselect();
		for(int r=0; r<nrows;r++)
			for(int c=0; c<ncols;c++){
				if(e.getSource().equals(cells[r][c])){
					select1 = new int[2];
					select1[0] = r;
					select1[1] = c;
					select(r,c);
					selecting=true;
				}
			}

	}
	
	public void delete_selected(){
		for(int r=0; r<nrows;r++)
			for(int c=0; c<ncols;c++)
				if(selected[r][c])
					cells[r][c].setText("");
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		drag_col=false;
		drag_row=false;
		selecting=false;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if ((e.getKeyCode() == KeyEvent.VK_C) && ((e.getModifiers() & KeyEvent.CTRL_MASK) != 0)) {
			
	        copytoClipboard();
	        return;
	    }
		if (e.getKeyCode() == KeyEvent.VK_DELETE){
			delete_selected();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
		//DO stuff
		System.out.print("insertUpdate\n");
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
	}

}


