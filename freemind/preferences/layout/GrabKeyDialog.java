/*
 * GrabKeyDialog.java - Grabs keys from the keyboard
 * :tabSize=8:indentSize=8:noTabs=false:
 * :folding=explicit:collapseFolds=1:
 *
 * Copyright (C) 2001, 2002 Slava Pestov
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package freemind.preferences.layout;

//{{{ Imports
import java.awt.AWTEvent;
import java.awt.Dialog;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import freemind.main.FreeMindMain;
import freemind.main.Resources;

/**
 * A dialog for getting shortcut keys.
 */
public class GrabKeyDialog extends JDialog {
	private final FreeMindMain fmMain;

	private static class Buffer {

		/**
		 */
		public int getLength() {
			// TODO Auto-generated method stub
			return 0;
		}

		/**
		 */
		public void insert(int length, String string) {
			// TODO Auto-generated method stub

		}

	}

	// {{{ toString() method
	public static String toString(KeyEvent evt) {
		String id;
		switch (evt.getID()) {
		case KeyEvent.KEY_PRESSED:
			id = "KEY_PRESSED";
			break;
		case KeyEvent.KEY_RELEASED:
			id = "KEY_RELEASED";
			break;
		case KeyEvent.KEY_TYPED:
			id = "KEY_TYPED";
			break;
		default:
			id = "unknown type";
			break;
		}

		return id + ",keyCode=0x" + Integer.toString(evt.getKeyCode(), 16)
				+ ",keyChar=0x" + Integer.toString(evt.getKeyChar(), 16)
				+ ",modifiers=0x" + Integer.toString(evt.getModifiers(), 16);
	} // }}}

	// {{{ GrabKeyDialog constructor
	/**
	 * Create and show a new modal dialog.
	 * 
	 * @param parent
	 *            center dialog on this component.
	 * @param binding
	 *            the action/macro that should get a binding.
	 * @param allBindings
	 *            all other key bindings.
	 * @param debugBuffer
	 *            debug info will be dumped to this buffer (may be null)
	 * @since jEdit 4.1pre7
	 */
	// private GrabKeyDialog(Dialog parent, KeyBinding binding,
	// Vector allBindings, Buffer debugBuffer)
	// {
	// super(parent,(getText(""grab-key.title")),true);
	//
	// init(binding,allBindings,debugBuffer);
	// } //}}}

	// {{{ GrabKeyDialog constructor
	/**
	 * Create and show a new modal dialog.
	 * 
	 * @param parent
	 *            center dialog on this component.
	 * @param binding
	 *            the action/macro that should get a binding.
	 * @param allBindings
	 *            all other key bindings.
	 * @param debugBuffer
	 *            debug info will be dumped to this buffer (may be null)
	 * @since jEdit 4.1pre7
	 */
	public GrabKeyDialog(FreeMindMain fmMain, Dialog parent,
			KeyBinding binding, Vector<KeyBinding> allBindings, Buffer debugBuffer) {
		this(fmMain, parent, binding, allBindings, debugBuffer, 0);
	}

	public GrabKeyDialog(FreeMindMain fmMain, Dialog parent,
			KeyBinding binding, Vector<KeyBinding> allBindings, Buffer debugBuffer,
			int modifierMask) {
		super(parent, (/* FIXME: getText */("grab-key.title")), true);
		this.fmMain = fmMain;
		this.modifierMask = modifierMask;
		setTitle(getText("grab-key.title"));

		init(binding, allBindings, debugBuffer);
	} // }}}

	// {{{ getShortcut() method
	/**
	 * Returns the shortcut, or null if the current shortcut should be removed
	 * or the dialog either has been cancelled. Use isOK() to determine if the
	 * latter is true.
	 */
	public String getShortcut() {
		if (isOK)
			return shortcut.getText();
		else
			return null;
	} // }}}

	// {{{ isOK() method
	/**
	 * Returns true, if the dialog has not been cancelled.
	 * 
	 * @since jEdit 3.2pre9
	 */
	public boolean isOK() {
		return isOK;
	} // }}}

