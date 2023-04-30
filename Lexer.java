
import java.util.ArrayList;
import java.util.HashMap;

//Lexer class
public class Lexer {
	
	/*
	 * Private member variables that track relevant states
	 */
	
	private ArrayList<Token> tokens;
	private HashMap<String, Token.tokenType> keywords;
	private HashMap<String, Token.tokenType> specialChars;
	private Token currentToken;
	private int line;
	private boolean startOfLine;
	private int indentLevel;

	private int prevIndentLevel;
	
	/*
	 * Constructor for Lexer class, initializes the token state, tokens array,
	 * keywords, and line number
	 * number
	 */
	
	public Lexer(String content) {
		
		/*
		 * Set initial state of Lexer
		 */
		
		currentToken = new Token("", Token.tokenType.NONE, 1);
		tokens = new ArrayList<Token>();
		keywords = new HashMap<>();
		specialChars = new HashMap<>();
		fillHashMap();
		line = 1;
		startOfLine = true;
		indentLevel = 0;
		prevIndentLevel = 0;
		
		/*
		 * Calls the lex function
		 */
		
		lex(content);
	}
	
	/*
	 * Sets the current State to a token with the specified params
	 */
	
	private void setState(String s, Token.tokenType t, int l) {
		currentToken = new Token(s, t, l);
	}
	
	/*
	 * Resets the current state to NONE (preserves line INTEGER)
	 */
	
	private void resetToken() {
		currentToken = new Token("", Token.tokenType.NONE, line);
	}
	
	/*
	 * Adds a new token to the tokens arraylist
	 */
	
	private void addToken(String s, Token.tokenType t, int l) {
		tokens.add(new Token(s, t, l));
	}
	
	private void fillHashMap() {
		
		/*
		 * Add all the keywords
		 */
		
		keywords.put("constants", Token.tokenType.CONST);
		keywords.put("define", Token.tokenType.DEFINE);
		keywords.put("variables", Token.tokenType.VARIABLES);
		keywords.put("var", Token.tokenType.VAR);
		keywords.put("char", Token.tokenType.CHAR);
		keywords.put("string", Token.tokenType.STRING);
		keywords.put("integer", Token.tokenType.INTEGER);
		keywords.put("boolean", Token.tokenType.BOOLEAN);
		keywords.put(":=", Token.tokenType.ASSIGN);
		keywords.put("==", Token.tokenType.BOOLEQ);
		keywords.put("while", Token.tokenType.WHILE);
		keywords.put("for", Token.tokenType.FOR);
		keywords.put("if", Token.tokenType.IF);
		keywords.put("else", Token.tokenType.ELSE);
		keywords.put("do", Token.tokenType.DO);
		keywords.put("true", Token.tokenType.TRUE);
		keywords.put("false", Token.tokenType.FALSE);
		keywords.put("array", Token.tokenType.ARRAY);
		keywords.put("of", Token.tokenType.OF);
		keywords.put("repeat", Token.tokenType.REPEAT);
		keywords.put("until", Token.tokenType.UNTIL);
		
		/*
		 * Add all the special characters
		 */
		
		specialChars.put("(", Token.tokenType.LPAREN);
		specialChars.put(")", Token.tokenType.RPAREN);
		specialChars.put("[", Token.tokenType.LBRACKET);
		specialChars.put("]", Token.tokenType.RBRACKET);
		specialChars.put("+", Token.tokenType.PLUS);
		specialChars.put("-", Token.tokenType.MINUS);
		specialChars.put("*", Token.tokenType.MUL);
		specialChars.put("/", Token.tokenType.DIV);
		specialChars.put("=", Token.tokenType.EQUALS);
		specialChars.put(":", Token.tokenType.COLON);
		specialChars.put(",", Token.tokenType.COMMA);
		specialChars.put(">", Token.tokenType.GREATERTHAN);
		specialChars.put("<", Token.tokenType.LESSTHAN);
		specialChars.put(".", Token.tokenType.DOT);
		specialChars.put("%", Token.tokenType.MODULO);
		
		
	}
	
	private void addKwToken(String kw) {
		if(!keywords.containsKey(kw)) {
			System.err.println("Keyword doesn't exist: " + kw);
			System.exit(1);
		}
		addToken("", keywords.get(kw), line);
	}
	
	private void addSpCharToken(String c) {
		if(!specialChars.containsKey(c)) {
			System.err.println("Character" + c + " is not a special char. [Line " + line + "]");
			System.exit(1);
		}
		addToken("", specialChars.get(c), line);
	}
	
	private boolean isKeyword(Token t) {
		if(keywords.containsValue(t.getTokenType())) {
			return true;
		}
		return false;
	}
	
	private boolean isSpecialChar(Token t) {
		if(specialChars.containsValue(t.getTokenType())) {
			return true;
		}
		return false;
	}
	
