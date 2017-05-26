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
	public static final int x_dim = 1400;
	   public static final int y_dim = 1000;
	   
	   //drawing stuff
	   private BufferedImage background= new BufferedImage(x_dim, y_dim, 2);
	   public BufferedImage image = new BufferedImage(x_dim, y_dim, 2);
	   private Graphics draw = image.getGraphics();
	   Graphics2D g2d = (Graphics2D) draw;
	   private AffineTransform at;

	   //Layers
	   public boolean showplot=true;
	   public boolean  showorigin=true;
	   public boolean showx=true;
	   public boolean showy=true;
	   public boolean showdata=true;
	   public boolean showfit=true;
	   public boolean showxlog=true;
	   public boolean showylog=true;
	   
	   //Fit stuff
	   int npoints;
	   String formula;
	   public String fit="none";
	   ArrayList<Point> fitdata = new ArrayList<Point>();
	   int order=3;
	   String x_header;
	   String y_header;
	   
	   //Calibration
	   Point origin;
	   Point x_cal;
	   Point y_cal;
	   float x_ref;
	   float y_ref;
	   float x_ori=0;
	   float y_ori=0;
	   boolean x_log=false;
	   boolean y_log=false;
	   
	   //data
	   ArrayList<Point> data = new ArrayList<Point>();
	   
	   //Moving
	   boolean move=false;
	   Point selected;
	   
	   //messages
	   private int step;
	   private JTextArea messages;
	
	   //Paint
	   public void paintComponent(Graphics g) {
		     super.paintComponent(g);
		     Graphics2D g2 =(Graphics2D) g; 
		     g.drawImage(background, 0, 0, null);
		     if(showplot)
		    	 g2.drawImage(image, at,null);
		     draw_fit(g2);
		     draw_points(g2);
		   }
	   
	   //Constructor
	   public Figure(){
		   
		   at = new AffineTransform();
		   
		   background.getGraphics().fillRect(0, 0, x_dim, y_dim);
		   setLayout(new BorderLayout());
		   messages = new JTextArea();
		   add(messages,BorderLayout.SOUTH);
		   set_step(0);
	   }
	   
	   
	   //Scale image to fit in plot area
		private void scaleimage(){
			
			at.setToIdentity();
			
			int h = image.getHeight();
			int w = image.getWidth();
			
			float s;
			
			if((float)w/h>(float)x_dim/y_dim)
				s=(float)x_dim/((float)1.2*w);
			else
				s=(float)y_dim/((float)1.2*h);

	        at.translate(x_dim/2, y_dim/2);
	        
	        //at.rotate(Math.toRadians(theta));
	        at.scale(s, s);
	        
	       at.translate(-w/2, -h/2);
		}
		
	   
		//Add image from file
	   public void addimage(){
		   FileDialog fd = new FileDialog(Board.frame, "Open", FileDialog.LOAD);
		   fd.setVisible(true);
		   try {
			   String path = fd.getDirectory()+fd.getFile();
			    image = ImageIO.read(new File(path));
		    	scaleimage();
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
	    	 scaleimage();
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
	   
	   //Distance between a point and a pair if integer coordinates
	   private int distance(Point p, int x, int y){
		   return (int) Math.pow(Math.pow(p.x-x, 2)+Math.pow(p.y-y, 2), 0.5);
	   }
	   
	   //Select a point
	   private boolean select(int x, int y, boolean isright){
		   String s="";
		   
		   //dist to origin
		   if(distance(origin,x,y)<10)
		   {
			   selected = origin;
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
			   switch(s){
			   case "move":
				   move = true;
				   break;
			   case "place":
				   set_step(1);
				   break;
			   case "value":
				   set_origin_location();
				   break;
			   case "square":
				   if(step>=4)
					   origin.setLocation(y_cal.x,x_cal.y);
				   break;
			   }
			   }
			   else
				   move = true;
			   return true;
		   }
		   
		   //dist to x_cal and y_cal
		   if(distance(x_cal,x,y)<10)
		   {
			   selected = x_cal;
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
			   switch(s){
			   case "move":
				   move = true;
				   break;
			   case "place" :
				   set_step(2);
				   break;
			   case "value" :
				   set_x_ref();
				   break;
			   case "square" :
				   x_cal.setLocation(x_cal.x,origin.y);
				   break;
			   case "type" :
				   set_type_x();
				   break;
			   }
			   }
			   else
				   move = true;
			   return true;
		   }
		   if(distance(y_cal,x,y)<10)
		   {
			   selected = y_cal;
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
			   switch(s){
			   case "move" :
				   move = true;
				   break;
			   case "place" :
				   set_step(3);
				   break;
			   case "value" :
				   set_y_ref();
				   break;
			   case "square" :
				   y_cal.setLocation(origin.x,y_cal.y);
				   break;
			   case "type" :
				   set_type_y();
				   break;
			   }
			   }
			   else
				   move=true;

			   return true;
		   }
		   //check curve points
		   for(int i=0;i<data.size();i++)
			   if(distance(data.get(i),x,y)<10)
			   {
				   selected = data.get(i);
				   if(isright){
				   Object[] possibilities = {"move", "delete","fit","new","export","coordinate","formula"};
				   s = (String)JOptionPane.showInputDialog(
				                       Board.frame,
				                       "What would you like to do to this data?:\n",
				                       "Data point",
				                       JOptionPane.PLAIN_MESSAGE,
				                       null,
				                       possibilities,
				                       "move");
				   switch(s){
				   case "export":
					   export();
					   break;
				   case "move":
					   move = true;
					   break;
				   case "delete":
					   delete(i);
					   break;
				   case "fit" :
					   choose_fit();
					   break;
				   case "new" :
					   set_step(0);
					   break;
				   case "coordinate" :
					   disp_coordinate(data.get(i));
					   break;
				   case "formula" :
					   disp_formula();
					   break;
				   }
				   }
				   else
					   move=true;

				   return true;
			   }
		   move=false;
		   if(selected!=null)
		   {
			   selected=null;
			   return true;
		   }
		   selected=null;
		   return false;
	   }
	   
	   private void disp_coordinate(Point p){
		   JOptionPane.showMessageDialog(Board.frame, 
				   "Point coordinate: ("+get_x(p.x)+","+get_y(p.y)+")");
	   }
	   
	   private void disp_formula(){
		   JOptionPane.showMessageDialog(Board.frame, 
				   "The formula of the fit is: "+formula);
	   }
	   
	   private void choose_fit(){
		   String s;
		   Object[] fit_types = {"linear","connect", "spline","interp","regression","none"};
		   s = (String)JOptionPane.showInputDialog(
		                       Board.frame,
		                       "Select fit type:\n",
		                       "Fit type",
		                       JOptionPane.PLAIN_MESSAGE,
		                       null,
		                       fit_types,
		                       "linear");
		   set_fit(s);
	   }
	   
	   private void delete(int i){
		   data.remove(i);
		   selected = null;
		   update_fit(10);
	   }
	   
	   private void set_type_x(){
		   Object[] types = {"log","linear"};
		   String s;
		   s  = (String)JOptionPane.showInputDialog(
		                       Board.frame,
		                       "What type is the Horizontal axis?:\n",
		                       "Set X axis type",
		                       JOptionPane.PLAIN_MESSAGE,
		                       null,
		                       types,
		                       "linear");
		   if(s=="log"){
			   x_log=true;
		   }
		   else{
			   x_log=false;
		   }
	   }
	   private void set_type_y(){
		   Object[] types = {"log","linear"};
		   String s;
		   s  = (String)JOptionPane.showInputDialog(
		                       Board.frame,
		                       "What type is the Vertical axis?:\n",
		                       "Set Y axis type",
		                       JOptionPane.PLAIN_MESSAGE,
		                       null,
		                       types,
		                       "linear");
		   if(s=="log"){
			   y_log=true;
		   }
		   else{
			   y_log=false;
		   }
	   }
	   
	   public void set_fit(String s){
		   if(s=="regression"){
			   try{
				   order = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
		                   "Enter regression order: ", null));
				   if(order<1)
					   order=1;
		            }
			   catch(RuntimeException e){
				   order=3;
		        }
		   }
		   fit = s;
		   update_fit(10);
	   }
	   
	   public void addpoint(int x, int y,boolean isright){
		   //check if un/selecting a point
		   if(select(x,y,isright))
			   return;
		   
		   if(isright){
			   choose_fit();
			   return;
		   }
		   
		   switch(step){
		   case 0:
			   addimage();
			   break;
		   case 1:
			   set_step(2);
			   origin = new Point(x,y);
			   set_origin_location();
			   break;
		   case 2:
			   set_step(3);
			   x_cal = new Point(x,y);
			   set_x_ref();
			   break;
		   case 3:
			   set_step(4);
			   y_cal = new Point(x,y);
			   set_y_ref();
			   break;
		   case 4:
			   data.add(new Point(x,y));
			   update_fit(10);
			   break;
		   }
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
				   x_ori=origin.x;
		        }
			   try{
				   y_ori = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
		                   "Enter origin Y value: ", null));
		            }
			   catch(RuntimeException e){
				   y_ori=origin.y;
		        }
		   }
	   }
	   
	   private void set_x_ref(){
		   try{
			   x_ref = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
	                   "Enter known X value: ", null));
	            }
		   catch(RuntimeException e){
			   x_ref=x_cal.x;
	        }
	   }
	   
	   private void set_y_ref(){
		   try{
			   y_ref = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
	                   "Enter known Y value: ", null));
	            }
		   catch(RuntimeException e){
			   y_ref=y_cal.y;
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
			   messages.setText("Click to add data points along the curve of interest…Then click ‘Fit’ to select curve fit.");
			   break;
		   }
		   step = i;
		   move=false;
		   selected=null;
	   }
	   
	   public void movepoint(int x, int y){
		   if(!move||selected==null)
			   return;
		   selected.translate(x, y);
		   update_fit(10);
	   }
	   
	   private void sort_data(){
		//Sorting
		   Collections.sort(data, new Comparator<Point>() {
				@Override
				public int compare(Point p1, Point p2) {
					// TODO Auto-generated method stub
					return p1.x-p2.x;
				}
		       });
	   }
	   
	   private void interpolation_fit( int dx){
		   int n = data.size();
		   int xa = data.get(0).x;
		   int xb = data.get(n-1).x;
		   
		   //interpolation
		   fitdata.clear();
		   double p=0;
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
			   fitdata.add(new Point(x,(int)p));
		   }
		   
		   formula = "formula not yet avialable for interpolation fit type.";
		   
	   }
	   
	   private void linear_fit( int dx){
		   int n = data.size();
		   int xa = data.get(0).x;
		   int xb = data.get(n-1).x;
		   
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
		   
		   double b = ((double)(sumy*sumxx-sumx*sumxy))/((double)(n*sumxx-sumx*sumx));
		   double m = ((double)(n*sumxy-sumx*sumy))/((double)(n*sumxx-sumx*sumx));
		   fitdata.clear();
		   for(int i=xa;i<=xb;i+=dx){
			   fitdata.add(new Point(i,(int) (m*i+b)));
		   }
		   
		   m = (get_y(fitdata.get(n).y)-get_y(fitdata.get(0).y))/(get_x(fitdata.get(n).x)-get_x(fitdata.get(0).x));
		   formula = "f(x) = "+m+"*x + "+get_y((int) b);
		   }
		   else
			   formula = "at least two points are needed to produce a linear fit.";

	   }
	   
	   private void spline_fit(int dx){
		   
		   formula = "Formula unavailable for spline fit.";
		   
		   int n = data.size();
		   int xa = data.get(0).x;
		   int xb = data.get(n-1).x;
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
		   fitdata.clear();
		   int pc = 0;
		   double t=0;
		   long c;
		   long d;
		   for(int x=xa;x<xb;x+=dx){
			   if(x>=data.get(pc+1).x)
				   pc++;
			   t = ((double)(x-data.get(pc).x))/((double)(data.get(pc+1).x-data.get(pc).x));
			   c= (int) (3*(data.get(pc+1).y-data.get(pc).y) - 2*D.get(pc, 0) - D.get(pc+1, 0));
			   d = (int) (2*(data.get(pc).y-data.get(pc+1).y) + D.get(pc, 0) + D.get(pc+1, 0));
			   fitdata.add(new Point(x,(int) (data.get(pc).y + D.get(pc, 0) * t + c*Math.pow(t, 2) + d*Math.pow(t, 3))));
		   }
	   }
	   
	   private void regression_fit( int dx){
		   int n = data.size();
		   int xa = data.get(0).x;
		   int xb = data.get(n-1).x;
		   int k=order+1;
		   double[][] Y = new double[n][1];
		   double[][] X = new double[n][k];
		   for(int i=0;i<n;i++){ 
			   Y[i][0] = data.get(i).y;
			   for(int j=0;j<k;j++){		   
				   X[i][j] = Math.pow(data.get(i).x, j);
			   }
		   }
		   Matrix mY = new Matrix(Y);
		   Matrix mX = new Matrix(X);
		   Matrix ma = ((mX.transpose().times(mX)).solve((mX.transpose().times(mY))));
		   fitdata.clear();
		   double sum = 0;
		   for(int x=xa;x<xb;x+=dx){
			   for(int j=0;j<k;j++){
				   sum+=ma.get(j,0)*Math.pow(x, j);
			   }
			   fitdata.add(new Point(x,(int) sum));
			   sum=0;
		   }
		   
		   formula = "f(x) = ";
		   for(int i=order;i>0;i--){
			   formula+= ma.get(i, 0)+"*x^"+i+" + ";
		   }
		   formula+=ma.get(0, 0);
	   }
	   
	   private void connect_fit(int dx){
		   formula = "Formula unavailable for connect fit.";
	   }
	   
	   private void none_fit(int dx){
		   formula = "No fit type selected.";
		   fitdata.clear();
		   for(Point p : data)
			   fitdata.add(p);
	   }
	   
	   private void update_fit(int dx){
		   sort_data();
		   switch(fit){
		   case "linear" :
			   linear_fit(dx);
		   break;
		   case "interp" :
			   interpolation_fit(dx);
		   break;
		   case "spline" :
			   spline_fit(dx);
			   break;
		   case "regression":
			   regression_fit(dx);
			   break;
		   case "connect":
			   connect_fit(dx);
			   break;
		   case "none":
			   none_fit(dx);
		   }
	   }
	   
	   private void draw_fit(Graphics2D g){
		   if(step<4||!showfit||fit=="none")
			   return;
		   for(int i=1;i<fitdata.size();i++){
			   g.drawLine(fitdata.get(i-1).x, fitdata.get(i-1).y, fitdata.get(i).x, fitdata.get(i).y);
		   }
	   }
	   
	   private void draw_points(Graphics2D g){
		   g.setStroke(new BasicStroke(10));
		   g.setColor(Color.RED);
		   if(step>1&&showorigin){

		   g.drawLine(origin.x, origin.y, origin.x, origin.y);
		   }
		   
		   if(step>2&&showx){
		   g.drawLine(x_cal.x, x_cal.y, x_cal.x, x_cal.y);
		   }
		   if(step>3&&showy){
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
		   if(selected!=null)
			   g.drawLine(selected.x, selected.y, selected.x, selected.y);
	   }
	   
	   private int  get_npoints(){
		   try{
			   npoints = Integer.parseInt(JOptionPane.showInputDialog(Board.frame,
	                   "Enter number of interpolation points to export: ", null));
	            }
		   catch(RuntimeException e){
			   npoints=data.size();
	        }
		   return (data.get(data.size()-1).x-data.get(0).x)/(npoints-1);
	   }
	   
	   private void get_headers(){
		   
		   try{
			   x_header = JOptionPane.showInputDialog(Board.frame,
	                   "Enter Horizontal Axis label: ", null);
	            }
		   catch(RuntimeException e){
			   x_header="Abscissa";
	        }
		   try{
			   y_header = JOptionPane.showInputDialog(Board.frame,
	                   "Enter Vertical Axis label: ", null);
	            }
		   catch(RuntimeException e){
			   y_header="Ordinate";
	        }
	   }
	   
	   public void export(){
		   
		   if(step<4)
			   return;
		   
		   if(fit!="none"){
			   int npoints = get_npoints();
			   get_headers();
			   update_fit(npoints);
		   }
		   
		   FileDialog fd = new FileDialog(Board.frame, "Save", FileDialog.LOAD);
		   fd.setVisible(true);
		   try {
			   String path = fd.getDirectory()+fd.getFile();
			   if(!path.contains("."))
				   path = path+".csv";
			   BufferedWriter br = new BufferedWriter(new FileWriter(path));
			   StringBuilder sb = new StringBuilder();
			   sb.append(x_header+","+y_header+"\n");
			   for (Point p : fitdata) {
			    sb.append(Float.toString((float) get_x(p.x)));
			    sb.append(",");
			    sb.append(Float.toString((float) get_y(p.y)));
			    sb.append("\n");
			   }
			   br.write(sb.toString());
			   br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		   update_fit(10);
	   }

	   	private double get_x(int x){
	   		double m,b;
	   		if(x_log){
		   		m = ((Math.log10(x_ref)-Math.log10(x_ori))/(x_cal.x-origin.x));
	   			b = (Math.log10(x_ref) - m*x_cal.x);
	   			return Math.pow(10, m*x+b);	
	   		}
	   		m = (x_ref-x_ori)/(x_cal.x-origin.x);
   			b = x_ref - m*x_cal.x;
   			return m*x+b;
	   	}
	   	private double get_y(int y){
	   		double m,b;
	   		if(y_log){
		   		m = ((Math.log10(y_ref)-Math.log10(y_ori))/(y_cal.y-origin.y));
	   			b = (Math.log10(y_ref) - m*y_cal.y);
	   			return Math.pow(10, m*y+b);  			
	   		}
	   		m = (y_ref-y_ori)/(y_cal.y-origin.y);
   			b = y_ref - m*y_cal.y;
   			return m*y+b;
	   	}

}
