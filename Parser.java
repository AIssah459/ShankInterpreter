import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
	public Lexer l;
	ArrayList<Token> tokens;
	int index;
	Token currentToken;
	ProgramNode p;
	HashMap<String, FunctionNode> functions;
	
	public Parser(String input) {
		p = new ProgramNode();
		l = new Lexer(input);
		tokens = new ArrayList<>(l.tokens());
		l.printTokens();
		currentToken = tokens.get(0);
		index = 0;
	}
	
	
	public ProgramNode parse() throws SyntaxErrorException {
		functions = new HashMap<>();
		p.addFunctions(functions);
		FunctionNode f;
		while(peek() != null) {
			f = function();
			functions.put(f.getName(), f);
			p.addFunc(f);
		}
		return p;
	}

	private FunctionNode function() throws SyntaxErrorException {
		//Initialize function variables
		FunctionNode f;

		ArrayList<VariableNode> params = new ArrayList<>();
		ArrayList<VariableNode> vars = new ArrayList<>();
		ArrayList<StatementNode> statements = new ArrayList<>();
		ArrayList<Node> expressions = new ArrayList<>();

		/*
		Parse function name
		 */

		currentToken = expectEndOfLine();
		matchAndRemove(Token.tokenType.DEFINE);
		Token name = matchAndRemove(Token.tokenType.IDENTIFIER);
		matchAndRemove(Token.tokenType.LPAREN);
		expectEndOfLine();


		/*
		Check function parameters
		 */

		parameterDeclarations(params);

		matchAndRemove(Token.tokenType.INDENT);

		/*
		expect constants and variables
		 */

		if(peek().getTokenType() == Token.tokenType.CONST)
			parseConstants(vars);
		if(currentToken.getTokenType() == Token.tokenType.VARIABLES) {
			parseVariables(vars);
		}
		else if(peek() != null) {
			if(peek().getTokenType() == Token.tokenType.VARIABLES) {
				parseVariables(vars);
			}
		}

		expectEndOfLine();

		while(peek() != null && currentToken.getTokenType() != Token.tokenType.DEDENT) {
			statements.add(statements());
		}

		matchAndRemove(Token.tokenType.DEDENT);
		f = new FunctionNode(name != null ? name.getValue() : null, params, vars, statements);
		f.addExpressions(expressions);
		return f;
	}

	private StatementNode statements() throws SyntaxErrorException {
		switch (currentToken.getTokenType()) {
			case IF:
				return parseIf();
			case FOR:
				return parseFor();
			case WHILE:
				return parseWhile();
			case IDENTIFIER:
				if(functions.containsKey(currentToken.getValue())) {
					return parseFuncCall();
				} else if (p.getFunctions().containsKey(currentToken.getValue())) {
					return parseFuncCall();
				} else {
					return assignment();
				}
			case REPEAT:
				return parseRepeat();
			default:
				return assignment();
		}
	}

	private AssignmentNode assignment() throws SyntaxErrorException {
		expectEndOfLine();
		AssignmentNode n = null;
		Node arrayIndexExpr = null;
		Token name = new Token(currentToken);
		matchAndRemove(Token.tokenType.IDENTIFIER);
		if(currentToken.getTokenType() == Token.tokenType.LBRACKET) {
			matchAndRemove(Token.tokenType.LBRACKET);
			arrayIndexExpr = expression();
			matchAndRemove(Token.tokenType.RBRACKET);
		}
		matchAndRemove(Token.tokenType.ASSIGN);
		Node rVal;

		try {
			rVal = boolCmp();
		} catch(Exception e) {
			Token.tokenType t = currentToken.getTokenType();
			rVal = switch(t) {
				case IDENTIFIER -> new VariableRefNode(currentToken.getValue());
				case INTEGERLIT -> new IntNode(Integer.parseInt(currentToken.getValue()));
				case STRINGLIT -> new StringNode(currentToken.getValue());
				case CHARLIT -> new CharNode(currentToken.getValue().charAt(0));
				case TRUE -> new BooleanNode(true);
				case FALSE -> new BooleanNode(false);
				default -> expression();
			};
		}
		if(arrayIndexExpr == null) {
			n = new AssignmentNode(name.getValue(), rVal);
		}
		else {
			n = new AssignmentNode(name.getValue(), arrayIndexExpr, rVal);
		}
		expectEndOfLine();
		return n;
	}

	private ParameterNode parseParameters() throws SyntaxErrorException {
		if (currentToken.getTokenType() == Token.tokenType.IDENTIFIER) {
			// Handle plain variable or array indexing.
			Token identifier = matchAndRemove(Token.tokenType.IDENTIFIER);
			String name = identifier.getValue();

			if (currentToken.getTokenType() == Token.tokenType.LBRACKET) {
				// Parse array index expression if '[' is found.
				matchAndRemove(Token.tokenType.LBRACKET);
				Node arrIndexExpr = expression(); // Parse the index expression.
				matchAndRemove(Token.tokenType.RBRACKET);
				return new ParameterNode(new VariableRefNode(name, arrIndexExpr));
			} else {
				// Plain variable reference.
				return new ParameterNode(new VariableRefNode(name));
			}
		} else if (currentToken.getTokenType() == Token.tokenType.VAR) {
			// Handle `var` keyword for mutable variable references.
			matchAndRemove(Token.tokenType.VAR);

			Token identifier = matchAndRemove(Token.tokenType.IDENTIFIER);
			String name = identifier.getValue();

			if (currentToken.getTokenType() == Token.tokenType.LBRACKET) {
				// Handle array index for mutable variable references.
				matchAndRemove(Token.tokenType.LBRACKET);
				Node arrIndexExpr = expression();
				matchAndRemove(Token.tokenType.RBRACKET);
				return new ParameterNode(new VariableRefNode(name, arrIndexExpr));
			} else {
				// Mutable plain variable reference.
				return new ParameterNode(new VariableRefNode(name, true));
			}
		} else {
			// Parse as a boolean comparison or other expression.
			Node boolCmp = boolCmp(); // Assuming `boolCmp()` parses boolean comparisons.
			return new ParameterNode(boolCmp);
		}
	}

	private FunctionCallNode parseFuncCall() throws SyntaxErrorException {
		Token name = matchAndRemove(Token.tokenType.IDENTIFIER);
		FunctionCallNode fc = new FunctionCallNode(name.getValue());
		while(currentToken.getTokenType() != Token.tokenType.ENDOFLINE) {
			fc.addArg(parseParameters());
		}
		expectEndOfLine();
		return fc;
	}

	private IfNode parseIf() throws SyntaxErrorException {
		expectEndOfLine();
		BoolCompNode condition = null;
		ArrayList<StatementNode> statements = new ArrayList<>();
		Node boolCmpResult;
		IfNode result = null;

		matchAndRemove(Token.tokenType.IF);
		boolCmpResult = boolCmp();
		if(boolCmpResult instanceof BoolCompNode) {
			condition = (BoolCompNode)boolCmpResult;
		}
		matchAndRemove(Token.tokenType.THEN);
		matchAndRemove(Token.tokenType.INDENT);
		while(peek() != null && currentToken.getTokenType() != Token.tokenType.ELSIF && currentToken.getTokenType() != Token.tokenType.ELSE && currentToken.getTokenType() != Token.tokenType.DEDENT) {
			statements.add(statements());
		}
		result = new IfNode(condition, statements, null);
		addElseStatements(result);
		expectEndOfLine();
		return result;
	}

	private ForNode parseFor() throws SyntaxErrorException {
		ArrayList<StatementNode> s = new ArrayList<>();
		matchAndRemove(Token.tokenType.FOR);
		Node e = boolCmp();
		matchAndRemove(Token.tokenType.FROM);
		Node f = boolCmp();
		matchAndRemove(Token.tokenType.TO);
		Node t = boolCmp();
		expectEndOfLine();
		matchAndRemove(Token.tokenType.INDENT);
		while (peek() != null && currentToken.getTokenType() != Token.tokenType.ENDOFLINE && currentToken.getTokenType() != Token.tokenType.DEDENT) {
			s.add(statements());
		}

		return new ForNode(e, f, t, s);
	}

	private WhileNode parseWhile() throws SyntaxErrorException {
		ArrayList<StatementNode> s = new ArrayList<>();
		matchAndRemove(Token.tokenType.WHILE);
		BoolCompNode c = (BoolCompNode) boolCmp();
		while (peek() != null && currentToken.getTokenType() != Token.tokenType.ENDOFLINE && currentToken.getTokenType() != Token.tokenType.DEDENT) {
			s.add(statements());
		}
		return new WhileNode(c, s);
	}

	private RepeatNode parseRepeat() throws SyntaxErrorException {
		expectEndOfLine();
		matchAndRemove(Token.tokenType.REPEAT);
		matchAndRemove(Token.tokenType.UNTIL);
		ArrayList<StatementNode> s = new ArrayList<>();
		while (peek() != null && currentToken.getTokenType() != Token.tokenType.ENDOFLINE && currentToken.getTokenType() != Token.tokenType.DEDENT) {
			s.add(statements());
		}
		return new RepeatNode((BoolCompNode) boolCmp(), s);
	}

	private Node boolCmp() throws SyntaxErrorException {
		expectEndOfLine();
		BoolCompNode n;
		Token t = currentToken;
		Node l = expression();
		switch (currentToken.getTokenType()) {
			case EQUALS:
				matchAndRemove(Token.tokenType.EQUALS);
				n = new BoolCompNode(Token.tokenType.EQUALS, l, expression());
				break;
			case LESSTHAN:
				matchAndRemove(Token.tokenType.LESSTHAN);
				n = new BoolCompNode(Token.tokenType.LESSTHAN, l, expression());
				break;
			case GREATERTHAN:
				matchAndRemove(Token.tokenType.GREATERTHAN);
				n = new BoolCompNode(Token.tokenType.GREATERTHAN, l, expression());
				break;
			case LESSEQ:
				matchAndRemove(Token.tokenType.LESSEQ);
				n = new BoolCompNode(Token.tokenType.LESSEQ, l, expression());
				break;
			case GREATEREQ:
				matchAndRemove(Token.tokenType.GREATEREQ);
				n = new BoolCompNode(Token.tokenType.GREATEREQ, l, expression());
				break;
			case NOTEQ:
				matchAndRemove(Token.tokenType.NOTEQ);
				n = new BoolCompNode(Token.tokenType.NOTEQ, l, expression());
				break;
			default:
				//matchAndRemove(t.getTokenType());
				return l;
		}
		//expectEndOfLine();
		return n;
	}

	private Node expression() throws SyntaxErrorException {
		currentToken = expectEndOfLine();
		Node left = term();
		while(currentToken.getTokenType() == Token.tokenType.PLUS || currentToken.getTokenType() == Token.tokenType.MINUS) {
			Token.tokenType mathOp = currentToken.getTokenType();
			matchAndRemove(mathOp);
			Node right = term();
			left = new MathOpNode(mathOp, left, right);
		}
		//expectEndOfLine();
		return left;
	}

	private Node term() throws SyntaxErrorException {
		Node left = factor();
		while (currentToken.getTokenType() == Token.tokenType.MUL || currentToken.getTokenType() == Token.tokenType.DIV) {
			Token.tokenType mathOp = currentToken.getTokenType();
			matchAndRemove(mathOp);
			Node right = factor();
			left = new MathOpNode(mathOp, left, right);
		}
		return left;
	}

	private Node factor() throws SyntaxErrorException {
		Token token = currentToken;
		switch (token.getTokenType()) {
			case INTEGERLIT:
				matchAndRemove(Token.tokenType.INTEGERLIT);
				return new IntNode(Integer.parseInt(token.getValue()));
			case IDENTIFIER:
				matchAndRemove(Token.tokenType.IDENTIFIER);
				return new VariableRefNode(token.getValue());
			case LPAREN:
				matchAndRemove(Token.tokenType.LPAREN);
				Node node = expression();
				matchAndRemove(Token.tokenType.RPAREN);
				//System.out.println("RETURNING NODE: " + node);
				return node;
			case STRINGLIT:
				matchAndRemove(Token.tokenType.STRINGLIT);
				return new StringNode(token.getValue());
			case CHARLIT:
				matchAndRemove(Token.tokenType.CHARLIT);
				return new CharNode(token.getValue().charAt(0));
			case REALLIT:
				matchAndRemove(Token.tokenType.REALLIT);
				return new RealNode(Float.parseFloat(token.getValue()));
			default:
				throw new RuntimeException("Unexpected token: " + token.getValue() + "(" + token.getTokenType() + "): (Line " + token.getLine() + ")");
		}
	}

	/*
	Helper function to parse constants
	 */

	private void parameterDeclarations(ArrayList<VariableNode> params) throws SyntaxErrorException {
		if(currentToken.getTokenType() != Token.tokenType.RPAREN) {
			while(peek() != null) {

				/*
				Loop until get closing parentheses
				 */

				if(currentToken.getTokenType() == Token.tokenType.RPAREN) {
					matchAndRemove(Token.tokenType.RPAREN);
					break;
				}

				/*
				Get variable info
				 */

				boolean changeable = false;
				if(currentToken.getTokenType() == Token.tokenType.VAR) {
					matchAndRemove(Token.tokenType.VAR);
					changeable = true;
				}
				String paramName = matchAndRemove(Token.tokenType.IDENTIFIER).getValue();
				matchAndRemove(Token.tokenType.COLON);

				/*
				Check for variable type
				 */

				Token.tokenType paramType = currentToken.getTokenType();

				if(paramType == Token.tokenType.INTEGER) {
					matchAndRemove(Token.tokenType.INTEGER);
				} else if (paramType == Token.tokenType.STRING) {
					matchAndRemove(Token.tokenType.STRING);
				} else if (paramType == Token.tokenType.CHAR) {
					matchAndRemove(Token.tokenType.CHAR);
				} else if (paramType == Token.tokenType.REAL) {
					matchAndRemove(Token.tokenType.REAL);
				} else if (paramType == Token.tokenType.BOOLEAN) {
					matchAndRemove(Token.tokenType.BOOLEAN);
				} else {
					throw new SyntaxErrorException("Unexpected token: " + currentToken);
				}

				params.add(new VariableNode(paramName, paramType, changeable));

				/*
				consume a comma
				 */

				if(currentToken.getTokenType() != Token.tokenType.RPAREN) {
					matchAndRemove(Token.tokenType.COMMA);
				}
			}
		}
		else {
			matchAndRemove(Token.tokenType.RPAREN);
		}
		expectEndOfLine();
	}

	private void parseConstants (ArrayList<VariableNode> vars) throws SyntaxErrorException {
		matchAndRemove(Token.tokenType.INDENT);
		Token.tokenType constant = Token.tokenType.CONST;

		/*
		Loop over all constant definitions
		 */

		while(peek() != null) {
			if(currentToken.getTokenType() == Token.tokenType.CONST) {
				matchAndRemove(constant);
				Token ident = matchAndRemove(Token.tokenType.IDENTIFIER);
				matchAndRemove(Token.tokenType.EQUALS);
				vars.add(new VariableNode(ident.getValue(), currentToken.getTokenType(), currentToken.getValue(), false));
				matchAndRemove(currentToken.getTokenType());
				expectEndOfLine();
			}
			else {
				break;
			}
		}
	}

	private void parseVariables(ArrayList<VariableNode> vars) throws SyntaxErrorException {
		if(currentToken.getTokenType() == Token.tokenType.INDENT) {
			matchAndRemove(Token.tokenType.INDENT);
		}

		Token.tokenType t;
		matchAndRemove(Token.tokenType.VARIABLES);
		Token ident = matchAndRemove(Token.tokenType.IDENTIFIER);
		if (currentToken.getTokenType() == Token.tokenType.COMMA) {
			ArrayList<Token> idents = new ArrayList<>();
			idents.add(ident);
			if (currentToken == null) {
				throw new SyntaxErrorException("Unexpected null token.");
			}
			matchAndRemove(Token.tokenType.COMMA);
			while (currentToken.getTokenType() != Token.tokenType.COLON) {
				idents.add(matchAndRemove(Token.tokenType.IDENTIFIER));
				if (currentToken.getTokenType() == Token.tokenType.COMMA) {
					matchAndRemove(Token.tokenType.COMMA);
				}
			}
			matchAndRemove(Token.tokenType.COLON);
			t = currentToken.getTokenType();
			matchAndRemove(t);
			if(currentToken.getTokenType() == Token.tokenType.FROM) {
				matchAndRemove(Token.tokenType.FROM);
				Token from = matchAndRemove(currentToken.getTokenType());
				matchAndRemove(Token.tokenType.TO);
				Token to = matchAndRemove(currentToken.getTokenType());
				if(t == Token.tokenType.INTEGER) {
					for(Token i: idents) {
						vars.add(new VariableNode(i.getValue(), t, null, true, Integer.parseInt(from.getValue()), Integer.parseInt(to.getValue())));
					}
				} else if (t == Token.tokenType.REAL) {
					for(Token i: idents) {
						vars.add(new VariableNode(i.getValue(), t, null, true, Float.parseFloat(from.getValue()), Float.parseFloat(to.getValue())));
					}
				}
			}
			for (Token i : idents) {
				vars.add(new VariableNode(i.getValue(), t, true));
			}
			expectEndOfLine();
		} else {
			matchAndRemove(Token.tokenType.COLON);
			t = currentToken.getTokenType();
			matchAndRemove(t);
			if(currentToken.getTokenType() == Token.tokenType.FROM) {
				matchAndRemove(Token.tokenType.FROM);
				Token from = matchAndRemove(currentToken.getTokenType());
				matchAndRemove(Token.tokenType.TO);
				Token to = matchAndRemove(currentToken.getTokenType());
				if(t == Token.tokenType.INTEGER) {
					vars.add(new VariableNode(ident.getValue(), t, null, true, Integer.parseInt(from.getValue()), Integer.parseInt(to.getValue())));
				} else if (t == Token.tokenType.REAL) {
					vars.add(new VariableNode(ident.getValue(), t, null, true, Float.parseFloat(from.getValue()), Float.parseFloat(to.getValue())));
				}
			}
			vars.add(new VariableNode(ident.getValue(), t, true));
		}
	}

	private void addElseStatements(IfNode i) throws SyntaxErrorException {
		if(peek().getTokenType() == Token.tokenType.ELSIF) {
			matchAndRemove(Token.tokenType.DEDENT);
			matchAndRemove(Token.tokenType.ELSIF);

			BoolCompNode conditionElse = null;
			ArrayList<StatementNode> statementsElse = new ArrayList<>();
			Node boolCmpResultElse;

			boolCmpResultElse = boolCmp();
			if(boolCmpResultElse instanceof BoolCompNode) {
				conditionElse = (BoolCompNode) boolCmpResultElse;
			}
			expectEndOfLine();
			matchAndRemove(Token.tokenType.INDENT);

			while (peek() != null && currentToken.getTokenType() != Token.tokenType.ELSE) {
				statementsElse.add(statements());
			}
			i.addElseBlock(new IfNode(conditionElse, statementsElse, null));
			addElseStatements(i.getElseBlock());
		} else if (peek().getTokenType() == Token.tokenType.ELSE) {
			matchAndRemove(Token.tokenType.DEDENT);
			matchAndRemove(Token.tokenType.ELSIF);

			BoolCompNode conditionElse = null;
			ArrayList<StatementNode> statementsElse = new ArrayList<>();
			Node boolCmpResultElse;

			boolCmpResultElse = boolCmp();
			if(boolCmpResultElse instanceof BoolCompNode) {
				conditionElse = (BoolCompNode) boolCmpResultElse;
			}
			expectEndOfLine();
			matchAndRemove(Token.tokenType.INDENT);

			while (peek() != null && currentToken.getTokenType() != Token.tokenType.ELSE) {
				statementsElse.add(statements());
			}
			i.addElseBlock(new IfNode(conditionElse, statementsElse, null));
		}
	}
	
	private Token next() {
		if(tokens.get(index + 1) == null) {
			return null;
		}
		if(index < tokens.size() - 1) {
			return tokens.get(++index);
		}
		return currentToken;
	}

	private Token matchAndRemove(Token.tokenType t) throws SyntaxErrorException {
		Token returned;
		if(index == tokens.size() - 1) {
			return null;
		}
		if(currentToken.getTokenType() == t) {
			returned = new Token(currentToken);
			currentToken = next();
			return returned;
		}
		else {
			throw new SyntaxErrorException("Unexpected token: " + currentToken + " [Line: " + currentToken.getLine() + "]");
		}
	}
	
	@SuppressWarnings("unused")
	private Token peek(int n) throws IndexOutOfBoundsException {
		if(n <= 0) {
			return currentToken;
		}
		return tokens.get(tokens.indexOf(currentToken) + n);
	}
	
	private Token peek() {
		Token t = null;
		try {
			t = tokens.get(tokens.indexOf(currentToken) + 1);
		}
		catch(Exception e) {
			return null;
		}
		return t;
	}
	
	private Token expectEndOfLine() throws SyntaxErrorException, IndexOutOfBoundsException {
		if(currentToken == tokens.get(tokens.size() - 1)) {
			return currentToken;
		}
		while(currentToken.getTokenType().equals(Token.tokenType.ENDOFLINE)) {
			matchAndRemove(Token.tokenType.ENDOFLINE);
		}
		if(currentToken.getTokenType() == Token.tokenType.ENDOFLINE) {
			matchAndRemove(Token.tokenType.ENDOFLINE);
		}
		return currentToken;
	}
}
