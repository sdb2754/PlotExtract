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
import javax.swing.JRadioButtonMenuItem;

public class AutoMenuBar extends JMenuBar implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;

	// Button Groups
	ArrayList<cGroup> groups = new ArrayList<cGroup>();

	// Top level menus
	ArrayList<cMenu> menus = new ArrayList<cMenu>();

	// Constructor
	AutoMenuBar() {

	}

	// Methods for adding items
	/*
	 * These methods take a name which is the display text. No two items may
	 * have the same name.
	 * 
	 * 
	 * the String "parent" is the name of the parent use "bar" to place a menu
	 * at the top level only menus can be added to the top level
	 * 
	 * the String "type" indicated which type of item to add Items can be menus,
	 * items, radio buttons, or check boxes This method is also used to add
	 * ButtonGroups
	 * 
	 * the boolean "checked" is used if the item is a check box or radio button
	 * to set initial value
	 * 
	 * Consumer<Boolean> action is a function that runs when the item is clicked
	 * and/or state changed This can be set like:
	 * 
	 * Consumer<Boolean> action = (y) -> {//some Java code}
	 * 
	 * When:
	 * 
	 * action.accept(boolean);
	 * 
	 * is called, then the code is executed
	 * 
	 */
	public void add_item(String name, String parent, String type, String group) {
		add_item(name, parent, type, group, false, (y) -> {
		});
	}

	public void add_item(String name, String parent, String type, String group, boolean checked) {
		add_item(name, parent, type, group, checked, (y) -> {
		});
	}

	public void add_item(String name, String parent, String type, Consumer<Boolean> action) {
		add_item(name, parent, type, "none", false, action);
	}

	public void add_item(String name, String parent, String type, boolean checked) {
		add_item(name, parent, type, "none", checked, (y) -> {
		});
	}

	public void add_item(String name, String parent, String type) {
		add_item(name, parent, type, "none", false, (y) -> {
		});
	}

	// The main add_item method
	public void add_item(String name, String parent, String type, String group, boolean checked,
			Consumer<Boolean> action) {

		// Get the parent Component
		Component cp = get(parent);

		if (!is_valid_parent(cp))
			return;

		switch (type) {

		case "menu":
			// if the parent is the MenuBar
			if (cp == this) {
				cMenu cm = new cMenu(name);
				// add to MenuBar
				this.add(cm);
				// Listen for click
				cm.addActionListener(this);
				// Add to top level menu list
				menus.add(cm);
				// Set the click action
				cm.set_action(action);
				return;
			}
			cMenu cm = new cMenu(name);
			// Add the item to its parent
			((cMenu) cp).add_item(cm, name, type, group);
			cm.addActionListener(this);
			cm.set_action(action);
			break;
		case "item":
			cItem ci = new cItem(name);
			((cMenu) cp).add_item(ci, name, type, group);
			ci.addActionListener(this);
			ci.set_action(action);
			break;
		case "radio":
			cRadio cr = new cRadio(name);
			((cMenu) cp).add_item(cr, name, type, group);
			cr.addActionListener(this);
			cr.addItemListener(this);
			// If the item should be added to a ButtonGroup
			if (group != "none")
				cr.group(group(group));
			// Set initial check state
			cr.setSelected(checked);
			cr.set_action(action);
			break;
		case "check":
			cCheck cc = new cCheck(name);
			((cMenu) cp).add_item(cc, name, type, group);
			cc.addActionListener(this);
			cc.addItemListener(this);
			cc.setSelected(checked);
			cc.set_action(action);
			break;
		case "group":
			// Add a new ButtonGroup
			groups.add(new cGroup(name));
			break;

		}
	}

	/*
	 * Methods for adding groups of items Less capability, but can add a lot of
	 * items at once Defines basic items with empty click actions and no initial
	 * checked state
	 */
	// Add top-level menus to the MenuBar
	public void add_top_menus(String[] names) {
		for (String n : names) {
			add_item(n, "bar", "menu");
		}
	}

	// Add groups of items to a menu
	public void add_items(String[][] def, String parent) {
		for (String[] item : def) {
			add_item(item[0], parent, item[1]);
		}
	}

	// Set the click action for an item
	public void set_action(String name, Consumer<Boolean> a) {

		// Get the Component named "name"
		Component c = get(name);

		if (c == null)
			return;
		// Must handle each type separately
		// because the Component can only be cast to one type
		if (c.getClass().equals(cMenu.class)) {
			// Calls the set_action method for the menu item
			((cMenu) c).set_action(a);
			return;
		}
		if (c.getClass().equals(cItem.class)) {
			((cItem) c).set_action(a);
			return;
		}
		if (c.getClass().equals(cCheck.class)) {
			((cCheck) c).set_action(a);
			return;
		}
		if (c.getClass().equals(cRadio.class)) {
			((cRadio) c).set_action(a);
			return;
		}
	}

	// Set the state changed action for an item
	public void set_statechange(String name, Consumer<Boolean> a) {

		// Get the Component named "name"
		Component c = get(name);
		// Must handle each type separately
		// because the Component can only be cast to one type
		if (c.getClass().equals(cCheck.class)) {
			((cCheck) c).set_statechanged(a);
			return;
		}
		if (c.getClass().equals(cRadio.class)) {
			((cRadio) c).set_statechanged(a);
			return;
		}
	}

	/*
	 * Check if a given item is a valid parent, by name 
	 * To be valid, it must:
	 * Exist
	 * Be either:
	 * 			 The Menubar
	 * or		 A cMenu type
	 */
	public boolean is_valid_parent(String p) {
		return is_valid_parent(get(p));
	}

	public boolean is_valid_parent(Component p) {

		// If no match was found, return
		if (p == null)
			return false;
		// If the parent is the MenuBar
		if (p == this)
			return true;
		// If the type of parent isn't cMenu, then something went wrong
		// Either a typo in the add_item call, or a redundant item name
		// Return
		if (p.getClass().equals(cMenu.class))
			return true;
		// Not a valid parent
		return false;
	}
	
	//Get a list of all items
	public ArrayList<Component> get_all(){
		ArrayList<Component> list = new ArrayList<Component>();
		//Add all top level menus
		list.addAll(menus);
		//Recursively add all items inside top level menus
		for (cMenu cm : menus) {
			list.addAll(cm.get_all());
		}
		return list;
	}

	// Get item by name
	private Component get(String name) {

		// return the MenuBar
		if (name == "bar")
			return this;
		// Check if item is in top level menus
		for (cMenu cm : menus) {
			if (cm.name.equals(name))
				return cm;
		}
		// Allocate an empty Component
		Component cp = null;
		// Recursively search items inside top level menus
		for (cMenu cm : menus) {
			// Calls the "get" method for a menu item
			cp = cm.get(name);
			// If it found a match
			if (cp != null)
				// return the Component
				return cp;
		}
		// Nothing found. Return null
		return null;
	}

	// get ButtonGroup by name
	private cGroup group(String name) {
		// Search list of ButtonGroups
		for (cGroup cg : groups) {
			// If a match is found
			if (cg.name.equals(name)) {
				// return the match
				return cg;
			}
		}
		// Nothing found. Return null
		return null;
	}

	// Custom JMenu class
	private class cMenu extends JMenu {
		private static final long serialVersionUID = 1L;
		// The display name
		String name;
		// The click action
		Consumer<Boolean> action;
		// Lists of items that are in this menu
		ArrayList<cMenu> menus = new ArrayList<cMenu>();
		ArrayList<cItem> items = new ArrayList<cItem>();
		ArrayList<cCheck> checkboxes = new ArrayList<cCheck>();
		ArrayList<cRadio> radiobuttons = new ArrayList<cRadio>();

		// Constructor
		public cMenu(String s) {
			// Set display name
			name = s;
			this.setText(name);
			// Initialize action with an empty function
			action = (y) -> {
			};
		}
		//Get all items for this this menu is an ancestor
		public ArrayList<Component> get_all(){
			ArrayList<Component> list = new ArrayList<Component>();
			//Add all items for which this menu is a parent
			list.addAll(menus);
			list.addAll(items);
			list.addAll(checkboxes);
			list.addAll(radiobuttons);
			//Recursively search menus inside this
			for (cMenu cm : menus) {
				list.addAll(cm.get_all());
			}
			return list;
		}

		// Get member item by name
		public Component get(String name) {
			// Search menus
			for (cMenu cm : menus) {
				if (cm.name.equals(name))
					return cm;
			}
			// Search items
			for (cItem ci : items) {
				if (ci.name.equals(name))
					return ci;
			}
			// Search check boxes
			for (cCheck cc : checkboxes) {
				if (cc.name.equals(name))
					return cc;
			}
			// Search radio buttons
			for (cRadio cr : radiobuttons) {
				if (cr.name.equals(name))
					return cr;
			}
			// Recursively search menus inside this menu
			Component cp = null;
			for (cMenu cm : menus) {
				cp = cm.get(name);
				if (cp != null)
					return cp;
			}
			// Nothing found
			return null;
		}

		// Add a new item to this menu
		public void add_item(Component c, String name, String type, String group) {
			// The item has already been configured in the top level "add_item"
			// method
			// So, all we have to do is add it to the appropriate list and to
			// the Menu itself
			switch (type) {

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

			// Add to the menu
			add(c);
		}

		// Setter method for click action
		public void set_action(Consumer<Boolean> c) {
			action = c;
		}

		// Runs the "action" function, with a boolean argument "b"
		public void act(boolean b) {
			action.accept(b);
		}
		@Override
		public String getName(){
			return name;
		}
	}

	// Custom JMenuItem class
	private class cItem extends JMenuItem {
		private static final long serialVersionUID = 1L;
		// Display name
		String name;
		// Click action
		Consumer<Boolean> action;

		// Constructor
		public cItem(String s) {
			name = s;
			this.setText(name);

			action = (y) -> {
			};

		}

		// Set click action
		public void set_action(Consumer<Boolean> c) {
			action = c;
		}

		// Run click action
		public void act(boolean b) {
			action.accept(b);
		}
		
		@Override
		public String getName(){
			return name;
		}

	}

	// Custom JCheckBoxMenuItem class
	private class cCheck extends JCheckBoxMenuItem {
		private static final long serialVersionUID = 1L;
		// Display name
		String name;
		// Click action
		Consumer<Boolean> action;
		Consumer<Boolean> statechanged;

		// Constructor
		public cCheck(String s) {
			name = s;
			this.setText(name);
			action = (y) -> {
			};
			statechanged = (y) -> {
			};
		}

		// Set click action
		public void set_action(Consumer<Boolean> c) {
			action = c;
		}

		public void set_statechanged(Consumer<Boolean> c) {
			statechanged = c;
		}

		// Run click action
		public void act(boolean b) {
			action.accept(b);
		}

		public void state(boolean b) {
			statechanged.accept(b);
		}
		@Override
		public String getName(){
			return name;
		}
	}

	// Custom JRadioButtonMenuItem class
	private class cRadio extends JRadioButtonMenuItem {
		private static final long serialVersionUID = 1L;
		// Display name
		String name;
		// Click action
		Consumer<Boolean> action;
		Consumer<Boolean> statechanged;

		public cRadio(String s) {
			name = s;
			this.setText(name);
			action = (y) -> {
			};
			statechanged = (y) -> {
			};
		}

		// Add this to ButtonGroup bg
		public void group(cGroup bg) {
			bg.add(this);
		}

		// Set click action
		public void set_action(Consumer<Boolean> c) {
			action = c;
		}

		public void set_statechanged(Consumer<Boolean> c) {
			statechanged = c;
		}

		// Run click action
		public void act(boolean b) {
			action.accept(b);
		}

		public void state(boolean b) {
			statechanged.accept(b);
		}
		@Override
		public String getName(){
			return name;
		}
	}

	// Custom ButtonGroup class
	private class cGroup extends ButtonGroup {
		private static final long serialVersionUID = 1L;
		// Reference name
		String name;

		public cGroup(String s) {
			name = s;
		}
	}

	// Click event
	@Override
	public void actionPerformed(ActionEvent e) {
		// Get the display name of the clicked item
		String cmd = e.getActionCommand();
		// Get the item by name
		Component c = get(cmd);
		// Must handle each type separately because
		// you can only cast Component to one type
		if (c.getClass().equals(cMenu.class)) {
			// Run click action
			((cMenu) c).act(true);
			return;
		}
		if (c.getClass().equals(cItem.class)) {
			((cItem) c).act(true);
			return;
		}
		if (c.getClass().equals(cCheck.class)) {
			((cCheck) c).act(true);
			return;
		}
		if (c.getClass().equals(cRadio.class)) {
			((cRadio) c).act(true);
			return;
		}

	}

	// Item state (e.g. checked) is changed
	@Override
	public void itemStateChanged(ItemEvent e) {
		// Abstract button to determine checked state
		AbstractButton button = (AbstractButton) e.getItem();
		// state of item
		boolean state = button.isSelected();
		// Get the display name
		String cmd = button.getText();
		// Get item by name
		Component c = get(cmd);
		// Must handle each type separately because
		// you can only cast Component to one type
		if (c.getClass().equals(cCheck.class)) {
			((cCheck) c).state(state);
			return;
		}
		if (c.getClass().equals(cRadio.class)) {
			((cRadio) c).state(state);
			return;
		}
	}

}