	/*
	 * Helper function to generate all the tokens up to the second
	 * to last token
	 */
	
	private String generateTokens(String acc, char[] contentArr) {
		if(currentToken.getTokenType() != Token.tokenType.COMMENT) {
			resetToken();
		}

		/*
		Loop over content array
		 */
		
		for(int i = 0; i < contentArr.length; i++) {
			if(startOfLine) {
				indentLevel = 0;
			}
			if(indentLevel > prevIndentLevel) {
				for(int k = 0; k <= indentLevel-prevIndentLevel-1; k++) {
					addToken("", Token.tokenType.INDENT, line);
				}
			}
			else {
				for(int l = 0; l < prevIndentLevel - indentLevel; l++)
					addToken("", Token.tokenType.DEDENT, line);
			}
			prevIndentLevel = indentLevel;

			/*
			 * Processes tokens differently based on the token type
			 */
			
			switch(currentToken.getTokenType()) {
			
			/*
			 * Case if token is Identifier
			 * THIS CODE IS ABSOLUTE SPAGHETTI, because it needs to be flexible
			 */
			
			case IDENTIFIER:
				
				/*
				 * First, check if the current character is a letter
				 */
				startOfLine = false;
				if(Character.isLetter(contentArr[i]))
					acc += contentArr[i];
				
				/*
				 * If the current character is space, adds a token (depends on if
				 * it's a keyword or not)
				 */
				
				else if(("" + contentArr[i]).equals(" ")) {
					if(keywords.containsKey(acc)) {
						addKwToken(acc);
						resetToken();
						acc = "";
					}
					else {
						addToken(acc.replace("\n", "").replace(" ", ""), Token.tokenType.IDENTIFIER, line);
						resetToken();
						acc = "";
					}
				}
				
				/*
				 * If the current character is a parenthesis, adds the next
				 * token then resets state, before clearing the accumulator
				 * String.
				 */

				else if(specialChars.containsKey(""+contentArr[i])) {
					if(!acc.isEmpty()) {
						if(keywords.containsKey(acc)) {
							addKwToken(acc);
						}
						else {
							addToken(acc, Token.tokenType.IDENTIFIER, line);
						}
						addSpCharToken("" + contentArr[i]);
						acc = "";
						resetToken();
					}
					else {
						resetToken();
					}
				}
				
				/*
				 * If the character's a newline, runs the keyword check, adds a token,
				 * resets state, then clear the accumulator
				 */
				
				else if(contentArr[i] == '\n' || ("" + contentArr[i]).equals("\r\n")) {
					if(keywords.containsKey(acc)) {
						addKwToken(acc);
						addToken(acc, Token.tokenType.ENDOFLINE, line++);
						startOfLine = true;
						resetToken();
						acc = "";
					}
					if(acc != "") {
						addToken(acc, Token.tokenType.IDENTIFIER, line);
						addToken(acc, Token.tokenType.ENDOFLINE, line++);
						startOfLine = true;
						resetToken();
						acc = "";
					}
					else {
						resetToken();
					}
				}
				
				/*
				 * For any unexpected state, the program fails and exits on an
				 * error code.
				 */
				
				else {
					System.err.println("Illegal character " + contentArr[i] + " in IDENTIFIER: " + acc.replace("\n", "").replace(" ", "") + " (Line " + line + ")");
					System.exit(1);
				}
				break;
				
			/*
			 * Case if token is an INTEGER value
			 */
				
			case INTEGERLIT:
				
				startOfLine = false;
				
				if(Character.isDigit(contentArr[i])) {
					acc += contentArr[i];
				}
				else if(("" + contentArr[i]).equals(" ")) {
					addToken(acc.replace(" ", ""), Token.tokenType.INTEGERLIT, line);
					resetToken();
					acc = "";
				}
				
				/*
				 * Handle parentheses
				 */

				else if(specialChars.containsKey(""+contentArr[i])) {
					if(contentArr[i] == '.') {
						if(Character.isDigit(contentArr[i + 1])) {
							acc += contentArr[i];
							setState("", Token.tokenType.REALLIT, line);
						}
						else {
							System.err.println("Unexpected character after '.': [Line " + line + "]");
						}
					}
					else if(!acc.isEmpty()) {
						if(contentArr[i] == ')' || contentArr[i] == ']') {
							addToken(acc, Token.tokenType.INTEGERLIT, line);
							addSpCharToken("" + contentArr[i]);
							resetToken();
							acc = "";
						}
					}
					else {
						resetToken();
					}
				}
				
				/*
				 * Other error handling
				 */
				
				else if(contentArr[i] == '\n' || ("" + contentArr[i]).equals("\r\n")) {
					addToken(acc, Token.tokenType.INTEGERLIT, line);
					addToken(acc, Token.tokenType.ENDOFLINE, line++);
					resetToken();
					startOfLine = true;
					acc = "";
				}
				else {
					System.err.println("Illegal character " + contentArr[i] + " in INTEGER: " + acc + " (Line " + line + ")");
					System.exit(0);
				}
					
				break;

			case REALLIT:

				startOfLine = false;

				if(Character.isDigit(contentArr[i])) {
					acc += contentArr[i];
				} else if(("" + contentArr[i]).equals(" ")) {
					addToken(acc.replace(" ", ""), Token.tokenType.REALLIT, line);
					resetToken();
					acc = "";
				} else if(specialChars.containsKey(""+contentArr[i])) {
					if(contentArr[i] == ')' || contentArr[i] == ']') {
						addToken(acc, Token.tokenType.INTEGERLIT, line);
						addSpCharToken("" + contentArr[i]);
						resetToken();
						acc = "";
					}
				} else if(contentArr[i] == '\n' || ("" + contentArr[i]).equals("\r\n")) {
					addToken(acc, Token.tokenType.REALLIT, line);
					addToken(acc, Token.tokenType.ENDOFLINE, line++);
					startOfLine = true;
					resetToken();
					acc = "";
				} else {
					System.err.println("Illegal character " + contentArr[i] + " in real number: " + acc + " (Line " + line + ")");
					System.exit(0);
				}
				break;
				
			/*
			 * Case if character literal
			 */
				
			case CHARLIT:
				
				startOfLine = false;
				
				if(contentArr[i + 1] == '\'') {
					addToken("" + contentArr[i++], Token.tokenType.CHARLIT, line);
					resetToken();
					acc = "";
				}
				else {
					System.err.println("Illegal character " + contentArr[i + 1] + "in character literal.");
					System.exit(1);
				}
				break;
				
			/*
			 * Case if string literal
			 */
				
			case STRINGLIT:
				
				startOfLine = false;
				
				String val = "";
				while(contentArr[i] != '\"') {
					if((i == contentArr.length - 1) && i != '\"') {
						System.err.println("Unclosed string literal at: Line " + line);
						System.exit(1);
					}
					val += contentArr[i];
					i++;
				}
				if(contentArr[i] == '\"') {
					addToken(val, Token.tokenType.STRINGLIT, line);
					resetToken();
					acc = "";
					i++;
				}
				else {
					System.err.println("Unclosed string literal.");
					System.exit(1);
				}
				
			/*
			 * Case if token is Newline
			 */
				
			case ENDOFLINE:
				
				startOfLine = true;
				
				if(contentArr[i] =='\n' || ("" + contentArr[i]).equals("\r\n")) {
					acc += contentArr[i];
					addToken(acc, Token.tokenType.ENDOFLINE, line);
					resetToken();
					acc = "";
					line++;
				} else {
					acc += contentArr[i];
					addToken(acc, Token.tokenType.ENDOFLINE, line);
					resetToken();
					acc = "" + contentArr[i];
					line++;
				}
				break;
				
			case COMMENT:
				break;
				
			/*
			 * Case if token is not yet decided
			 */
				
			case NONE:
				
				if(Character.isLetter(contentArr[i])) {
					acc += contentArr[i];
					setState("" + contentArr[i], Token.tokenType.IDENTIFIER, line);
				}
				else if(Character.isDigit(contentArr[i])) {
					acc += contentArr[i];
					setState(("" + contentArr[i]), Token.tokenType.INTEGERLIT, line);
				}
				else if(contentArr[i] =='\n' || ("" + contentArr[i]).equals("\r\n")) {
					addToken(("" + contentArr[i]), Token.tokenType.ENDOFLINE, line);
					startOfLine = true;
					line++;
				}
				else if(contentArr[i] == '\t') {
					if(startOfLine) {
						indentLevel++;
						startOfLine = false;
					}
				}
				else if(contentArr[i] == '\'') {
					setState((""), Token.tokenType.CHARLIT, line);
				}
				else if(contentArr[i] == '\"') {
					setState((""), Token.tokenType.STRINGLIT, line);
				}
				else if(contentArr[i] == '=') {
					if(contentArr[i + 1] == '=') {
						addToken("", Token.tokenType.BOOLEQ, line);
						i += 2;
					}
					else {
						addToken("", Token.tokenType.EQUALS, line);
					}
				}
				else if(specialChars.containsKey("" + contentArr[i])) {
					if(contentArr[i] == '-') {
						if(Character.isDigit(contentArr[i + 1])) {
							acc += contentArr[i];
							setState("", Token.tokenType.INTEGERLIT, line);
						}
						else {
							addSpCharToken("" + contentArr[i]);
						}
					}
					else if(contentArr[i] == ':') {
						if(contentArr[i + 1] == '=') {
							addToken("", Token.tokenType.ASSIGN, line);
							i += 2;
						}
						else {
							addSpCharToken("" + contentArr[i]);
						}
					} else if (contentArr[i] == '<') {
						if(contentArr[i + 1] == '=') {
							addToken("", Token.tokenType.LESSEQ, line);
							i += 2;
						} else if (contentArr[i + 1] == '>') {
							addToken("", Token.tokenType.NOTEQ, line);
							i += 2;
						} else {
							addSpCharToken("" + contentArr[i]);
						}
					} else if (contentArr[i] == '>') {
						if(contentArr[i + 1] == '=') {
							addToken("", Token.tokenType.GREATEREQ, line);
							i += 2;
						} else {
							addSpCharToken("" + contentArr[i]);
						}
					} else {
						addSpCharToken("" + contentArr[i]);
					}
				}
				else {
					if(contentArr[i] == ' ') {
						if(startOfLine) {
							boolean indented = true;
							for (int j = 1; j < 3; j++) {
								if (contentArr[i+j] != ' ') {
									indented = false;
								}
							}
							if(indented == true) {
								indentLevel++;
							}
							i += 4;
						}
						resetToken();
					}
					else {
						System.err.println("Illegal character: " + contentArr[i] + " in token: " + acc + " (Line " + line + ")");
						System.exit(1);
					}
				}
				break;
				
			/*
			 * Illegal character/token
			 */
				
			default:
				if(contentArr[i] == ' ') {
					System.err.println("Character: SPACE not a valid token. (in Line " + line + ")");
				}
				System.err.println("Character: " + contentArr[i] + " not a valid token. (in Line " + line + ")");
				System.exit(1);
			}
		}
		return acc;
	}
	
