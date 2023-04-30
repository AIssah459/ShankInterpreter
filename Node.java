import java.util.ArrayList;
import java.util.HashMap;

public abstract class Node {
	public abstract String toString();
}

class ProgramNode extends Node {
	private HashMap<String, FunctionNode> functions;

	public ProgramNode() {
		functions = new HashMap<>();
		functions.put("Read", new BuiltInRead());
		functions.put("Write", new BuiltInWrite());
		functions.put("Left", new BuiltInLeft());
		functions.put("Right", new BuiltInRight());
		functions.put("Substring", new BuiltInSubstring());
		functions.put("SquareRoot", new BuiltInSqrt());
		functions.put("GetRandom", new BuiltInRandom());
		functions.put("IntegerToReal", new BuiltInIntToReal());
		functions.put("RealToInteger", new BuiltInRealToInt());
	}
	
	public ProgramNode(HashMap<String, FunctionNode> functions) {
		for (FunctionNode fn: functions.values()) {
			functions.put(fn.getName(), fn);
		}
	}

	public HashMap<String, FunctionNode> getFunctions() {
		return functions;
	}
	public void addFunctions(HashMap<String, FunctionNode> functions) {
		for (FunctionNode fn: functions.values()) {
			functions.put(fn.getName(), fn);
		}
	}
	public void addFunc(FunctionNode fn) {
		functions.put(fn.getName(), fn);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String t = "~~~~~PROGRAM~~~~~\n";
		for(FunctionNode fn: functions.values()) {
			if(fn instanceof BuiltInFunction) {

			} else {
				t += fn.toString();
			}
		}
		return t;
	}
}

class FunctionNode extends Node {
	protected String name;
	private ArrayList<VariableNode> params;
	private ArrayList<VariableNode> vars;
	private ArrayList<Node> expressions;
	private ArrayList<StatementNode> statements;

	protected boolean isVariadic() {return false;}

	public FunctionNode() {
		params = new ArrayList<>();
		vars = new ArrayList<>();
		statements = new ArrayList<>();
		expressions = null;
	}

	public FunctionNode(String n, ArrayList<VariableNode> p, ArrayList<VariableNode> v, ArrayList<StatementNode> s) {
		name = n;
		params = p;
		vars = v;
		statements = s;
		expressions = null;
	}

	public void addExpressions(ArrayList<Node> e) {
		this.expressions = e;
	}

	public String getName() {
		return this.name;
	}

	public ArrayList<VariableNode> getParams() {
		return params;
	}

	public ArrayList<VariableNode> vars() {
		return vars;
	}

	public ArrayList<StatementNode> statements() {
		return statements;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String t = ":FUNCTION " + name + "\n\t:PARAMS: (";
		for (VariableNode v: params) {
			t += v.toString();
			t += ", ";
		}
		t += ")\n\t:VARS: (";
		for(VariableNode v: vars) {
			t += v.toString();
			t += ", ";
		}
		t += ")\n";
		if(statements != null) {
			t += "\t:STATEMENTS\n";
			for (Node s: statements) {
				t += "\t\t";
				t += s.toString();
				t += "\n";
			}
		}
		t += ":END\n";
		return t;
	}
}

class StatementNode extends Node {

	public StatementNode() {

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}

class AssignmentNode extends StatementNode {
	VariableRefNode target;
	Node val;
	public AssignmentNode(String name, Node val) {
		target = new VariableRefNode(name);
		this.val = val;
	}
	public AssignmentNode(String name, Node arrIndexExpr, Node val) {
		target = new VariableRefNode(name, arrIndexExpr);
		this.val = val;
	}

	public Node getVal() {
		return val;
	}

	public VariableRefNode getTarget() {
		return target;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return target.toString() + " := " + val.toString();
	}
}

class FunctionCallNode extends StatementNode {
	private String name;
	private ArrayList<ParameterNode> params;
	public FunctionCallNode(String n) {
		name = n;
		params = new ArrayList<>();
	}
	public FunctionCallNode(String n, ArrayList<ParameterNode> p) {
		name = n;
		params = p;
	}

