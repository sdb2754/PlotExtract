package events;

import java.awt.BasicStroke;
import Jama.Matrix;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public class Figure extends JPanel{
	
	   /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int x_dim = 1000;
	   public static final int y_dim = 800;
	   
	   private BufferedImage background= new BufferedImage(x_dim, y_dim, 2);
	   public BufferedImage image = new BufferedImage(x_dim, y_dim, 2);
	   private Graphics draw = image.getGraphics();
	   Graphics2D g2d = (Graphics2D) draw;
	   
	   public boolean showplot=true;
	   public boolean  showorigin=true;
	   public boolean showx=true;
	   public boolean showy=true;
	   public boolean showdata=true;
	   public boolean showfit=true;
	   
	   Point origin;
	   Point x_cal;
	   Point y_cal;
	   float x_ref;
	   float y_ref;
	   float x_ori=0;
	   float y_ori=0;
	   ArrayList<Point> data = new ArrayList<Point>();
	   
	   public String fit="none";
	   
	   boolean x_log=false;
	   
	   boolean y_log=false;
	   
	   boolean move=false;
	   int sel_ind=-1;
	   
	   private int step;
	   
	   private JTextArea messages;
	   
	   Point lin1=new Point(0,0);
	   Point lin2=new Point(0,0);
	   
	   ArrayList<int[]> interp = new ArrayList<int[]>();
	   int int_ord=0;
	   
	   ArrayList<int[]> spline = new ArrayList<int[]>();
	
	   public void paintComponent(Graphics g) {
		     super.paintComponent(g);
		     Graphics2D g2 =(Graphics2D) g; 
		     g.drawImage(background, 0, 0, null);
		     if(showplot)
		    	 g2.drawImage(image, scaleimage(),null);
		     draw_fit(g2);
		     draw_points(g2);
		   }
	   
	   public Figure(){
		   
		   background.getGraphics().fillRect(0, 0, x_dim, y_dim);
		   setLayout(new BorderLayout());
		   messages = new JTextArea();
		   add(messages,BorderLayout.SOUTH);
		   set_step(0);
	   }
	   
		private AffineTransform scaleimage(){
			
			int h = image.getHeight();
			int w = image.getWidth();
			
			int s;
			
			if(w/h>x_dim/y_dim)
				s=x_dim/w;
			else
				s=y_dim/h;
			
			AffineTransform at = new AffineTransform();
	        at.translate(x_dim/2, y_dim/2);
	        
	        //at.rotate(Math.toRadians(theta));
	        at.scale(s, s);
	        
	       at.translate(-w/2, -h/2);
	       
	      return at;
		}
	   
	   public void addimage(){
		   FileDialog fd = new FileDialog(Board.frame, "Open", FileDialog.LOAD);
		   fd.setVisible(true);
		   try {
			   String path = fd.getDirectory()+fd.getFile();
			    image = ImageIO.read(new File(path));
			    set_step(1);
			} catch (IOException e) {
				set_step(0);
				 e.printStackTrace();
			}
	   }
	   
	   /**
	    * Get an image off the system clipboard.
	    * 
	    * @return Returns an Image if successful; otherwise returns null.
	    */
	   public void getImageFromClipboard()
	   {
		   
	     Transferable transferable = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
	     if (transferable != null && transferable.isDataFlavorSupported(DataFlavor.imageFlavor))
	     {
	       try
	       {
	         image= (BufferedImage) transferable.getTransferData(DataFlavor.imageFlavor);
	         set_step(1);
	       }
	       catch (UnsupportedFlavorException e)
	       {
	         // handle this as desired
	         e.printStackTrace();
	       }
	       catch (IOException e)
	       {
	         // handle this as desired
	         e.printStackTrace();
	       }
	     }
	     else
	     {
	       set_step(0);
	     }
	     return;
	   }
	   
	   private int distance(Point p, int x, int y){
		   return (int) Math.pow(Math.pow(p.x-x, 2)+Math.pow(p.y-y, 2), 0.5);
	   }
	   
	   private boolean select(int x, int y, boolean isright){
		   //dist to origin
		   String s;
		   if(distance(origin,x,y)<10)
		   {
			   sel_ind = -2;
			   Object[] possibilities = {"move", "place", "value","square"};
			   if(isright){
			   s = (String)JOptionPane.showInputDialog(
			                       Board.frame,
			                       "What would you like to do to the Origin point?:\n",
			                       "Select Origin",
			                       JOptionPane.PLAIN_MESSAGE,
			                       null,
			                       possibilities,
			                       "move");
			   }
			   else
				   s = "move";
			   switch(s){
			   case "move" :
				   move = true;
				   break;
			   case "place" :
				   set_step(1);
				   break;
			   case "value" :
				   set_origin_location();
				   break;
			   case "square" :
				   if(step>=4)
					   origin = new Point(y_cal.x,x_cal.y);
				   break;
			   }
			   return true;
		   }
		   
		   //dist to x_cal and y_cal
		   if(distance(x_cal,x,y)<10)
		   {
			   sel_ind = -3;
			   if(isright){
			   Object[] possibilities = {"move", "place", "value","square","type"};
			   s = (String)JOptionPane.showInputDialog(
			                       Board.frame,
			                       "What would you like to do to the X axis calibration point?:\n",
			                       "Calibrate X axis",
			                       JOptionPane.PLAIN_MESSAGE,
			                       null,
			                       possibilities,
			                       "move");
			   }
			   else
				   s="move";
			   switch(s){
			   case "move" :
				   move = true;
				   break;
			   case "place" :
				   set_step(2);
				   break;
			   case "value" :
				   x_ref = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
		                   "Enter known X value: ", null));
				   break;
			   case "square" :
				   x_cal = new Point(x_cal.x,origin.y);
				   break;
			   case "type" :
				   Object[] types = {"log","linear"};
				   s  = (String)JOptionPane.showInputDialog(
				                       Board.frame,
				                       "What type is the Horizontal axis?:\n",
				                       "Set X axis Type",
				                       JOptionPane.PLAIN_MESSAGE,
				                       null,
				                       types,
				                       "linear");
				   if(s=="log")
					   x_log=true;
				   else
					   x_log=false;
				   break;
			   }
			   return true;
		   }
		   if(distance(y_cal,x,y)<10)
		   {
			   sel_ind = -4;
			   if(isright){
			   Object[] possibilities = {"move", "place", "value","square","type"};
			   s = (String)JOptionPane.showInputDialog(
			                       Board.frame,
			                       "What would you like to do to the Y axis calibration point?:\n",
			                       "Calibrate Y axis",
			                       JOptionPane.PLAIN_MESSAGE,
			                       null,
			                       possibilities,
			                       "move");
			   }
			   else
				   s="move";
			   switch(s){
			   case "move" :
				   move = true;
				   break;
			   case "place" :
				   set_step(3);
				   break;
			   case "value" :
				   y_ref = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
		                   "Enter known Y value: ", null));
				   break;
			   case "square" :
				   y_cal = new Point(origin.x,y_cal.y);
				   break;
			   case "type" :
				   Object[] types = {"log","linear"};
				   s  = (String)JOptionPane.showInputDialog(
				                       Board.frame,
				                       "What type is the Vertical axis?:\n",
				                       "Set Y axis Type",
				                       JOptionPane.PLAIN_MESSAGE,
				                       null,
				                       types,
				                       "linear");
				   if(s=="log")
					   y_log=true;
				   else
					   y_log=false;
				   break;
			   }
			   return true;
		   }
		   
		   //check curve points
		   for(int i=0;i<data.size();i++)
			   if(distance(data.get(i),x,y)<10)
			   {
				   sel_ind = i;
				   if(isright){
				   Object[] possibilities = {"move", "delete","fit","new","export"};
				   s = (String)JOptionPane.showInputDialog(
				                       Board.frame,
				                       "What would you like to do to this point?:\n",
				                       "Data point",
				                       JOptionPane.PLAIN_MESSAGE,
				                       null,
				                       possibilities,
				                       "move");
				   }
				   else
					   s="move";
				   switch(s){
				   case "export":
					   export();
					   break;
				   case "move" :
					   move = true;
					   break;
				   case "delete" :
					   data.remove(i);
					   break;
				   case "fit" :
					   Object[] fit_types = {"linear","connect", "spline","interp","none"};
					   s = (String)JOptionPane.showInputDialog(
					                       Board.frame,
					                       "Select fit type:\n",
					                       "Fit type",
					                       JOptionPane.PLAIN_MESSAGE,
					                       null,
					                       fit_types,
					                       "linear");
					   set_fit(s);
					   break;
				   case "new" :
					   set_step(0);
					   break;
				   }
				   return true;
			   }
		   move=false;
		   if(sel_ind!=-1)
		   {
			   sel_ind=-1;
			   return true;
		   }
		   sel_ind=-1;
		   return false;
	   }
	   
	   public void set_fit(String s){
		   fit = s;
		   update_fit(10);
	   }
	   
	   public void addpoint(int x, int y,boolean isright){
		   //check if (un)selecting a point
		   if(select(x,y,isright))
			   return;
		   switch(step){
		   case 0:
			   addimage();
			   return;
		   case 1:
			   origin = new Point(x,y);
			   set_origin_location();
			   break;
		   case 2:
			   x_cal = new Point(x,y);
			   set_x_ref();
			   break;
		   case 3:
			   y_cal = new Point(x,y);
			   set_y_ref();
			   break;
		   case 4:
			   data.add(new Point(x,y));
			   update_fit(10);
			   break;
		   }
		   if(step<4)
			   set_step(step+1);
	   }
	   
	   private void set_origin_location(){
		   int iszero = JOptionPane.showConfirmDialog(null,
                   "Is the origin at (0,0)?", null,
                   JOptionPane.YES_NO_OPTION);
		   if(iszero != JOptionPane.YES_OPTION){
			   try{
				   x_ori = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
		                   "Enter origin X value: ", null));
		            }
			   catch(RuntimeException e){
				   x_ori=1;
		        }
			   try{
				   y_ori = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
		                   "Enter origin Y value: ", null));
		            }
			   catch(RuntimeException e){
				   y_ori=1;
		        }
		   }
	   }
	   
	   private void set_x_ref(){
		   try{
			   x_ref = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
	                   "Enter known X value: ", null));
	            }
		   catch(RuntimeException e){
			   x_ref=1;
	        }
	   }
	   
	   private void set_y_ref(){
		   try{
			   y_ref = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
	                   "Enter known Y value: ", null));
	            }
		   catch(RuntimeException e){
			   y_ref=1;
	        }
	   }
	   
	   
	   public void set_step(int i){
		   switch(i){
		   case 0:
			   data.clear();
			   origin=new Point(-1,-1);
			   x_cal=new Point(-1,-1);
			   y_cal=new Point(-1,-1);
			   image = new BufferedImage(x_dim, y_dim, 2);
			   messages.setText("Click the plot area to add an image of a graph or plot, or paste one in.");
			   break;
		   case 1:
			   data.clear();
			   origin=new Point(-1,-1);
			   x_cal=new Point(-1,-1);
			   y_cal=new Point(-1,-1);
			   messages.setText("Click the origin of the Plot/Graph.");
			   break;
		   case 2:
			   data.clear();
			   x_cal=new Point(-1,-1);
			   y_cal=new Point(-1,-1);
			   messages.setText("Click a known point on the Horizontal axis.");
			   break;
		   case 3:
			   data.clear();
			   y_cal=new Point(-1,-1);
			   messages.setText("Click a known point on the Vertical axis.");
			   break;
		   case 4:
			   data.clear();
			   messages.setText("Click to add data points along the curve of interest.");
			   break;
		   }
		   step = i;
		   move=false;
		   sel_ind=-1;
	   }
	   
	   public void movepoint(String s){
		   if(!move||sel_ind==-1)
			   return;
		   Point p;
		   switch(sel_ind){
		   case -4:
			   p = y_cal;
			   break;
		   case -3:
			   p = x_cal;
			   break;
		   case -2:
			   p=origin;
			   break;
		   default:
			   p=data.get(sel_ind);
			   break;
		   }
		   switch(s){
		   case "up": 
			   p.translate(0, -1);
			   break;
		   case "down": 
			   p.translate(0, 1);
			   break;
		   case "left": 
			   p.translate(-1, 0);
			   break;
		   case "right": 
			   p.translate(1, 0);
			   break;
		   }
		   update_fit(10);
	   }
	   
	   private void sort_data(){
		// Sorting
		   Collections.sort(data, new Comparator<Point>() {
				@Override
				public int compare(Point p1, Point p2) {
					// TODO Auto-generated method stub
					return p1.x-p2.x;
				}
		       });
	   }
	   
	   private void update_fit(int dx){
		   sort_data();
		   int n = data.size();
		   int[] pnt = new int[2];
		   int xa = data.get(0).x;
		   int xb = data.get(n-1).x;
		   double p;
		   switch(fit){
		   case "linear" :
		   //linear
		   long sumy = 0;
		   long sumxx=0;
		   long sumyy=0;
		   long sumx=0;
		   long sumxy=0;
		   if(n>1){
		   for(Point pt : data){
			   sumy = sumy + pt.y;
			   sumx = sumx + pt.x;
			   sumxx = sumxx + pt.x*pt.x;
			   sumyy = sumyy + pt.y*pt.y;
			   sumxy = sumxy + pt.x*pt.y;
		   }
		   
		   double a = ((double)(sumy*sumxx-sumx*sumxy))/((double)(n*sumxx-sumx*sumx));
		   
		   double b = ((double)(n*sumxy-sumx*sumy))/((double)(n*sumxx-sumx*sumx));
		   lin1 = new Point(origin.x,(int) (a+b*origin.x));
		   lin2 = new Point(data.get(n-1).x,(int) (a+b*data.get(n-1).x));
		   }
		   break;
		   case "interp" :
		   //interpolation
		   interp.clear();
		   p=0;
		   double pi;
		   for(int x=xa;x<xb+dx;x+=dx){
			   p=0;
			   for(int i=0;i<n;i++)
			   {
				   pi=1;
				   for(int j=0;j<n;j++)
					   if(i!=j){
						   pi = pi*(((double)(x-data.get(j).x))/((double)(data.get(i).x-data.get(j).x)));
					   }
				   p=p+pi*data.get(i).y;
			   }
			   pnt[0]=x;
			   pnt[1]=(int) p;
			   interp.add(pnt.clone());
		   }
		   break;
		   case "spline" :
			   double[][] M = new double[n][n];
			   for(int i=0;i<n;i++)
				   for(int j=0;j<n;j++){
					   M[i][j]=0;
					   if(i==j)
						   M[i][j]=4;
					   if(((int)Math.abs(i-j))==1)
						   M[i][j]=1;
				   }
			   M[0][0]=2;
			   M[n-1][n-1]=2;
			   
			   double[][] bv = new double[n][1];
			   
			   for(int i=1;i<n;i++){
				   bv[i][0] = 3*(data.get(i-1).y-data.get(i).y);
			   }
			   
			   Matrix A = new Matrix(M);
			   Matrix B = new Matrix(bv);
			   Matrix D = A.solve(B);
			   spline.clear();
			   int pc = 0;
			   double t=0;
			   long c;
			   long d;
			   p=0;
			   for(int x=xa;x<xb;x+=dx){
				   if(x>=data.get(pc+1).x)
					   pc++;
				   pnt[0] = x;
				   t = ((double)(x-data.get(pc).x))/((double)(data.get(pc+1).x-data.get(pc).x));
				   c= (int) (3*(data.get(pc+1).y-data.get(pc).y) - 2*D.get(pc, 0) - D.get(pc+1, 0));
				   d = (int) (2*(data.get(pc).y-data.get(pc+1).y) + D.get(pc, 0) + D.get(pc+1, 0));
				   pnt[1] = (int) (data.get(pc).y + D.get(pc, 0) * t + c*Math.pow(t, 2) + d*Math.pow(t, 3));
				   System.out.print("x:"+pnt[0]+" y:"+pnt[1]+"\n");
				   spline.add(pnt.clone());
			   }
			   break;
		   }
	   }
	   
	   private void draw_fit(Graphics2D g){
		   if(step<4||!showfit)
			   return;
		   switch(fit){
		   
		   case "linear":
			   g.drawLine(lin1.x,lin1.y,lin2.x,lin2.y);
			   break;
			   
		   case "connect" :
			   for(int i=1;i<data.size();i++){
				   g.drawLine(data.get(i-1).x, data.get(i-1).y, data.get(i).x,data.get(i).y);
			   }
			   break;
		   case "interp" :
			   for(int i=1;i<interp.size();i++){
				   g.drawLine(interp.get(i-1)[0], interp.get(i-1)[1], interp.get(i)[0], interp.get(i)[1]);
			   }
			   break;
		   case "spline" :
			   for(int i=1;i<spline.size();i++){
				   g.drawLine(spline.get(i-1)[0], spline.get(i-1)[1], spline.get(i)[0], spline.get(i)[1]);
			   }
			   break;
		   }
	   }
	   
	   private void draw_points(Graphics2D g){
		   
		   g.setStroke(new BasicStroke(10));
		   g.setColor(Color.RED);
		   if(step>=1&&showorigin){

		   g.drawLine(origin.x, origin.y, origin.x, origin.y);
		   }
		   
		   if(step>=2&&showx){
		   g.drawLine(x_cal.x, x_cal.y, x_cal.x, x_cal.y);
		   }
		   if(step>=3&&showy){
		   g.drawLine(y_cal.x, y_cal.y, y_cal.x, y_cal.y);
		   }
		   if(step>=4&&showdata){
		   g.setColor(Color.BLUE);
		   for (Point pnt : data){
			   g.drawLine(pnt.x, pnt.y, pnt.x, pnt.y);
		   }
		   g.setColor(Color.gray);
		   g.setStroke(new BasicStroke(2));
		   g.drawLine(origin.x, origin.y, x_cal.x, x_cal.y);
		   g.drawLine(origin.x, origin.y, y_cal.x, y_cal.y);
		   g.setStroke(new BasicStroke(10));
		   }
		   g.setColor(Color.GREEN);
		   switch(sel_ind){
		   case -4: g.drawLine(y_cal.x, y_cal.y, y_cal.x, y_cal.y);break;
		   case -3: g.drawLine(x_cal.x, x_cal.y, x_cal.x, x_cal.y);break;
		   case -2: g.drawLine(origin.x, origin.y, origin.x, origin.y);break;
		   }
		   if(sel_ind>=0)
			   g.drawLine(data.get(sel_ind).x, data.get(sel_ind).y, data.get(sel_ind).x, data.get(sel_ind).y);
	   }
	   
	   public void export(){
		   if(step<4)
			   return;
		   update_fit(1);
		   ArrayList<int[]> export = new ArrayList<int[]>();
		   int[] pnt=new int[2];
		   switch(fit){
		   		
		   case "linear" :
			   double m = ((double)(lin2.y-lin1.y))/((double)(lin2.x-lin1.x));
			   int b = lin1.y;
			   for(int i=data.get(0).x;i<=data.get(data.size()-1).x;i++){
				   pnt[0]=i;
				   pnt[1] = (int) (m*i+b);
				   export.add(pnt.clone());
			   }
			   break;
			   
		   case "spline" :
			   export = spline;
			   break;
			   
		   case "interp" :
			   export = interp;
			   break;
			   
		   case "none" :
			   for(int i=0;i<data.size();i++){
				   pnt[0] = data.get(i).x;
				   pnt[1] = data.get(i).y;
				   export.add(pnt.clone());
			   }
			   break;
		   
		   }
		   
		   //map to new coordinate system
		   float[][] output = new float[export.size()][2];
		   
		   for(int i = 0; i<export.size();i++){
			   
			   output[i][0] = (((float)(export.get(i)[0]-origin.x))/((float)(x_cal.x-origin.x)))*(x_ref-x_ori)+x_ori;
			   output[i][1] = (((float)(export.get(i)[1]-origin.y))/((float)(y_cal.y-origin.y)))*(y_ref-y_ori)+y_ori;
			   
		   }
		   
		   FileDialog fd = new FileDialog(Board.frame, "Save", FileDialog.LOAD);
		   fd.setVisible(true);
		   try {
			   String path = fd.getDirectory()+fd.getFile();
			   if(!path.contains("."))
				   path = path+".csv";
			   BufferedWriter br = new BufferedWriter(new FileWriter(path));
			   StringBuilder sb = new StringBuilder();
			   for (float[] element : output) {
			    sb.append(Float.toString(element[0]));
			    sb.append(",");
			    sb.append(Float.toString(element[1]));
			    sb.append("\n");
			   }
			   br.write(sb.toString());
			   br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		   update_fit(10);
	   }


}
