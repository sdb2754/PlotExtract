package events;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.function.Consumer;

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
	
	ArrayList<cGroup> groups = new ArrayList<cGroup>();
	ArrayList<cMenu> menus = new ArrayList<cMenu>();
	   
	   CustomMenuBar(){
	   }
	   
	   public void add_item(String name, String parent, String type){
		   add_item(name,parent,type,"none");
	   }
	   
	   public void add_item(String name, String parent,String type, String group){
		   
		   Component cp = get(parent);
		   
		   switch(type){
		   
		   case "menu":
			   if(cp == this){
				   cMenu cm = new cMenu(name);
				   this.add(cm);
				   cm.addActionListener(this);
				   menus.add(cm);
				   return;
			   }
			   cMenu cm = new cMenu(name);
			   ((cMenu) cp).add_item(cm, name, type, group);
			   cm.addActionListener(this);		   
			   break;
		   case "item":
			   cItem ci = new cItem(name);
			   ((cMenu) cp).add_item(ci, name, type, group);
			   ci.addActionListener(this);	
			   break;
		   case "radio":
			   cRadio cr = new cRadio(name);
			   ((cMenu) cp).add_item(cr, name, type, group);
			   cr.addActionListener(this);	
			   cr.addItemListener(this);
			   if(group!="none")
				   cr.group(group(group));
			   break;
		   case "check":
			   cCheck cc = new cCheck(name);
			   ((cMenu) cp).add_item(cc, name, type, group);
			   cc.addActionListener(this);
			   cc.addItemListener(this);
			   break;
		   case "group":
			   groups.add(new cGroup(name));
			   break;
		   
		   }
	   }
	   
	   public void set_action(String name, Consumer<Boolean> a){
		   Component c = get(name);
			if(c.getClass().equals(cMenu.class)){
				((cMenu) c).set_action(a);
				return;
			}
			if(c.getClass().equals(cItem.class)){
				((cItem) c).set_action(a);
				return;
			}
			if(c.getClass().equals(cCheck.class)){
				((cCheck) c).set_action(a);
				return;
			}
			if(c.getClass().equals(cRadio.class)){
				((cRadio) c).set_action(a);
				return;
			}
	   }
	   
	   
	   private Component get(String name){
		   
		   if(name=="bar")
			   return this;
		   for(cMenu cm : menus){
			   if(cm.name.equals(name))
				   return cm;
		   }
		   Component cp = null;
		   for(cMenu cm : menus){
			   cp = cm.get(name);
			   if(cp!=null)
				   return cp;
		   }

		   return null;
	   }
	   
	   private cGroup group(String name){
		   for(cGroup cg : groups){
			   if(cg.name.equals(name))
				   return cg;
		   }
		   return null;
	   }
	   
	   
	   private class cMenu extends JMenu{
		private static final long serialVersionUID = 1L;
		String name;
		
		private Consumer<Boolean> action;
		
		   ArrayList<cMenu> menus = new ArrayList<cMenu>();
		   ArrayList<cItem> items = new ArrayList<cItem>();
		   ArrayList<cCheck> checkboxes = new ArrayList<cCheck>();
		   ArrayList<cRadio> radiobuttons = new ArrayList<cRadio>();
		   public cMenu( String s){
			   name = s;
			   this.setText(name);
			   action = (y) -> {};
		   }
		   
		   public Component get(String name){
			   
			   for(cMenu cm : menus){
				   if(cm.name.equals(name))
					   return cm;
			   }
			   
			   for(cItem ci : items){
				   if(ci.name.equals(name))
					   return ci;
			   }
			   
			   for(cCheck cc : checkboxes){
				   if(cc.name.equals(name))
					   return cc;
			   }
			   
			   for(cRadio cr : radiobuttons){
				   if(cr.name.equals(name))
					   return cr;
			   }
			   
			   Component cp = null;
			   for(cMenu cm : menus){
				   cp = cm.get(name);
				   if(cp!=null)
					   return cp;
			   }
			
			   return null;
		   }
		   
		   public void add_item(Component c, String name, String type, String group){

			   switch(type){
			   
			   case "menu":
				   menus.add((cMenu) c);
				   break;
			   case "item":
				   items.add((cItem) c);
				   break;
			   case "radio":
				   radiobuttons.add((cRadio) c);
				   break;
			   case "check":
				   checkboxes.add((cCheck) c);
				   break;
			   }
			   add(c);
		   }
			public void set_action(Consumer<Boolean> c){
				action = c;
			}
			
			public void act(boolean b){
				action.accept(b);
			}
			
	   }
	   
	   private class cItem extends JMenuItem{
		private static final long serialVersionUID = 1L;
		String name;
		
		private Consumer<Boolean> action;
		
		public cItem( String s){
			name = s;
			this.setText(name);
			
			action = (y) -> {};
			
			}
		
		public void set_action(Consumer<Boolean> c){
			action = c;
		}
		public void act(boolean b){
			action.accept(b);
		}
		   
	   }
	   
	   private class cCheck extends JCheckBoxMenuItem{
		private static final long serialVersionUID = 1L;
		String name;
		
		Consumer<Boolean> action;
		
		
		   public cCheck( String s){
			   name = s;
			   this.setText(name);
			   action = (y) -> {};
		   }
		   
		   public void set_action(Consumer<Boolean> c){
				action = c;
			}
		   public void act(boolean b){
				action.accept(b);
			}
	   }
	   
	   private class cRadio extends JRadioButtonMenuItem{
		private static final long serialVersionUID = 1L;
		String name;
		
		Consumer<Boolean> action;
		
		
		   public cRadio( String s){
			   name = s;
			   this.setText(name);
			   action = (y) -> {};
		   }
		   
		   public void group(cGroup bg){
			   bg.add(this);
		   }
		   
		   public void set_action(Consumer<Boolean> c){
				action = c;
			}
		   public void act(boolean b){
				action.accept(b);
			}
	   }
	   
	   private class cGroup extends ButtonGroup{
		private static final long serialVersionUID = 1L;
		String name;
		   public cGroup( String s){
			   name = s;
		   }
	   }

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		Component c = get(cmd);
		if(c.getClass().equals(cMenu.class)){
			((cMenu) c).action.accept(true);
			return;
		}
		if(c.getClass().equals(cItem.class)){
			((cItem) c).act(true);
			return;
		}
		if(c.getClass().equals(cCheck.class)){
			((cCheck) c).act(true);
			return;
		}
		if(c.getClass().equals(cRadio.class)){
			((cRadio) c).act(true);
			return;
		}

	}

	@Override
	public void itemStateChanged(ItemEvent e) {

		AbstractButton button = (AbstractButton) e.getItem();
		boolean state = button.isSelected();
	    String cmd = button.getText();
		Component c = get(cmd);
		if(c.getClass().equals(cMenu.class)){
			((cMenu) c).act(true);
			return;
		}
		if(c.getClass().equals(cItem.class)){
			((cItem) c).act(true);
			return;
		}
		if(c.getClass().equals(cCheck.class)){
			((cCheck) c).act(true);
			return;
		}
		if(c.getClass().equals(cRadio.class)){
			((cRadio) c).act(true);
			return;
		}
	}
	   

}