	public void addArg(ParameterNode p) {
		params.add(p);
	}
	public String getName() {
		return name;
	}

	public ArrayList<ParameterNode> getParams() {
		return params;
	}

	@Override
	public String toString() {

		String s = ":FUNCCALL ";
		s += name + ": ";
		for(ParameterNode p: params) {
			s += p.toString() + " ";
		}
		s += "\n";
		// TODO Auto-generated method stub
		return s;
	}
}

class IfNode extends StatementNode {

	private BoolCompNode condition;
	private ArrayList<StatementNode> statements;
	private IfNode elseNode;

	public IfNode(BoolCompNode c, ArrayList<StatementNode> s, IfNode e) throws SyntaxErrorException{
		if(c == null) {
			throw new SyntaxErrorException("If statement must have a condition");
		}
		condition = c;
		statements = s;
		elseNode = e;
	}

	public void addElseBlock(IfNode e) {
		elseNode = e;
	}
	public BoolCompNode getCondition() {
		return condition;
	}
	public ArrayList<StatementNode> statements() {
		return statements;
	}
	public IfNode getElseBlock() {
		return elseNode;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String s = "\t:IF ";
		s += condition.toString();
		for (StatementNode sn: statements) {
			s += "\n\t\t";
			s += sn.toString();
		}
		s += "\n";
		return s;
	}
}

class WhileNode extends StatementNode {

	BoolCompNode condition;
	ArrayList<StatementNode> statements;

	public WhileNode(BoolCompNode c, ArrayList<StatementNode> s) {
		condition = c;
		statements = s;
	}

	public BoolCompNode getCondition() {
		return condition;
	}
	public ArrayList<StatementNode> statements() {
		return statements;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String s = "\t:WHILE ";
		s += condition.toString();
		for (StatementNode sn: statements) {
			s += "\n\t\t";
			s += sn.toString();
		}
		s += "\n";
		return s;
	}
}

class ForNode extends StatementNode {

	Node expr;
	ArrayList<StatementNode> statements;
	Node from;
	Node to;

	public ForNode(Node e, Node f, Node t, ArrayList<StatementNode> s) {
		expr = e;
		from = f;
		to = t;
		statements = s;
	}

	public Node getFrom() {
		return from;
	}
	public Node getTo() {
		return to;
	}
	public ArrayList<StatementNode> statements() {
		return statements;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}

class RepeatNode extends StatementNode {

	BoolCompNode condition;
	ArrayList<StatementNode> statements;

	public RepeatNode(BoolCompNode c, ArrayList<StatementNode> s) {
		condition = c;
		statements = s;
	}
	public BoolCompNode getCondition() {
		return condition;
	}
	public ArrayList<StatementNode> statements() {
		return statements;
	}
	public String toString() {
		// TODO Auto-generated method stub
		return "REPEAT UNTIL " + condition.toString();
	}
}

class BoolCompNode extends Node {
	private Node lexpr;
	private Token.tokenType comparison;
	private Node rexpr;
	public BoolCompNode() {

	}

	public BoolCompNode(Token.tokenType comparison, Node l, Node r) {
		this.comparison = comparison;
		lexpr = l;
		rexpr = r;
	}

	public Token.tokenType condition() {
		return comparison;
	}
	public Node left() {
		return lexpr;
	}
	public Node right() {
		return rexpr;
	}

	@Override
	public String toString() {
		String t = lexpr.toString();
		switch(comparison) {
			case GREATERTHAN:
				t += " > ";
				break;
			case LESSTHAN:
				t += " < ";
				break;
			case GREATEREQ:
				t += " >= ";
				break;
			case LESSEQ:
				t += " <= ";
				break;
			case EQUALS:
				t += " = ";
				break;
			case NOTEQ:
				t += " <> ";
		}
		t += rexpr.toString();
		// TODO Auto-generated method stub
		return t;
	}
}

class IntNode extends Node {
	private int val;
	public IntNode(int val) {
		this.val = val;
	}
	
	/*
	 * Accessor
	 */
	
