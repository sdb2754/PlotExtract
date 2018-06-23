package events;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

/*
 * AutoMenuBar
 * 
 * This class is designed to make construction of menus easier.
 * 
 * Traditionally, constructing menus requires a lot of repetitive code.
 * This class seeks to simplify the process of adding menus and items.
 * 
 * In this class, menus of arbitrary depth can be constructed.
 * 
 * Items are referenced by their display name, a String.
 * 
 * To add an item, use the following.
 * 
 * AutoMenuBar.add_item(String name, String parent, String type, String group, boolean checked, Consumer<Boolean> action);
 * 
 * or one of the variants.
 * 
 * String name: is the display name of the item
 * 
 * String parent: is the display name of the parent JMenu
 * 
 * String type: is the type of item. It can be:
 * cMenu (extends JMenu)
 * cItem (extends JItem)
 * cRadio (extends JRadioButtonMenuItem) 
 * cChecked (extends JCheckBoxMenuItem)
 * 
 * String group: is the ButtonGroup that a cRadio item is a member of
 * 
 * boolean checked: sets the initial state of checkboxes and radiobuttons
 * 
 * Consumer<Boolean> action: is a Consumer which represents a Function with a single boolean argument.
 * This is assigned as follows:
 * Consumer<Boolean> action = (b) -> {//Java code};
 * 
 * This function will be run when the item is clicked.
 * 
 */
public class AutoMenuBar extends JMenuBar implements ActionListener, ItemListener {
	private static final long serialVersionUID = 1L;

	// Button Groups
	ArrayList<cGroup> groups = new ArrayList<cGroup>();

	// Top level menus
	ArrayList<cMenu> menus = new ArrayList<cMenu>();

	// Constructor
	AutoMenuBar() {

	}

