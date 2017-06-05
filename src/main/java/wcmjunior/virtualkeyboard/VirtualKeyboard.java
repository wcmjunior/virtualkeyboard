package wcmjunior.virtualkeyboard;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

/**
 * A simple virtual keyboard in the Brazilian ABNT2 layout.
 *
 * In order to use this class you must:
 *
 * 1. Create a new instance providing the size of the virtual keyboard; <br>
 * 2. Provide a text component that will be used to store the the keys typed
 * (this is performed with a separate call to setCurrentTextComponent; <br>
 * 3. Call the show method in order to show the virtual keyboard in a given
 * JFrame.
 *
 * @author Wilson de Carvalho
 */
public class VirtualKeyboard implements FocusListener {

    /**
     * Private class for storing key specification.
     */
    private class Key {

        public final int keyCode;
        public final String value;
        public final String shiftValue;

        public Key(int keyCode, String value, String shiftValue) {
            this.keyCode = keyCode;
            this.value = value;
            this.shiftValue = shiftValue;
        }

        public Key(int keyCode, String value) {
            this(keyCode, value, value);
        }

        public boolean hasShiftValue() {
            return !this.value.equals(this.shiftValue);
        }

        public boolean isLetter() {
            return value.length() == 1
                    && Character.isLetter(value.toCharArray()[0]);
        }
    }

    // Special keys definition
    private final Key TAB_KEY = new Key(KeyEvent.VK_TAB, "Tab");
    private final Key CAPS_LOCK_KEY = new Key(KeyEvent.VK_CAPS_LOCK, "Caps Lock");
    private final Key SHIFT_KEY = new Key(KeyEvent.VK_SHIFT, "Shift");
    private final Key ACUTE_KEY = new Key(KeyEvent.VK_DEAD_ACUTE, "´", "`");
    private final Key GRAVE_KEY = new Key(KeyEvent.VK_DEAD_GRAVE, "`");
    private final Key TILDE_CIRCUMFLEX_KEY = new Key(KeyEvent.VK_DEAD_TILDE, "~", "^");
    private final Key CIRCUMFLEX_KEY = new Key(KeyEvent.VK_DEAD_TILDE, "^");

    // First key row
    private Key[] row1 = new Key[]{
        new Key(KeyEvent.VK_QUOTE, "'", "\""),
        new Key(KeyEvent.VK_1, "1"), new Key(KeyEvent.VK_2, "2"),
        new Key(KeyEvent.VK_3, "3"), new Key(KeyEvent.VK_4, "4"),
        new Key(KeyEvent.VK_5, "5"), new Key(KeyEvent.VK_6, "6"),
        new Key(KeyEvent.VK_7, "7"), new Key(KeyEvent.VK_8, "8"),
        new Key(KeyEvent.VK_9, "9"), new Key(KeyEvent.VK_0, "0"),
        new Key(KeyEvent.VK_MINUS, "-", "_"),
        new Key(KeyEvent.VK_BACK_SPACE, "<<<")
    };

    // Second key row
    private Key[] row2 = new Key[]{
        TAB_KEY,
        new Key(KeyEvent.VK_Q, "q"), new Key(KeyEvent.VK_W, "w"),
        new Key(KeyEvent.VK_E, "e"), new Key(KeyEvent.VK_R, "r"),
        new Key(KeyEvent.VK_T, "t"), new Key(KeyEvent.VK_Y, "y"),
        new Key(KeyEvent.VK_U, "u"), new Key(KeyEvent.VK_I, "i"),
        new Key(KeyEvent.VK_O, "o"), new Key(KeyEvent.VK_P, "p"),
        ACUTE_KEY,
        new Key(KeyEvent.VK_BRACELEFT, "[", "{")
    };

    // Third key row
    private Key[] row3 = new Key[]{
        CAPS_LOCK_KEY,
        new Key(KeyEvent.VK_A, "a"), new Key(KeyEvent.VK_S, "s"),
        new Key(KeyEvent.VK_D, "d"), new Key(KeyEvent.VK_F, "f"),
        new Key(KeyEvent.VK_G, "g"), new Key(KeyEvent.VK_H, "h"),
        new Key(KeyEvent.VK_J, "j"), new Key(KeyEvent.VK_K, "k"),
        new Key(KeyEvent.VK_L, "l"), new Key(KeyEvent.VK_DEAD_CEDILLA, "ç"),
        TILDE_CIRCUMFLEX_KEY,
        new Key(KeyEvent.VK_BRACERIGHT, "]", "}")
    };

    // Fourth key row
    private Key[] row4 = new Key[]{
        SHIFT_KEY,
        new Key(KeyEvent.VK_BACK_SLASH, "\\", "|"),
        new Key(KeyEvent.VK_Z, "z"), new Key(KeyEvent.VK_X, "x"),
        new Key(KeyEvent.VK_C, "c"), new Key(KeyEvent.VK_V, "v"),
        new Key(KeyEvent.VK_B, "b"), new Key(KeyEvent.VK_N, "n"),
        new Key(KeyEvent.VK_M, "m"), new Key(KeyEvent.VK_COMMA, ",", "<"),
        new Key(KeyEvent.VK_PERIOD, ".", ">"),
        new Key(KeyEvent.VK_SEMICOLON, ";", ":"),
        new Key(KeyEvent.VK_SLASH, "/", "?")
    };

