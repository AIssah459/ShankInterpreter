
import javax.swing.plaf.nimbus.State;
import java.util.ArrayList;
import java.util.HashMap;

public abstract class Node {
	public abstract String toString();
}

class ProgramNode extends Node {
	private HashMap<String, FunctionNode> functions;

	public ProgramNode() {
		functions = null;
	}
	
	public ProgramNode(HashMap<String, FunctionNode> functions) {
		this.functions = functions;
	}
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String t = "~~~~~PROGRAM~~~~~\n\n";
		for(FunctionNode f: functions.values()) {
			t += f.toString();
		}
		return t;
	}
}

class FunctionNode extends Node {
	private String name;
	private ArrayList<VariableNode> params;
	private ArrayList<VariableNode> vars;
	private ArrayList<Node> expressions;
	private ArrayList<StatementNode> statements;

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
		t += ":END";
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
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return target.toString() + " := " + val.toString();
	}
}

class FunctionCallNode extends StatementNode {
	String name;
	ArrayList<VariableNode> params;
	public FunctionCallNode(String n) {
		name = n;
	}
	public FunctionCallNode(String n, ArrayList<VariableNode> p) {
		name = n;
		params = p;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}

class IfNode extends Node {

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

	public IfNode getElseBlock() {
		return elseNode;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}

class WhileNode extends Node {

	BoolCompNode condition;
	ArrayList<StatementNode> statements;
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}
}

class FromNode extends Node {

	BoolCompNode condition;
	ArrayList<StatementNode> statements;
	Node from;
	Node to;
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
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
		DIV
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
			default -> throw new SyntaxErrorException("Invalid Math Operation: " + t);
		};
		this.l = l;
		this.r = r;
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
	private int from;
	private int to;
	
	public VariableNode(String name, Token.tokenType t, Object val, boolean changeable) throws SyntaxErrorException {
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
		return t;
	}
}

class VariableRefNode extends Node{
	private String name;
	private Node arrIndexExpr;
	public VariableRefNode(String name) {
		this.name = name;
		this.arrIndexExpr = null;
	}
	public VariableRefNode(String name, Node arrIndexExpr) {
		this.name = name;
		this.arrIndexExpr = arrIndexExpr;
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