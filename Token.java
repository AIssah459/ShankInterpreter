
public class Token {
	public enum tokenType {
		VARIABLES,
		IDENTIFIER,
		CONST,
		STRING,
		STRINGLIT,
		CHAR,
		CHARLIT,
		PLUS,
		MINUS,
		MUL,
		DIV,
		BOOLEAN,
		BOOLEQ,
		LESSTHAN,
		LESSEQ,
		GREATERTHAN,
		GREATEREQ,
		NOTEQ,
		INTEGER,
		INTEGERLIT,
		EQUALS,
		WHILE,
		FOR,
		IF,
		THEN,
		ELSE,
		ELSIF,
		DO,
		FROM,
		TO,
		TRUE,
		FALSE,
		COMMENT,
		LPAREN,
		RPAREN,
		ENDOFLINE,
		INDENT,
		NONE, COLON, DEFINE, DEDENT, VAR, REAL, REALLIT, COMMA, ASSIGN, LBRACKET, RBRACKET, DOT, OF, ARRAY, REPEAT, MODULO, UNTIL, SEMICOLON
	}
	private String value;
	private tokenType type;
	private int line;
	
	public Token(String value, tokenType type, int line) {
		this.value = value;
		this.type = type;
		this.line = line;
	}

	public Token(Token t) {
		this.value = t.getValue();
		this.type = t.getTokenType();
		this.line = t.getLine();
	}
	
	public tokenType getTokenType() {
		return type;
	}
	public String getValue() {
		return value;
	}
	public int getLine() {
		return this.line;
	}
	public void newLine() {
		line++;
	}
	
	//toString method
	@Override
	public String toString() {
		if(this.type == tokenType.ENDOFLINE) {
			return "" + this.type;
		} else if(this.type == tokenType.COMMENT) {
			return "" + this.type;
		}
		return this.type + ": " + this.value;
	}
}