    // Fifth key row (spacebar only)
    private Key[] row5 = new Key[]{
        new Key(KeyEvent.VK_SPACE, " ")
    };

    private final Map<Key, JButton> buttons;
    private Component currentComponent;
    private JTextComponent lastFocusedTextComponent;
    private JFrame frame;
    private boolean isCapsLockPressed = false;
    private boolean isShiftPressed = false;
    private Color defaultColor;
    private Key accentuationBuffer;

    public VirtualKeyboard() {
        this.buttons = new HashMap<>();
    }

    /**
     * Initializes the virtual keyboard and shows in the informed JFrame.
     *
     * @param frame JFrame that will be used to show the virtual keyboard.
     * @param keyboardPanel The panel where this keyboard will be held.
     */
    public void show(JFrame frame, JPanel keyboardPanel) {
        this.frame = frame;
        currentComponent = frame.getFocusOwner();
        if (currentComponent == null) {
            currentComponent = frame.getFocusTraversalPolicy().getFirstComponent(frame);
        }

        keyboardPanel.setLayout(new GridLayout(5, 1));

        keyboardPanel.add(initRow(row1, keyboardPanel.getSize()));
        keyboardPanel.add(initRow(row2, keyboardPanel.getSize()));
        keyboardPanel.add(initRow(row3, keyboardPanel.getSize()));
        keyboardPanel.add(initRow(row4, keyboardPanel.getSize()));
        keyboardPanel.add(initRow(row5, keyboardPanel.getSize()));

        frame.pack();
    }

