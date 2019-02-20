package fr.mercury.nucleus.input;

/**
 * <code>Input</code> is an interface for providing access to various static informations related to inputs in the <code>Mercury-Engine</code>.
 * <li>Buttons: mouse button integer value registry.
 * <li>Keys: keyboard keys integer value registry.
 * <li>Modifiers: modifier keys bit mask, based on <code>GLFW</code>.</li>
 * 
 * @author GnosticOccultist
 */
public interface Input {
	
	public static interface Buttons {
		/**
		 * The undefined mouse button.
		 */
		int BUTTON_UNDEFINED = -1;
		/**
		 * The left mouse button.
		 */
		int BUTTON_LEFT = 0;
		/**
		 * The right mouse button.
		 */
		int BUTTON_RIGHT = 1;
		/**
		 * The middle or scroll mouse button.
		 */
		int BUTTON_MIDDLE = 2;
	}
	
	public static interface Keys {
		/**
		 * The undefined key value.
		 */
		int KEY_UNKNOWN = -1;
		/**
		 * The escape key.
		 */
		int KEY_ESCAPE = 0;
		/**
		 * The 1 key. 
		 */
		int KEY_1 = 1;
		/**
		 * The 2 key. 
		 */
		int KEY_2 = 2;
		/**
		 * The 3 key.
		 */
		int KEY_3 = 3;
		/**
		 * The 4 key.
		 */
		int KEY_4 = 4;
		/**
		 * The 5 key.
		 */
		int KEY_5 = 5;
		/**
		 * The 6 key.
		 */
		int KEY_6 = 6;
		/**
		 * The 7 key.
		 */
		int KEY_7 = 7;
		/**
		 * The 8 key.
		 */
		int KEY_8 = 8;
		/**
		 * The 9 key.
		 */
		int KEY_9 = 9;
		/**
		 * The 0 key.
		 */
		int KEY_0 = 10;
		/**
		 * The minus '-' key.
		 */
		int KEY_MINUS = 11;
		/**
		 * The equal '=' key.
		 */
		int KEY_EQUAL = 12; 
		/**
		 * The backspace key.
		 */
		int KEY_BACKSPACE = 13;
		/**
		 * The tab key.
		 */
		int KEY_TAB = 14;
		/**
		 * The Q key.
		 */
		int KEY_Q = 15;
		/**
		 * The W key.
		 */
		int KEY_W = 16;
		/**
		 * The W key.
		 */
		int KEY_E = 17;
		/**
		 * The R key.
		 */
		int KEY_R = 18;
		/**
		 * The R key.
		 */
		int KEY_T = 19;
		/**
		 * The Y key.
		 */
		int KEY_Y = 20;
		/**
		 * The U key.
		 */
		int KEY_U = 21;
		/**
		 * The I key.
		 */
		int KEY_I = 22;
		/**
		 * The O key.
		 */
		int KEY_O = 23;
		/**
		 * The O key.
		 */
		int KEY_P = 24;
		/**
		 * The left bracket key.
		 */
		int KEY_LEFT_BRACKET = 25;
		/**
		 * The right bracket key.
		 */
		int KEY_RIGHT_BRACKET = 26;
		/**
		 * The enter key.
		 */
		int KEY_ENTER = 27;
		/**
		 * The left control key.
		 */
		int KEY_LEFT_CONTROL = 28;
		/**
		 * The A key.
		 */
		int KEY_A = 29;
		/**
		 * The S key.
		 */
		int KEY_S = 30;
		/**
		 * The D key.
		 */
		int KEY_D = 31;
		/**
		 * The F key.
		 */
		int KEY_F = 32;
		/**
		 * The G key.
		 */
		int KEY_G = 33;
		/**
		 * The D key.
		 */
		int KEY_H = 34;
		/**
		 * The J key.
		 */
		int KEY_J = 35;
		/**
		 * The K key.
		 */
		int KEY_K = 36;
		/**
		 * The L key.
		 */
		int KEY_L = 37;
		/**
		 * The semicolon ';' key.
		 */
		int KEY_SEMICOLON = 38;
		/**
		 * The apostrophe ''' key.
		 */
		int KEY_APOSTROPHE = 39;
		/**
		 * The grave accent '`' key.
		 */
		int KEY_GRAVE_ACCENT = 40;
		/**
		 * The left shift key.
		 */
		int KEY_LEFT_SHIFT = 41;
		/**
		 * The backslash '\' key.
		 */
		int KEY_BACKSLASH = 42;
		/**
		 * The Z key.
		 */
		int KEY_Z = 43;
		/**
		 * The X key.
		 */
		int KEY_X = 44;
		/**
		 * The C key.
		 */
		int KEY_C = 45;
		/**
		 * The V key.
		 */
		int KEY_V = 46;
		/**
		 * The B key.
		 */
		int KEY_B = 47;
		/**
		 * The N key.
		 */
		int KEY_N = 48;
		/**
		 * The M key.
		 */
		int KEY_M = 49;
		/**
		 * The comma ',' key.
		 */
		int KEY_COMMA = 50;
		/**
		 * The period '/' key.
		 */
		int KEY_PERIOD = 51;
		/**
		 * The period '.' key.
		 */
		int KEY_SLASH = 52;
		/**
		 * The right shift key.
		 */
		int KEY_RIGHT_SHIFT = 53;
		/**
		 * The multiply '*' key.
		 */
		int KEY_MULTIPLY = 54;
		/**
		 * The left alt key.
		 */
		int KEY_LEFT_ALT = 55;
		/**
		 * The space key.
		 */
		int KEY_SPACE = 56;
		/**
		 * The caps lock key.
		 */
		int KEY_CAPS_LOCK = 57;
		/**
		 * The F1 key.
		 */
		int KEY_F1 = 58;
		/**
		 * The F2 key.
		 */
		int KEY_F2 = 59;
		/**
		 * The F3 key.
		 */
		int KEY_F3 = 60;
		/**
		 * The F4 key.
		 */
		int KEY_F4 = 61;
		/**
		 * The F5 key.
		 */
		int KEY_F5 = 62;
		/**
		 * The F6 key.
		 */
		int KEY_F6 = 63;
		/**
		 * The F7 key.
		 */
		int KEY_F7 = 64;
		/**
		 * The F8 key.
		 */
		int KEY_F8 = 65;
		/**
		 * The F8 key.
		 */
		int KEY_F9 = 66;
		/**
		 * The F8 key.
		 */
		int KEY_F10 = 67;
		/**
		 * The number lock key.
		 */
		int KEY_NUM_LOCK = 68;
		/**
		 * The scroll lock key.
		 */
		int KEY_SCROLL_LOCK = 69;
		/**
		 * The numpad 7 key.
		 */
		int KEY_NUMPAD_7 = 70;
		/**
		 * The numpad 8 key.
		 */
		int KEY_NUMPAD_8 = 71;
		/**
		 * The numpad 9 key.
		 */
		int KEY_NUMPAD_9 = 72;
		/**
		 * The numpad subtract '-' key.
		 */
		int KEY_SUBTRACT = 73;
		/**
		 * The numpad 4 key.
		 */
		int KEY_NUMPAD_4 = 74;
		/**
		 * The numpad 5 key.
		 */
		int KEY_NUMPAD_5 = 75;
		/**
		 * The numpad 6 key.
		 */
		int KEY_NUMPAD_6 = 76;
		/**
		 * The numpad add '+' key.
		 */
		int KEY_ADD = 77;
		/**
		 * The numpad 1 key.
		 */
		int KEY_NUMPAD_1 = 78;
		/**
		 * The numpad 1 key.
		 */
		int KEY_NUMPAD_2 = 79;
		/**
		 * The numpad 1 key.
		 */
		int KEY_NUMPAD_3 = 80;
		/**
		 * The numpad 1 key.
		 */
		int KEY_NUMPAD_0 = 81;
		/**
		 * The numpad decimal key '.' key.
		 */
		int KEY_DECIMAL = 82;
		/**
		 * The F11 key.
		 */
		int KEY_F11 = 83;
		/**
		 * The F12 key.
		 */
		int KEY_F12 = 84;
		/**
		 * The F13 key.
		 */
		int KEY_F13 = 85;
		/**
		 * The F14 key.
		 */
		int KEY_F14 = 86;
		/**
		 * The F15 key.
		 */
		int KEY_F15 = 87;
		/**
		 * The numpad enter key.
		 */
		int KEY_NUMPAD_ENTER = 88;
		/**
		 * The numpad enter key.
		 */
		int KEY_RIGHT_CONTROL = 89;
		/**
		 * The numpad divide key.
		 */
		int KEY_NUMPAD_DIVIDE = 90;
		/**
		 * The numpad print screen key.
		 */
		int KEY_PRINT_SCREEN = 91;
		/**
		 * The right alt key.
		 */
		int KEY_RIGHT_ALT = 92;
		/**
		 * The pause key.
		 */
		int KEY_PAUSE = 93;
		/**
		 * The home key.
		 */
		int KEY_HOME = 94;
		/**
		 * The up key.
		 */
		int KEY_UP = 95;
		/**
		 * The page-up key.
		 */
		int KEY_PAGE_UP = 96;
		/**
		 * The left key.
		 */
		int KEY_LEFT = 97;
		/**
		 * The right key.
		 */
		int KEY_RIGHT = 98;
		/**
		 * The end key.
		 */
		int KEY_END = 99;
		/**
		 * The down key.
		 */
		int KEY_DOWN = 100;
		/**
		 * The page-down key.
		 */
		int KEY_PAGE_DOWN = 101;
		/**
		 * The insert key.
		 */
		int KEY_INSERT = 102;
		/**
		 * The delete key.
		 */
		int KEY_DELETE = 103;
		/**
		 * The left super key.
		 */
		int KEY_LEFT_SUPER = 104;
		/**
		 * The right super key.
		 */
		int KEY_RIGHT_SUPER = 105;
		/**
		 * The last key.
		 */
		int KEY_LAST = 224;
	}
	
	public static interface Modifiers {
		/**
		 * The bit to set when the shift key is down.
		 */
		int SHIFT_DOWN = 0x1;
		/**
		 * The bit to set when the CTRL key is down.
		 */
		int CONTROL_DOWN = 0x2;
		/**
		 * The bit to set when the ALT key is down.
		 */
		int ALT_DOWN = 0x4;
		
		/**
		 * Return whether the provided int value contains the specified bit mask
		 * modifier.
		 * 
		 * @param modifiers The set of modifiers.
		 * @param mask		The bit mask of the modifier to check presence of.
		 * @return			Whether the modifier is present in the set.
		 */
		public static boolean hasModifiers(int modifiers, int mask) {
			return (modifiers & mask) == mask;
		}
	}
}
