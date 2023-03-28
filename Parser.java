import java.util.ArrayList;
import java.util.HashMap;

public class Parser {
	public Lexer l;
	ArrayList<Token> tokens;
	int index;
	Token currentToken;
	
	public Parser(String input) {
		l = new Lexer(input);
		tokens = new ArrayList<>(l.tokens());
		//l.printTokens();
		currentToken = tokens.get(0);
		index = 0;
	}
	
	
	public ProgramNode parse() throws SyntaxErrorException {
		HashMap<String, FunctionNode> functions = new HashMap<>();
		ProgramNode p;
		FunctionNode f;
		while(peek() != null) {
			f = function();
			functions.put(f.getName(), f);
		}
		p = new ProgramNode(functions);
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


		/*
		Check function parameters
		 */

		parameterDeclarations(params);

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

		while(peek() != null) {
			statements.add(statements());
		}

		matchAndRemove(Token.tokenType.DEDENT);
		f = new FunctionNode(name != null ? name.getValue() : null, params, vars, statements);
		f.addExpressions(expressions);
		return f;
	}

	private StatementNode statements() throws SyntaxErrorException {
		return assignment();
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

	private Node parseFuncCall() throws SyntaxErrorException {
		Token name = matchAndRemove(Token.tokenType.IDENTIFIER);
		matchAndRemove(Token.tokenType.LPAREN);
		if(currentToken.getTokenType() != Token.tokenType.RPAREN) {

		}
		else {
			matchAndRemove(Token.tokenType.RPAREN);
		}
	}

	private IfNode parseIf() throws SyntaxErrorException {
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
		while(peek() != null && currentToken.getTokenType() != Token.tokenType.ELSIF && currentToken.getTokenType() != Token.tokenType.ELSE) {
			statements.add(statements());
		}
		addElseStatements(result);
		return result;
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
				return l;
		}
		expectEndOfLine();
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
		expectEndOfLine();
		//System.out.println("Returning expression: " + left);
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
				}
				else {
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

		Token.tokenType variable = Token.tokenType.VARIABLES;
		matchAndRemove(variable);
		Token ident = matchAndRemove(Token.tokenType.IDENTIFIER);
		if(currentToken.getTokenType() == Token.tokenType.COMMA) {
			ArrayList<Token> idents = new ArrayList<>();
			Token.tokenType t;
			idents.add(ident);
			if(currentToken == null) {
				throw new SyntaxErrorException("Unexpected null token.");
			}
			matchAndRemove(Token.tokenType.COMMA);
			while(currentToken.getTokenType() != Token.tokenType.COLON) {
				idents.add(matchAndRemove(Token.tokenType.IDENTIFIER));
				if(currentToken.getTokenType() == Token.tokenType.COMMA) {
					matchAndRemove(Token.tokenType.COMMA);
				}
			}
			matchAndRemove(Token.tokenType.COLON);
			t = currentToken.getTokenType();
			matchAndRemove(t);
			for(Token i: idents) {
				vars.add(new VariableNode(i.getValue(), t, true));
			}
			expectEndOfLine();
		}
		else {
			matchAndRemove(Token.tokenType.EQUALS);
			vars.add(new VariableNode(ident.getValue(), currentToken.getTokenType(), currentToken.getValue(), true));
			matchAndRemove(currentToken.getTokenType());
			expectEndOfLine();
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