	public int getVal() {
		return val;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ""+val;
	}
}

class RealNode extends Node {
	private float val;
	public RealNode(float val) {
		this.val = val;
	}
	
	/*
	 * Accessor
	 */
	
	public float getVal() {
		return val;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ""+val;
	}
}

class CharNode extends Node {
	private char val;
	public CharNode(char val) {
		this.val = val;
	}
	
	/*
	 * Accessor
	 */

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ""+val;
	}
	
	public char getVal() {
		return val;
	}
}

class StringNode extends Node {
	private String val;
	public StringNode(String val) {
		this.val = val;
	}
	
	/*
	 * Accessor
	 */

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return val;
	}
	
	public String getVal() {
		return val;
	}
}

class BooleanNode extends Node {
	private boolean val;
	public BooleanNode(boolean val) {
		this.val = val;
	}
	
	/*
	 * Accessor
	 */
	
	public boolean getVal() {
		return val;
	}

	public Token.tokenType type() {
		if(val == true) {
			return Token.tokenType.TRUE;
		}
		return Token.tokenType.FALSE;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return ""+val;
	}
}

class MathOpNode extends Node {
	public enum MathOp {
		ADD,
		SUB,
		MUL,
		DIV,
		MOD
	}
	MathOp op;
	Node l;
	Node r;
	public MathOpNode(Token.tokenType t, Node l, Node r) throws SyntaxErrorException {
		op = switch (t) {
			case PLUS -> MathOp.ADD;
			case MINUS -> MathOp.SUB;
			case MUL -> MathOp.MUL;
			case DIV -> MathOp.DIV;
			case MODULO -> MathOp.MOD;
			default -> throw new SyntaxErrorException("Invalid Math Operation: " + t);
		};
		this.l = l;
		this.r = r;
	}

	public Token.tokenType getOp() {
		Token.tokenType t = switch (op) {
			case ADD -> Token.tokenType.PLUS;
			case SUB -> Token.tokenType.MINUS;
			case MUL -> Token.tokenType.MUL;
			case DIV -> Token.tokenType.DIV;
			case MOD -> Token.tokenType.MODULO;
		};
		return t;
	}
	public Node left() {
		return l;
	}
	public Node right() {
		return r;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "(" + l.toString() + " " + op.name() + " " + r.toString() + ")";
	}
}

class VariableNode extends Node {
	private String name;
	private enum varType {
		CHAR,
		INT,
		REAL,
		STRING,
		BOOLEAN
	}
	private varType type;
	private boolean changeable;
	private Node val;
	private boolean ranged = false;
	private boolean realRanged = false;
	private int from;
	private int to;

	private float realFrom;
	private float realTo;
	
	public VariableNode(String name, Token.tokenType t, Object val, boolean changeable) throws SyntaxErrorException {
		ranged = false;
		this.name = name;
		switch (t) {
			case CHARLIT:
				this.type = varType.CHAR;
				this.val = new CharNode((Character)val);
				break;

			case INTEGERLIT:
				this.type = varType.INT;
				this.val = new IntNode(Integer.parseInt((String)val));
				break;

			case REALLIT:
				this.type = varType.REAL;
				this.val = new RealNode(Float.parseFloat((String)val));
				break;

			case STRINGLIT:
				this.type = varType.STRING;
				this.val = new StringNode((String)val);
				break;

			case BOOLEAN:
				this.type = varType.BOOLEAN;
				this.val = new BooleanNode((Boolean)val);
				break;

			default:
				throw new SyntaxErrorException("Invalid variable type: " + t);
		}
		this.changeable = changeable;
	}
	public VariableNode(String name, Token.tokenType t, boolean changeable) throws SyntaxErrorException {
		ranged = false;
		this.name = name;
		switch (t) {
			case CHAR:
				this.type = varType.CHAR;
				break;

			case INTEGER:
				this.type = varType.INT;
				break;

			case REAL:
				this.type = varType.REAL;
				break;

			case STRING:
				this.type = varType.STRING;
				break;

			case BOOLEAN:
				this.type = varType.BOOLEAN;
				break;

			default:
				throw new SyntaxErrorException("Invalid variable type");
		}
		this.val = null;
		this.changeable = changeable;
	}

