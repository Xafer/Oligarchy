package haven.oligarchy;

public enum ActionType {
	HEARTH ('h'),
	MOVE ('m'),
	RIGHTCLICK ('r'),
	FCHOOSE ('c'),
	WAIT ('w'),
	REPEAT ('$');
	
	private char typeChar;
	
	private ActionType(char typeChar){ this.typeChar = typeChar; }
	
	public char getTypeChar() { return this.typeChar; }
}
 