	// {{{ isManagingFocus() method
	/**
	 * Returns if this component can be traversed by pressing the Tab key. This
	 * returns false.
	 */
	public boolean isManagingFocus() {
		return false;
	} // }}}

	// {{{ getFocusTraversalKeysEnabled() method
	/**
	 * Makes the tab key work in Java 1.4.
	 * 
	 * @since jEdit 3.2pre4
	 */
	public boolean getFocusTraversalKeysEnabled() {
		return false;
	} // }}}

	// {{{ processKeyEvent() method
	protected void processKeyEvent(KeyEvent evt) {
		shortcut.processKeyEvent(evt);
	} // }}}

	// {{{ Private members

	// {{{ Instance variables
	private InputPane shortcut; // this is a bad hack
	private JLabel assignedTo;
	private JButton ok;
	private JButton remove;
	private JButton cancel;
	private JButton clear;
	private boolean isOK;
	private KeyBinding binding;
	KeyBinding bindingReset;
	private Vector<KeyBinding> allBindings;
	private Buffer debugBuffer;
	private int modifierMask;
	// }}}
	public final static String MODIFIER_SEPARATOR = " ";

	// {{{ init() method
	private void init(KeyBinding binding, Vector<KeyBinding> allBindings, Buffer debugBuffer) {
		this.binding = binding;
		this.allBindings = allBindings;
		this.debugBuffer = debugBuffer;

		enableEvents(AWTEvent.KEY_EVENT_MASK);

		// create a panel with a BoxLayout. Can't use Box here
		// because Box doesn't have setBorder().
		JPanel content = new JPanel(new GridLayout(0, 1, 0, 6)) {
			/**
			 * Makes the tab key work in Java 1.4.
			 * 
			 * @since jEdit 3.2pre4
			 */
			public boolean getFocusTraversalKeysEnabled() {
				return false;
			}
		};
		content.setBorder(new EmptyBorder(12, 12, 12, 12));
		setContentPane(content);

		JLabel label = new JLabel(
				debugBuffer == null ? (getText("grab-key.caption") + " " + binding.label)
						// FIXME: getText("grab-key.caption")+new String[] {
						// binding.label })
						: (getText("grab-key.keyboard-test")));

		Box input = Box.createHorizontalBox();

		shortcut = new InputPane();
		input.add(shortcut);
		input.add(Box.createHorizontalStrut(12));

		clear = new JButton((getText("grab-key.clear")));
		clear.addActionListener(new ActionHandler());
		input.add(clear);

		assignedTo = new JLabel();
		if (debugBuffer == null)
			updateAssignedTo(null);

		Box buttons = Box.createHorizontalBox();
		buttons.add(Box.createGlue());

		if (debugBuffer == null) {
			ok = new JButton(getText("common.ok"));
			ok.addActionListener(new ActionHandler());
			buttons.add(ok);
			buttons.add(Box.createHorizontalStrut(12));

			if (binding.isAssigned()) {
				// show "remove" button
				remove = new JButton((getText("grab-key.remove")));
				remove.addActionListener(new ActionHandler());
				// FIXME: REACTIVATE buttons.add(remove);
				buttons.add(Box.createHorizontalStrut(12));
			}
		}

		cancel = new JButton(getText("common.cancel"));
		cancel.addActionListener(new ActionHandler());
		buttons.add(cancel);
		buttons.add(Box.createGlue());

		// FIXME: REACTIVATE content.add(label);
		content.add(input);
		// if(debugBuffer == null)
		// FIXME: REACTIVATE content.add(assignedTo);
		content.add(buttons);

		setDefaultCloseOperation(DISPOSE_ON_CLOSE);

		pack();
		setLocationRelativeTo(getParent());
		setResizable(false);
		setVisible(true);
	} // }}}