	public VariableNode(String name, Token.tokenType t, Object val, boolean changeable, int from, int to) throws SyntaxErrorException {
		ranged = true;
		this.from = from;
		this.to = to;
		this.name = name;
		switch (t) {
			case CHARLIT:
				this.type = varType.CHAR;
				this.val = new CharNode((Character)val);
				break;

			case INTEGERLIT:
				this.type = varType.INT;
				this.val = new IntNode(Integer.parseInt((String)val));
				break;

			case REALLIT:
				this.type = varType.REAL;
				this.val = new RealNode(Float.parseFloat((String)val));
				break;

			case STRINGLIT:
				this.type = varType.STRING;
				this.val = new StringNode((String)val);
				break;

			case BOOLEAN:
				this.type = varType.BOOLEAN;
				this.val = new BooleanNode((Boolean)val);
				break;

			default:
				throw new SyntaxErrorException("Invalid variable type: " + t);
		}
		this.changeable = changeable;
	}

	public VariableNode(String name, Token.tokenType t, Object val, boolean changeable, float rf, float rt) throws SyntaxErrorException {
		realRanged = true;
		realFrom = rf;
		realTo = rt;
		this.name = name;
		switch (t) {
			case CHARLIT:
				this.type = varType.CHAR;
				this.val = new CharNode((Character)val);
				break;

			case INTEGERLIT:
				this.type = varType.INT;
				this.val = new IntNode(Integer.parseInt((String)val));
				break;

			case REALLIT:
				this.type = varType.REAL;
				this.val = new RealNode(Float.parseFloat((String)val));
				break;

			case STRINGLIT:
				this.type = varType.STRING;
				this.val = new StringNode((String)val);
				break;

			case TRUE, FALSE:
				this.type = varType.BOOLEAN;
				this.val = new BooleanNode((Boolean)val);
				break;

			default:
				throw new SyntaxErrorException("Invalid variable type: " + t);
		}
		this.changeable = changeable;
	}

	public String name() {
		return name;
	}

	public Token.tokenType type() throws SyntaxErrorException {
		Token.tokenType t = switch (type) {
			case REAL -> Token.tokenType.REALLIT;
			case INT -> Token.tokenType.INTEGERLIT;
			case CHAR -> Token.tokenType.CHARLIT;
			case STRING -> Token.tokenType.STRINGLIT;
			case BOOLEAN -> ((BooleanNode) val).type();
		};
		return t;
	}

	public Node getVal() {
		return val;
	}
	public boolean isChangeable() {
		return changeable;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String t = "";
		if(changeable) {
			t += "VAR ";
		}
		t += this.name + ": " + this.type;
		if(val != null) {
			t += ": " + this.val;
		}
		if(ranged) {
			t += " FROM " + from + " TO " + to;
		}
		else if(realRanged) {
			t += " FROM " + from + " TO " + to;
		}
		return t;
	}
}

class ParameterNode {
	private Node v;
	public ParameterNode(Node v) {
		if(v instanceof VariableNode) {
			this.v = (VariableNode) v;
		}
		else {
			this.v = v;
		}
	}

	public ParameterNode(VariableRefNode v) {
		this.v = v;
	}

	public Node getVar() {
		return v;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "ARG(" + v.toString() + ")";
	}
}

class VariableRefNode extends Node{
	private String name;
	private Node arrIndexExpr;

	private boolean changeable = false;
	public VariableRefNode(String name) {
		this.name = name;
		this.arrIndexExpr = null;
	}
	public VariableRefNode(String name, boolean changeable) {
		this.name = name;
		this.arrIndexExpr = null;
	}
	public VariableRefNode(String name, Node arrIndexExpr) {
		this.name = name;
		this.arrIndexExpr = arrIndexExpr;
	}
	public String getName() {
		return name;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		if(arrIndexExpr == null) {
			return name;
		}
		else {
			return name + "[" + arrIndexExpr.toString() + "]";
		}
	}
}

class ArrayNode {

}