    private JPanel initRow(Key[] keys, Dimension dimensions) {
        JPanel p = new JPanel(new GridLayout(1, keys.length));
        int buttonWidth = dimensions.width / keys.length;
        int buttonHeight = dimensions.height / 5; // number of rows
        for (int i = 0; i < keys.length; ++i) {
            Key key = keys[i];
            JButton button;
            if (buttons.containsKey(key)) {
                button = buttons.get(key);
            } else {
                button = new JButton(key.value);
                button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));
                button.addFocusListener(this);
                buttons.put(key, button);
                button.addActionListener(e -> actionListener(key));
            }
            p.add(button);
        }
        return p;
    }

    private void actionListener(Key key) {
        if (currentComponent == null || !(currentComponent instanceof JComponent)) {
            return;
        }
        ((JComponent) currentComponent).requestFocus();
        JTextComponent currentTextComponent = getCurrentTextComponent();
        switch (key.keyCode) {
            case KeyEvent.VK_CAPS_LOCK:
                capsLockPressed();
                break;
            case KeyEvent.VK_SHIFT:
                shiftPressed();
                break;
            case KeyEvent.VK_BACK_SPACE:
                if (currentTextComponent == null) {
                    return;
                }
                backspacePressed(currentTextComponent);
                break;
            case KeyEvent.VK_TAB:
                tabPressed();
                break;
            default:
                if (currentTextComponent == null) {
                    return;
                }
                otherKeyPressed(key, currentTextComponent);
                break;
        }
    }

    private void capsLockPressed() {
        isCapsLockPressed = !isCapsLockPressed;
        buttons.forEach((k, b) -> {
            if (k.isLetter() && k.hasShiftValue()) {
                if (isCapsLockPressed) {
                    b.setText(k.shiftValue);
                } else {
                    b.setText(k.value);
                }
            }
        });
        if (isCapsLockPressed) {
            if (defaultColor == null) {
                defaultColor = buttons.get(SHIFT_KEY).getBackground();
            }
            buttons.get(CAPS_LOCK_KEY).setBackground(Color.orange);
        } else {
            buttons.get(CAPS_LOCK_KEY).setBackground(defaultColor);
        }
    }

    private void shiftPressed() {
        isShiftPressed = !isShiftPressed;
        buttons.forEach((k, b) -> {
            if (k.hasShiftValue()) {
                if (isShiftPressed) {
                    b.setText(k.shiftValue);
                } else {
                    b.setText(k.value);
                }
            }
        });
        if (isShiftPressed) {
            if (defaultColor == null) {
                defaultColor = buttons.get(SHIFT_KEY).getBackground();
            }
            buttons.get(SHIFT_KEY).setBackground(Color.orange);
        } else {
            buttons.get(SHIFT_KEY).setBackground(defaultColor);
        }
    }

    private void backspacePressed(JTextComponent component) {
        if (currentComponent instanceof JTextComponent) {
            int caretPosition = component.getCaretPosition();
            if (!component.getText().isEmpty() && caretPosition > 0) {
                try {
                    component.setText(component.getText(0, caretPosition - 1)
                            + component.getText(caretPosition,
                                    component.getText().length() - caretPosition));
                } catch (BadLocationException ex) {
                }
                component.setCaretPosition(caretPosition - 1);
            }
        }
    }

    private void tabPressed() {
        if (currentComponent != null && currentComponent instanceof JComponent) {
            Component nextComponent = ((JComponent) currentComponent).getNextFocusableComponent();
            if (nextComponent != null) {
                nextComponent.requestFocus();
                this.currentComponent = nextComponent;
            }
        }
    }

    private void otherKeyPressed(Key key, JTextComponent currentTextComponent) {
        if (key.isLetter()) {
            String keyString;
            if (accentuationBuffer == null) {
                keyString = key.value;
            } else {
                switch (key.keyCode) {
                    case KeyEvent.VK_A:
                        keyString = accentuationBuffer
                                == ACUTE_KEY ? "á"
                                        : accentuationBuffer == GRAVE_KEY ? "à"
                                                : accentuationBuffer == TILDE_CIRCUMFLEX_KEY ? "ã"
                                                        : accentuationBuffer == CIRCUMFLEX_KEY ? "â" : key.value;
                        break;
                    case KeyEvent.VK_E:
                        keyString = accentuationBuffer
                                == ACUTE_KEY ? "é"
                                        : accentuationBuffer == GRAVE_KEY ? "è"
                                                : accentuationBuffer == TILDE_CIRCUMFLEX_KEY ? "~e"
                                                        : accentuationBuffer == CIRCUMFLEX_KEY ? "ê" : key.value;
                        break;
                    case KeyEvent.VK_I:
                        keyString = accentuationBuffer
                                == ACUTE_KEY ? "í"
                                        : accentuationBuffer == GRAVE_KEY ? "ì"
                                                : accentuationBuffer == TILDE_CIRCUMFLEX_KEY ? "~i"
                                                        : accentuationBuffer == CIRCUMFLEX_KEY ? "î" : key.value;
                        break;
                    case KeyEvent.VK_O:
                        keyString = accentuationBuffer
                                == ACUTE_KEY ? "ó"
                                        : accentuationBuffer == GRAVE_KEY ? "ò"
                                                : accentuationBuffer == TILDE_CIRCUMFLEX_KEY ? "õ"
                                                        : accentuationBuffer == CIRCUMFLEX_KEY ? "ô" : key.value;
                        break;
                    case KeyEvent.VK_U:
                        keyString = accentuationBuffer
                                == ACUTE_KEY ? "ú"
                                        : accentuationBuffer == GRAVE_KEY ? "ù"
                                                : accentuationBuffer == TILDE_CIRCUMFLEX_KEY ? "~u"
                                                        : accentuationBuffer == CIRCUMFLEX_KEY ? "û" : key.value;
                    default:
                        keyString = key.value;
                        break;
                }
                accentuationBuffer = null;
            }
            if (isCapsLockPressed) {
                keyString = keyString.toUpperCase();
                if (isShiftPressed) {
                    shiftPressed();
                }
            } else if (isShiftPressed) {
                keyString = keyString.toUpperCase();
                shiftPressed();
            }
            addText(currentTextComponent, keyString);
        } else if (key == ACUTE_KEY || key == TILDE_CIRCUMFLEX_KEY) {
            if (key == ACUTE_KEY) {
                if (!isShiftPressed) {
                    accentuationBuffer = key;
                } else {
                    accentuationBuffer = GRAVE_KEY;
                }
            } else if (key == TILDE_CIRCUMFLEX_KEY) {
                if (!isShiftPressed) {
                    accentuationBuffer = key;
                } else {
                    accentuationBuffer = CIRCUMFLEX_KEY;
                }
            }
            if (isShiftPressed) {
                shiftPressed();
            }
        } else {
            String keyString;
            if (isCapsLockPressed) {
                keyString = key.value.toUpperCase();
                if (isShiftPressed) {
                    shiftPressed();
                }
            } else if (isShiftPressed) {
                keyString = key.shiftValue;
                shiftPressed();
            } else {
                keyString = key.value;
            }
            addText(currentTextComponent, keyString);
        }
    }

    private JTextComponent getCurrentTextComponent() {
        if (currentComponent != null && currentComponent instanceof JTextComponent) {
            return (JTextComponent) currentComponent;
        } else {
            return null;
        }
    }

    /**
     * Adds text considering the caret position.
     *
     * @param component Text component.
     * @param text Text that will be added.
     */
    private void addText(JTextComponent component, String text) {
        int caretPosition = component.getCaretPosition();
        try {
            component.setText(component.getText(0, caretPosition)
                    + text + component.getText(caretPosition,
                            component.getText().length() - caretPosition));
            component.setCaretPosition(caretPosition + 1);
        } catch (BadLocationException ex) {
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        Component previousComponent = e.getOppositeComponent();
        if (previousComponent != null && !(previousComponent instanceof JButton
                && buttons.values().contains((JButton) previousComponent))) {
            this.currentComponent = previousComponent;
        }
    }

    @Override
    public void focusLost(FocusEvent e) {
    }
}