	// {{{ getSymbolicName() method
	private String getSymbolicName(int keyCode) {
		if (keyCode == KeyEvent.VK_UNDEFINED)
			return null;
		/*
		 * else if(keyCode == KeyEvent.VK_OPEN_BRACKET) return "["; else
		 * if(keyCode == KeyEvent.VK_CLOSE_BRACKET) return "]";
		 */

		if (keyCode >= KeyEvent.VK_A && keyCode <= KeyEvent.VK_Z) {
			return String.valueOf(Character.toLowerCase((char) keyCode));
		}

		try {
			Field[] fields = KeyEvent.class.getFields();
			for (int i = 0; i < fields.length; i++) {
				Field field = fields[i];
				String name = field.getName();
				if (name.startsWith("VK_") && field.getInt(null) == keyCode) {
					return name.substring(3);
				}
			}
		} catch (Exception e) {
			// Log.log(Log.ERROR,this,e);
		}

		return null;
	} // }}}

	// {{{ updateAssignedTo() method
	private void updateAssignedTo(String shortcut) {
		String text = (getText("grab-key.assigned-to.none"));
		KeyBinding kb = getKeyBinding(shortcut);

		if (kb != null)
			if (kb.isPrefix)
				text = getText("grab-key.assigned-to.prefix") + " " + shortcut;
			// FIXME: getText("grab-key.assigned-to.prefix")+
			// new String[] { shortcut };
			else
				text = kb.label;

		if (ok != null)
			ok.setEnabled(kb == null || !kb.isPrefix);

		assignedTo.setText((getText("grab-key.assigned-to") + " " + text));
		// FIXME: assignedTo.setText(
		// (getText("grab-key.assigned-to")+
		// new String[] { text }));
	} // }}}

	// {{{ getKeyBinding() method
	private KeyBinding getKeyBinding(String shortcut) {
		if (shortcut == null || shortcut.length() == 0)
			return null;

		String spacedShortcut = shortcut + " ";
		Enumeration<KeyBinding> e = allBindings.elements();

		while (e.hasMoreElements()) {
			KeyBinding kb = (KeyBinding) e.nextElement();

			if (!kb.isAssigned())
				continue;

			String spacedKbShortcut = kb.shortcut + " ";

			// eg, trying to bind C+n C+p if C+n already bound
			if (spacedShortcut.startsWith(spacedKbShortcut))
				return kb;

			// eg, trying to bind C+e if C+e is a prefix
			if (spacedKbShortcut.startsWith(spacedShortcut)) {
				// create a temporary (synthetic) prefix
				// KeyBinding, that won't be saved
				return new KeyBinding(kb.name, kb.label, shortcut, true);
			}
		}

		return null;
	} // }}}

	// }}}

	// {{{ KeyBinding class
	/**
	 * A jEdit action or macro with its two possible shortcuts.
	 * 
	 * @since jEdit 3.2pre8
	 */
	public static class KeyBinding {
		public KeyBinding(String name, String label, String shortcut,
				boolean isPrefix) {
			this.name = name;
			this.label = label;
			this.shortcut = shortcut;
			this.isPrefix = isPrefix;
		}

		public String name;
		public String label;
		public String shortcut;
		public boolean isPrefix;

		public boolean isAssigned() {
			return shortcut != null && shortcut.length() > 0;
		}
	} // }}}

	// {{{ InputPane class
	class InputPane extends JTextField {
		// {{{ getFocusTraversalKeysEnabled() method
		/**
		 * Makes the tab key work in Java 1.4.
		 * 
		 * @since jEdit 3.2pre4
		 */
		public boolean getFocusTraversalKeysEnabled() {
			return false;
		} // }}}