	AutoMenuBar(String[] names) {
		addTopMenus(names);
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
	public void addItem(String name, String parent, String type, String group) {
		addItem(name, parent, type, group, false, (y) -> {
		});
	}

	public void addItem(String name, String parent, String type, String group, boolean checked) {
		addItem(name, parent, type, group, checked, (y) -> {
		});
	}

	public void addItem(String name, String parent, String type, Consumer<Boolean> action) {
		addItem(name, parent, type, "none", false, action);
	}

	public void addItem(String name, String parent, String type, boolean checked) {
		addItem(name, parent, type, "none", checked, (y) -> {
		});
	}

	public void addItem(String name, String parent, String type) {
		addItem(name, parent, type, "none", false, (y) -> {
		});
	}

	// The main add_item method
	public void addItem(String name, String parent, String type, String group, boolean checked,
			Consumer<Boolean> action) {

		// Get the parent Component
		Component cp = get(parent);

		if (!isValidParent(cp))
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
				cm.setAction(action);
				return;
			}
			cMenu cm = new cMenu(name);
			// Add the item to its parent
			((cMenu) cp).addItem(cm, name, type, group);
			cm.addActionListener(this);
			cm.setAction(action);
			break;
		case "item":
			cItem ci = new cItem(name);
			((cMenu) cp).addItem(ci, name, type, group);
			ci.addActionListener(this);
			ci.setAction(action);
			break;
		case "radio":
			cRadio cr = new cRadio(name);
			((cMenu) cp).addItem(cr, name, type, group);
			cr.addActionListener(this);
			cr.addItemListener(this);
			// If the item should be added to a ButtonGroup
			if (group != "none")
				cr.group(group(group));
			// Set initial check state
			cr.setSelected(checked);
			cr.setAction(action);
			break;
		case "check":
			cCheck cc = new cCheck(name);
			((cMenu) cp).addItem(cc, name, type, group);
			cc.addActionListener(this);
			cc.addItemListener(this);
			cc.setSelected(checked);
			cc.setAction(action);
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
	public void addTopMenus(String[] names) {
		for (String n : names) {
			addItem(n, "bar", "menu");
		}
	}

	// Add groups of items to a menu
	public void add_items(String[][] def, String parent) {
		for (String[] item : def) {
			addItem(item[0], parent, item[1]);
		}
	}


	// Set the click action for an item
	public void setAction(String name, Consumer<Boolean> a) {

		// Get the Component named "name"
		Component c = get(name);

		if (c == null)
			return;
		// Must handle each type separately
		// because the Component can only be cast to one type
		if (c instanceof cMenu) {
			// Calls the set_action method for the menu item
			((cMenu) c).setAction(a);
			return;
		}
		if (c instanceof cItem) {
			((cItem) c).setAction(a);
			return;
		}
		if (c instanceof cCheck) {
			((cCheck) c).setAction(a);
			return;
		}
		if (c instanceof cRadio) {
			((cRadio) c).setAction(a);
			return;
		}
	}

	// Set the state changed action for an item
	public void setStateChange(String name, Consumer<Boolean> a) {

		// Get the Component named "name"
		Component c = get(name);
		// Must handle each type separately
		// because the Component can only be cast to one type
		if (c instanceof cCheck) {
			((cCheck) c).setStateChanged(a);
			return;
		}
		if (c instanceof cRadio) {
			((cRadio) c).setStateChanged(a);
			return;
		}
	}

	public void setAllBackgroundColors(Color b) {
		for (Component c : getAll())
			setBackgroundColor(c.getName(), b);
	}

	public void setBackgroundColor(String name, Color b) {
		Component c = get(name);
		if (c instanceof cMenu) {
			((cMenu) c).setBackground(b);
			return;
		}
		if (c instanceof cItem) {
			((cItem) c).setBackground(b);
			return;
		}
		if (c instanceof cCheck) {
			((cCheck) c).setBackground(b);
			return;
		}
		if (c instanceof cRadio) {
			((cRadio) c).setBackground(b);
			return;
		}
	}

	public void setAllForegroundColors(Color f) {
		for (Component c : getAll())
			setForegroundColor(c.getName(), f);
	}

	public void setForegroundColor(String name, Color f) {
		Component c = get(name);
		if (c instanceof cMenu) {
			((cMenu) c).setForeground(f);
			return;
		}
		if (c instanceof cItem) {
			((cItem) c).setForeground(f);
			return;
		}
		if (c instanceof cCheck) {
			((cCheck) c).setForeground(f);
			return;
		}
		if (c instanceof cRadio) {
			((cRadio) c).setForeground(f);
			return;
		}
	}

	public void setAllIcons(Icon i) {
		for (Component c : getAll())
			setIcon(c.getName(), i);
	}

	public void setIcon(String name, Icon i) {
		Component c = get(name);
		if (c instanceof cMenu) {
			((cMenu) c).setIcon(i);
			return;
		}
		if (c instanceof cItem) {
			((cItem) c).setIcon(i);
			return;
		}
		if (c instanceof cCheck) {
			((cCheck) c).setIcon(i);
			return;
		}
		if (c instanceof cRadio) {
			((cRadio) c).setIcon(i);
			return;
		}
	}

	public void setAllFonts(Font f) {
		for (Component c : getAll())
			setFont(c.getName(), f);
	}

	public void setFont(String name, Font f) {
		Component c = get(name);
		if (c instanceof cMenu) {
			((cMenu) c).setFont(f);
			return;
		}
		if (c instanceof cItem) {
			((cItem) c).setFont(f);
			return;
		}
		if (c instanceof cCheck) {
			((cCheck) c).setFont(f);
			return;
		}
		if (c instanceof cRadio) {
			((cRadio) c).setFont(f);
			return;
		}
	}
	
	/*
	 * Check if a given item is a valid parent, by name To be valid, it must:
	 * Exist Be either: The Menubar or A cMenu type
	 */
	public boolean isValidParent(String p) {
		return isValidParent(get(p));
	}

	public boolean isValidParent(Component p) {

		// If no match was found, return
		if (p == null)
			return false;
		// If the parent is the MenuBar
		if (p == this)
			return true;
		// If the type of parent isn't cMenu, then something went wrong
		// Either a typo in the add_item call, or a redundant item name
		// Return
		if (p instanceof cMenu)
			return true;
		// Not a valid parent
		return false;
	}

	// Get a list of all items
	public ArrayList<Component> getAll() {
		ArrayList<Component> list = new ArrayList<Component>();
		// Add all top level menus
		list.addAll(menus);
		// Recursively add all items inside top level menus
		for (cMenu cm : menus) {
			list.addAll(cm.getAll());
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

	// Click event
	@Override
	public void actionPerformed(ActionEvent e) {
		// Get the display name of the clicked item
		String cmd = e.getActionCommand();
		// Get the item by name
		Component c = get(cmd);
		// Must handle each type separately because
		// you can only cast Component to one type
		if (c instanceof cMenu) {
			// Run click action
			((cMenu) c).act(true);
			return;
		}
		if (c instanceof cItem) {
			((cItem) c).act(true);
			return;
		}
		if (c instanceof cCheck) {
			((cCheck) c).act(true);
			return;
		}
		if (c instanceof cRadio) {
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
		if (c instanceof cCheck) {
			((cCheck) c).state(state);
			return;
		}
		if (c instanceof cRadio) {
			((cRadio) c).state(state);
			return;
		}
	}

	// Custom JMenu class
	private static class cMenu extends JMenu {
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

		// Get all items for this this menu is an ancestor
		public ArrayList<Component> getAll() {
			ArrayList<Component> list = new ArrayList<Component>();
			// Add all items for which this menu is a parent
			list.addAll(menus);
			list.addAll(items);
			list.addAll(checkboxes);
			list.addAll(radiobuttons);
			// Recursively search menus inside this
			for (cMenu cm : menus) {
				list.addAll(cm.getAll());
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
		public void addItem(Component c, String name, String type, String group) {
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
		public void setAction(Consumer<Boolean> c) {
			action = c;
		}

		// Runs the "action" function, with a boolean argument "b"
		public void act(boolean b) {
			action.accept(b);
		}

		@Override
		public String getName() {
			return name;
		}
	}

	// Custom JMenuItem class
	private static class cItem extends JMenuItem {
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
		public void setAction(Consumer<Boolean> c) {
			action = c;
		}

		// Run click action
		public void act(boolean b) {
			action.accept(b);
		}

		@Override
		public String getName() {
			return name;
		}

	}

	// Custom JCheckBoxMenuItem class
	private static class cCheck extends JCheckBoxMenuItem {
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
		public void setAction(Consumer<Boolean> c) {
			action = c;
		}

		public void setStateChanged(Consumer<Boolean> c) {
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
		public String getName() {
			return name;
		}
	}

	// Custom JRadioButtonMenuItem class
	private static class cRadio extends JRadioButtonMenuItem {
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
		public void setAction(Consumer<Boolean> c) {
			action = c;
		}

		public void setStateChanged(Consumer<Boolean> c) {
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
		public String getName() {
			return name;
		}
	}

	// Custom ButtonGroup class
	private static class cGroup extends ButtonGroup {
		private static final long serialVersionUID = 1L;
		// Reference name
		String name;

		public cGroup(String s) {
			name = s;
		}
	}

}