	/*
	 * After generating all the tokens, handles the last token from the file
	 */
	
	private void finalToken(char[] contentArr) {
		
		/*
		 * Calls generateTokens to generate all the tokens
		 */
		
		String acc = "";
		
		acc = generateTokens(acc, contentArr);

		if(indentLevel > prevIndentLevel) {
			for(int k = 0; k <= indentLevel-prevIndentLevel-1; k++) {
				addToken("", Token.tokenType.INDENT, line);
			}
		}
		else {
			for(int l = 0; l < prevIndentLevel - indentLevel; l++)
				addToken("", Token.tokenType.DEDENT, line);
		}
		prevIndentLevel = indentLevel;
		
		/*
		 * Handles the final token
		 */
		
		switch(currentToken.getTokenType()) {
		case IDENTIFIER:
			if(keywords.containsKey(acc)) {
				addKwToken(acc);
			}
			else {
				addToken(acc, Token.tokenType.IDENTIFIER, line);
			}
			acc = "";
			break;

		case STRINGLIT:
			addToken(acc, Token.tokenType.STRINGLIT, line);
			acc = "";
			break;

		case CHARLIT:
			addToken(acc, Token.tokenType.CHARLIT, line);
			acc = "";
			break;
			
		case INTEGERLIT:
			addToken(acc, Token.tokenType.INTEGERLIT, line);
			acc = "";
			break;

		case REALLIT:
			addToken(acc, Token.tokenType.REALLIT, line);
			acc = "";
			break;
			
		case ENDOFLINE:
			addToken(acc, Token.tokenType.ENDOFLINE, line);
			acc = "";
			break;
		
		case NONE:
			break;
			
		default:
			System.err.println("Illegal character");
			System.exit(1);
		}
	}
	