		// {{{ processKeyEvent() method
		protected void processKeyEvent(KeyEvent _evt) {
			if ((getModifierMask() & _evt.getModifiers()) != 0) {
				KeyEvent evt = new KeyEvent(_evt.getComponent(), _evt.getID(),
						_evt.getWhen(), ~getModifierMask()
								& _evt.getModifiers(), _evt.getKeyCode(),
						_evt.getKeyChar(), _evt.getKeyLocation());
				processKeyEvent(evt);
				if (evt.isConsumed()) {
					_evt.consume();
				}
				return;
			}
			KeyEvent evt = KeyEventWorkaround.processKeyEvent(_evt);
			if (debugBuffer != null) {
				debugBuffer.insert(debugBuffer.getLength(), "Event "
						+ GrabKeyDialog.toString(_evt)
						+ (evt == null ? " filtered\n" : " passed\n"));
			}

			if (evt == null)
				return;

			evt.consume();

			KeyEventTranslator.Key key = KeyEventTranslator
					.translateKeyEvent(evt);
			if (key == null)
				return;

			if (debugBuffer != null) {
				debugBuffer.insert(debugBuffer.getLength(),
						"==> Translated to " + key + "\n");
			}

			StringBuffer keyString = new StringBuffer(/* getText() */);

			// if(getDocument().getLength() != 0)
			// keyString.append(' ');

			if (key.modifiers != null)
				keyString.append(key.modifiers).append(' '); // TODO: plus ??
																// .append('+');

			if (key.input == ' ')
				keyString.append("SPACE");
			else if (key.input != '\0')
				keyString.append(key.input);
			else {
				String symbolicName = getSymbolicName(key.key);

				if (symbolicName == null)
					return;

				keyString.append(symbolicName);
			}

			setText(keyString.toString());
			if (debugBuffer == null)
				updateAssignedTo(keyString.toString());
		} // }}}
	} // }}}

	// {{{ ActionHandler class
	class ActionHandler implements ActionListener {
		// {{{ actionPerformed() method
		public void actionPerformed(ActionEvent evt) {
			if (evt.getSource() == ok) {
				if (canClose())
					dispose();
			} else if (evt.getSource() == remove) {
				shortcut.setText(null);
				isOK = true;
				dispose();
			} else if (evt.getSource() == cancel)
				dispose();
			else if (evt.getSource() == clear) {
				shortcut.setText(null);
				if (debugBuffer == null)
					updateAssignedTo(null);
				shortcut.requestFocus();
			}
		} // }}}

		// {{{ canClose() method
		private boolean canClose() {
			String shortcutString = shortcut.getText();
			if (shortcutString.length() == 0 && binding.isAssigned()) {
				// ask whether to remove the old shortcut
				int answer = JOptionPane
						.showConfirmDialog(GrabKeyDialog.this,
								getText("grab-key.remove-ask"), null,
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE);
				if (answer == JOptionPane.YES_OPTION) {
					shortcut.setText(null);
					isOK = true;
				} else
					return false;
			}

			// check whether this shortcut already exists
			bindingReset = getKeyBinding(shortcutString);
			
			if (bindingReset == null || bindingReset == binding) {
				isOK = true;
				return true;
			}

			// check whether the other shortcut is the alt. shortcut
			if (bindingReset.name == binding.name) {
				// we don't need two identical shortcuts
				JOptionPane.showMessageDialog(GrabKeyDialog.this,
						getText("grab-key.duplicate-alt-shortcut"));
				return false;
			}

			// check whether shortcut is a prefix to others
			if (bindingReset.isPrefix) {
				// can't override prefix shortcuts
				JOptionPane.showMessageDialog(GrabKeyDialog.this,
						getText("grab-key.prefix-shortcut"));
				return false;
			}

			// ask whether to override that other shortcut
			int answer = JOptionPane.showConfirmDialog(GrabKeyDialog.this,
					Resources.getInstance().format(
							"GrabKeyDialog.grab-key.duplicate-shortcut",
							new Object[] { bindingReset.name })
					, null,
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
			if (answer == JOptionPane.YES_OPTION) {
				if (bindingReset.shortcut != null
						&& shortcutString.startsWith(bindingReset.shortcut)) {
					bindingReset.shortcut = null;

				}
				isOK = true;
				return true;
			} else
				return false;
		} // }}}

	} // }}}

	/**
	 */
	private String getText(String resourceString) {
		return fmMain.getResourceString("GrabKeyDialog." + resourceString);
	}

	private int getModifierMask() {
		return modifierMask;
	}
}