	/*
	 * Lex function, main caller
	 */
	
	private void lex(String content) {
		char[] contentArr = new char[content.length()];
		
		/*
		 * Changes content string to a char array
		 */
		
		for(int i = 0; i < content.length(); i++) {
			contentArr[i] = content.charAt(i);
		}
		finalToken(contentArr);
	}
	
	/*
	 * Helper function to print all the tokens generated by lex()
	 */
	
	public void printTokens() {
		boolean startOfLine = true;
		int i = 0;
		for(i = 0; i < tokens.size(); i++) {
			if(startOfLine)
				System.out.print("[");
			if(tokens.get(i).getTokenType() == Token.tokenType.ENDOFLINE) {
				startOfLine = true;
				System.out.println(tokens.get(i) + "] Line: " + tokens.get(i).getLine() + "\n");
			}
			else {
				startOfLine = false;
				Token t = tokens.get(i);
				if(isKeyword(t)) {
					System.out.print(t.getTokenType() + ", ");
				}
				else if(isSpecialChar(t)) {
					System.out.print(t.getTokenType() + ", ");
				}
				else {
					System.out.print(tokens.get(i) + ", ");
				}
			}
		}
		if(tokens.get(--i).getTokenType() != Token.tokenType.ENDOFLINE) {
			System.out.println("] Line: " + tokens.get(i).getLine());
		}
	}

	public void getIndentLevel() {

	}

	public ArrayList<Token> tokens() {
		return this.tokens;
	}
}
