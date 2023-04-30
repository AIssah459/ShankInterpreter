import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

public class Interpreter {

    HashMap<String, InterpreterDataType> localVars;
    HashMap<String, InterpreterDataType> constants;
    ArrayList<InterpreterDataType> params;
    ProgramNode pn;
    Parser p;
    public Interpreter(String input) throws SyntaxErrorException{
        p = new Parser(input);
        pn = p.parse();
        System.out.println(pn);
        localVars = new HashMap<>();
        constants = new HashMap<>();
        start();
    }

    public void start() throws SyntaxErrorException{
        for (FunctionNode fn: pn.getFunctions().values()) {
            interpretFunction(fn, null);
        }
    }

    //Interprets a function in the program
    private void interpretFunction(FunctionNode fn, ArrayList<InterpreterDataType> args) throws SyntaxErrorException {
        localVars = new HashMap<>();
        constants = new HashMap<>();
        constantNodes(fn);

        if(args != null) {
            for(int i = 0; i < args.size(); i++) {
                localVars.put(fn.getParams().get(i).name(), args.get(i));
            }
        }

        //Initialize variables and constants
        for (VariableNode v: fn.vars()) {
            switch(v.type()) {
                case INTEGERLIT -> localVars.put(v.name(), new IntegerDataType(((IntNode)v.getVal()).getVal()));
                case REALLIT -> localVars.put(v.name(), new RealDataType(((RealNode)v.getVal()).getVal()));
                case STRINGLIT -> localVars.put(v.name(), new StringDataType(((StringNode)v.getVal()).getVal()));
                case CHARLIT -> localVars.put(v.name(), new CharacterDataType(((CharNode)v.getVal()).getVal()));
                case TRUE, FALSE -> localVars.put(v.name(), new BoolDataType(((BooleanNode)v.getVal()).getVal()));
            }
        }

        interpretBlock(fn.statements());
        if(fn instanceof BuiltInFunction) {

        } else {
            System.out.println("Interpreted function " + fn.getName());
        }
    }

    /*
    Interprets each block of code in the function
     */
    private void interpretBlock(ArrayList<StatementNode> statements) throws SyntaxErrorException {
        for (StatementNode s: statements) {
            if(s instanceof AssignmentNode) {
                assignmentNode((AssignmentNode) s);
            } else if (s instanceof IfNode) {
                ifNode((IfNode) s);
            } else if (s instanceof ForNode) {
                forNode((ForNode) s);
            } else if(s instanceof WhileNode) {
                whileNode((WhileNode) s);
            } else if (s instanceof RepeatNode) {
                repeatNode((RepeatNode) s);
            } else if (s instanceof FunctionCallNode) {
                functionCallNode((FunctionCallNode) s);

            } else {
                throw new SyntaxErrorException("Invalid statement.");
            }
        }
    }

    private void functionCallNode(FunctionCallNode fc) throws SyntaxErrorException{
        FunctionNode fn = pn.getFunctions().get(fc.getName());
        ArrayList<ParameterNode> fcParams = fc.getParams();
        ArrayList<VariableNode> fnParams = fn.getParams();
        ArrayList<InterpreterDataType> args = new ArrayList<>();
        boolean isVariadic = fn.isVariadic();

        if(!isVariadic) {
            if(fcParams.size() != fnParams.size()) {
                throw new RuntimeException("Incorrect number of parameters in " + fc.getName() + ".");
            }
        }
        for (ParameterNode pn: fcParams) {
            Node eval = expression(pn.getVar());
            if(eval instanceof IntNode) {
                args.add(new IntegerDataType(((IntNode) eval).getVal()));
            } else if (eval instanceof RealNode) {
                args.add(new RealDataType(((RealNode) eval).getVal()));
            } else if (eval instanceof CharNode) {
                args.add(new CharacterDataType(((CharNode) eval).getVal()));
            } else if (eval instanceof StringNode) {
                args.add(new StringDataType(((StringNode) eval).toString()));
            } else if (eval instanceof BooleanNode) {
                args.add(new BoolDataType(((BooleanNode) eval).getVal()));
            } else if (eval instanceof MathOpNode) {
                args.add(mathOpNode((MathOpNode) eval));
            } else {
                throw new SyntaxErrorException("Invalid type");
            }
        }

        if(fn instanceof BuiltInFunction) {
            if(fn instanceof BuiltInRead) {
                ((BuiltInRead) fn).execute(args);
            } else if (fn instanceof BuiltInWrite) {
                ((BuiltInWrite) fn).execute(args);
            } else if (fn instanceof BuiltInRight) {
                if(args.size() != 3) {
                    throw new RuntimeException("Incorrect number of arguments in " + fc.getName() + ".");
                }
                ((BuiltInRight) fn).execute((StringDataType) args.get(0), (IntegerDataType) args.get(1), (StringDataType) args.get(2));
            } else if (fn instanceof BuiltInLeft) {
                if(args.size() != 3) {
                    throw new RuntimeException("Incorrect number of arguments in " + fc.getName() + ".");
                }
                ((BuiltInLeft) fn).execute((StringDataType) args.get(0), (IntegerDataType) args.get(1), (StringDataType) args.get(2));
            } else if (fn instanceof BuiltInSubstring) {
                if(args.size() != 4) {
                    throw new RuntimeException("Incorrect number of arguments in " + fc.getName() + ".");
                }
                ((BuiltInSubstring) fn).execute((StringDataType) args.get(0), (IntegerDataType) args.get(1), (IntegerDataType) args.get(2), (StringDataType) args.get(3));
            } else if (fn instanceof BuiltInSqrt) {
                if(args.size() != 2) {
                    throw new RuntimeException("Incorrect number of arguments in " + fc.getName() + ".");
                }
                ((BuiltInSqrt) fn).execute((RealDataType) args.get(0), (RealDataType) args.get(1));
            } else if (fn instanceof BuiltInRandom) {
                if(args.size() != 1) {
                    throw new RuntimeException("Incorrect number of arguments in " + fc.getName() + ".");
                }
                ((BuiltInRandom) fn).execute((IntegerDataType) args.get(0));
            } else if (fn instanceof BuiltInIntToReal) {
                if(args.size() != 2) {
                    throw new RuntimeException("Incorrect number of arguments in " + fc.getName() + ".");
                }
                ((BuiltInIntToReal) fn).execute((IntegerDataType) args.get(0), (RealDataType) args.get(1));
            } else if (fn instanceof BuiltInRealToInt) {
                if(args.size() != 2) {
                    throw new RuntimeException("Incorrect number of arguments in " + fc.getName() + ".");
                }
                ((BuiltInRealToInt) fn).execute((RealDataType) args.get(0), (IntegerDataType) args.get(1));
            } else if (fn instanceof BuiltInStart) {
                if(args.size() != 2) {
                    throw new RuntimeException("Incorrect number of arguments in " + fc.getName() + ".");
                }
                ((BuiltInStart) fn).execute((ArrayDataType<InterpreterDataType>) args.get(0), args.get(1));
            } else if (fn instanceof BuiltInEnd) {
                if(args.size() != 2) {
                    throw new RuntimeException("Incorrect number of arguments in " + fc.getName() + ".");
                }
                ((BuiltInEnd) fn).execute((ArrayDataType<InterpreterDataType>) args.get(0), args.get(1));
            }
        } else {
            if(args.size() != fn.getParams().size()) {
                throw new RuntimeException("Incorrect number of arguments in " + fc.getName() + ".");
            }
            interpretFunction(fn, args);
        }
    }

    /*
    Interpret assignment statements
     */
    private void assignmentNode(AssignmentNode a) throws SyntaxErrorException {
        Node val = expression(a.getVal());
        VariableRefNode target = a.getTarget();

        /*
        Check the type of value to be assigned to the target
        If the target is a constant, throws an exception
         */
        if(val instanceof MathOpNode) {
            if(constants.containsKey(target.getName()))
                throw new RuntimeException("Cannot reassign constant");
            localVars.replace(target.getName(), mathOpNode((MathOpNode) val));
        } else if (val instanceof BoolCompNode) {
            if(constants.containsKey(target.getName()))
                throw new RuntimeException("Cannot reassign constant");
            localVars.replace(target.getName(), booleanCompare((BoolCompNode) val));
        } else if (val instanceof IntNode) {
            if(constants.containsKey(target.getName()))
                throw new RuntimeException("Cannot reassign constant");
            localVars.replace(target.getName(), new IntegerDataType(((IntNode) val).getVal()));
        } else if (val instanceof RealNode) {
            if(constants.containsKey(target.getName()))
                throw new RuntimeException("Cannot reassign constant");
            localVars.replace(target.getName(), new RealDataType(((RealNode) val).getVal()));
        } else if (val instanceof StringNode) {
            if(constants.containsKey(target.getName()))
                throw new RuntimeException("Cannot reassign constant");
            localVars.replace(target.getName(), new StringDataType(((StringNode) val).getVal()));
        } else if (val instanceof CharNode) {
            if(constants.containsKey(target.getName()))
                throw new RuntimeException("Cannot reassign constant");
            localVars.replace(target.getName(), new CharacterDataType(((CharNode) val).getVal()));
        } else if (val instanceof BooleanNode) {
            if(constants.containsKey(target.getName()))
                throw new RuntimeException("Cannot reassign constant");
            localVars.replace(target.getName(), new BoolDataType(((BooleanNode) val).getVal()));
        } else throw new RuntimeException("Invalid assignment");
    }

    /*
    Interprets if statements
     */
    private void ifNode(IfNode i) throws SyntaxErrorException{
        BoolDataType b = booleanCompare(i.getCondition());
        if(b.getVal() == true) {
            interpretBlock(i.statements());
        }
        /*
        If the condition is false, interpret the else block
         */
        else {
            IfNode elseNode = i.getElseBlock();
            if(elseNode != null) {
                ifNode(elseNode);
            }
        }
    }

    /*
    Interprets the for loop until from reaches to
     */
    private void forNode(ForNode f) throws SyntaxErrorException {
        int intFrom = ((IntNode) f.getFrom()).getVal();
        int intTo = ((IntNode) f.getTo()).getVal();
        if(intFrom >= intTo) {
            throw new RuntimeException("Lower bound greater than upper bound in for statement.");
        }
        while (intFrom < intTo) {
            interpretBlock(f.statements());
            intFrom++;
        }
    }

    /*
    Interprets statements in the repeat loop until the condition
    is met
     */
    private void repeatNode(RepeatNode r) throws SyntaxErrorException{
        BoolDataType b = booleanCompare(r.getCondition());
        while(b.getVal() == false) {
            b = booleanCompare(r.getCondition());
            interpretBlock(r.statements());
        }
    }

    /*
    Interprets statements in the while loop while the condition
    is true
     */
    private void whileNode(WhileNode w) throws SyntaxErrorException{
        BoolDataType b = booleanCompare(w.getCondition());
        while (b.getVal() == true) {
            b = booleanCompare(w.getCondition());
            interpretBlock(w.statements());
        }
    }

    /*
    Returns IDT referred to by a variable reference
     */
    private InterpreterDataType variableRefNode(VariableRefNode v) {
        if(!localVars.containsKey(v.getName())) {
            throw new RuntimeException("Variable " + v.getName() + " does not exist.");
        }
        return localVars.get(v.getName());
    }

    /*
    Registers all contants in the hashmap
     */
    private void constantNodes(FunctionNode fn) throws SyntaxErrorException {
        for (VariableNode v: fn.vars()) {
            if(v.isChangeable() == false) {
                switch(v.type()) {
                    case INTEGERLIT -> constants.put(v.name(), new IntegerDataType(((IntNode)v.getVal()).getVal()));
                    case REALLIT -> constants.put(v.name(), new RealDataType(((RealNode)v.getVal()).getVal()));
                    case STRINGLIT -> constants.put(v.name(), new StringDataType(((StringNode)v.getVal()).getVal()));
                    case CHARLIT -> constants.put(v.name(), new CharacterDataType(((CharNode)v.getVal()).getVal()));
                    case TRUE, FALSE -> constants.put(v.name(), new BoolDataType(((BooleanNode)v.getVal()).getVal()));
                }
            }
        }
    }

    /*
    Evaluates a boolean compare node
     */
    private BoolDataType booleanCompare(BoolCompNode b) throws SyntaxErrorException {
        Token.tokenType condition = b.condition();
        Node lexpr = expression(b.left());
        Node rexpr = expression(b.right());

        return boolCompareHelper(lexpr, rexpr, condition);
    }

    /*
    Evaluates a boolean compare node depending on the comparison.
    Strings can only be compared with Strings. char and boolean cannot
    be compared
     */
    private BoolDataType boolCompareHelper(Node lexpr, Node rexpr, Token.tokenType condition) throws SyntaxErrorException{
        switch (condition) {
            case GREATERTHAN -> {
                if(lexpr instanceof IntNode && rexpr instanceof IntNode)
                    return new BoolDataType(((IntNode) lexpr).getVal() > ((IntNode) rexpr).getVal());
                else if (lexpr instanceof IntNode && rexpr instanceof  RealNode) {
                    return new BoolDataType(((IntNode) lexpr).getVal() > ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof IntNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() > ((IntNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof RealNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() > ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof MathOpNode) {
                    InterpreterDataType lVal = mathOpNode((MathOpNode) lexpr);
                    if(lVal instanceof IntegerDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) > ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) > ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof IntegerDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) > ((RealNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) > ((RealNode) rexpr).getVal());
                    }
                } else if (rexpr instanceof MathOpNode) {
                    InterpreterDataType rVal = mathOpNode((MathOpNode) rexpr);
                    if(rVal instanceof IntegerDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) < ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) < ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof IntegerDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) < ((RealNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) < ((RealNode) lexpr).getVal());
                    }
                } else {
                    throw new RuntimeException("Incompatible operands for >");
                }
            }
            case LESSTHAN -> {
                if(lexpr instanceof IntNode && rexpr instanceof IntNode)
                    return new BoolDataType(((IntNode) lexpr).getVal() < ((IntNode) rexpr).getVal());
                else if (lexpr instanceof IntNode && rexpr instanceof  RealNode) {
                    return new BoolDataType(((IntNode) lexpr).getVal() < ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof IntNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() < ((IntNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof RealNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() < ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof MathOpNode) {
                    InterpreterDataType lVal = mathOpNode((MathOpNode) lexpr);
                    if(lVal instanceof IntegerDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) < ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) < ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof IntegerDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) < ((RealNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) < ((RealNode) rexpr).getVal());
                    }
                } else if (rexpr instanceof MathOpNode) {
                    InterpreterDataType rVal = mathOpNode((MathOpNode) rexpr);
                    if(rVal instanceof IntegerDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) > ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) > ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof IntegerDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) > ((RealNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) > ((RealNode) lexpr).getVal());
                    }
                } else {
                    throw new RuntimeException("Incompatible operands for <");
                }
            }
            case GREATEREQ -> {
                if(lexpr instanceof IntNode && rexpr instanceof IntNode)
                    return new BoolDataType(((IntNode) lexpr).getVal() >= ((IntNode) rexpr).getVal());
                else if (lexpr instanceof IntNode && rexpr instanceof  RealNode) {
                    return new BoolDataType(((IntNode) lexpr).getVal() >= ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof IntNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() >= ((IntNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof RealNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() >= ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof MathOpNode) {
                    InterpreterDataType lVal = mathOpNode((MathOpNode) lexpr);
                    if(lVal instanceof IntegerDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) >= ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) >= ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof IntegerDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) >= ((RealNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) >= ((RealNode) rexpr).getVal());
                    }
                } else if (rexpr instanceof MathOpNode) {
                    InterpreterDataType rVal = mathOpNode((MathOpNode) rexpr);
                    if(rVal instanceof IntegerDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) <= ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) <= ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof IntegerDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) <= ((RealNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) <= ((RealNode) lexpr).getVal());
                    }
                } else {
                    throw new RuntimeException("Incompatible operands for >=");
                }
            }
            case LESSEQ -> {
                if(lexpr instanceof IntNode && rexpr instanceof IntNode)
                    return new BoolDataType(((IntNode) lexpr).getVal() <= ((IntNode) rexpr).getVal());
                else if (lexpr instanceof IntNode && rexpr instanceof  RealNode) {
                    return new BoolDataType(((IntNode) lexpr).getVal() <= ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof IntNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() <= ((IntNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof RealNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() <= ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof MathOpNode) {
                    InterpreterDataType lVal = mathOpNode((MathOpNode) lexpr);
                    if(lVal instanceof IntegerDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) <= ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) <= ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof IntegerDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) <= ((RealNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) <= ((RealNode) rexpr).getVal());
                    }
                } else if (rexpr instanceof MathOpNode) {
                    InterpreterDataType rVal = mathOpNode((MathOpNode) rexpr);
                    if(rVal instanceof IntegerDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) >= ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) >= ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof IntegerDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) >= ((RealNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) >= ((RealNode) lexpr).getVal());
                    }
                } else {
                    throw new RuntimeException("Incompatible operands for <=");
                }
            }
            case NOTEQ -> {
                if(lexpr instanceof IntNode && rexpr instanceof IntNode)
                    return new BoolDataType(((IntNode) lexpr).getVal() != ((IntNode) rexpr).getVal());
                else if (lexpr instanceof IntNode && rexpr instanceof  RealNode) {
                    return new BoolDataType(((IntNode) lexpr).getVal() != ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof IntNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() != ((IntNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof RealNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() != ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof StringNode && rexpr instanceof StringNode) {
                    return new BoolDataType(!((StringNode) lexpr).getVal().equals(((StringNode) rexpr).getVal()));
                } else if (lexpr instanceof MathOpNode) {
                    InterpreterDataType lVal = mathOpNode((MathOpNode) lexpr);
                    if(lVal instanceof IntegerDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) != ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) != ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof IntegerDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) != ((RealNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) != ((RealNode) rexpr).getVal());
                    }
                    else if (lVal instanceof StringDataType && rexpr instanceof StringNode) {
                        return new BoolDataType(!(lVal.ToString().equals(((StringNode) rexpr).getVal())));
                    }
                } else if (rexpr instanceof MathOpNode) {
                    InterpreterDataType rVal = mathOpNode((MathOpNode) rexpr);
                    if(rVal instanceof IntegerDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) != ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) != ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof IntegerDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) != ((RealNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) != ((RealNode) lexpr).getVal());
                    }
                    else if (rVal instanceof StringDataType && lexpr instanceof StringNode) {
                        return new BoolDataType(!(rVal.ToString().equals(((StringNode) lexpr).getVal())));
                    }
                } else {
                    throw new RuntimeException("Incompatible operands for !=");
                }
            }
            case EQUALS -> {
                if(lexpr instanceof IntNode && rexpr instanceof IntNode)
                    return new BoolDataType(((IntNode) lexpr).getVal() == ((IntNode) rexpr).getVal());
                else if (lexpr instanceof IntNode && rexpr instanceof  RealNode) {
                    return new BoolDataType(((IntNode) lexpr).getVal() == ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof IntNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() == ((IntNode) rexpr).getVal());
                } else if (lexpr instanceof RealNode && rexpr instanceof RealNode) {
                    return new BoolDataType(((RealNode) lexpr).getVal() == ((RealNode) rexpr).getVal());
                } else if (lexpr instanceof StringNode && rexpr instanceof StringNode) {
                    return new BoolDataType(((StringNode) lexpr).getVal().equals(((StringNode) rexpr).getVal()));
                } else if (lexpr instanceof MathOpNode) {
                    InterpreterDataType lVal = mathOpNode((MathOpNode) lexpr);
                    if(lVal instanceof IntegerDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) == ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) == ((IntNode) rexpr).getVal());
                    }
                    else if(lVal instanceof IntegerDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(lVal.ToString()) == ((RealNode) rexpr).getVal());
                    }
                    else if(lVal instanceof RealDataType && rexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(lVal.ToString()) == ((RealNode) rexpr).getVal());
                    }
                    else if (lVal instanceof StringDataType && rexpr instanceof StringNode) {
                        return new BoolDataType(lVal.ToString().equals(((StringNode) rexpr).getVal()));
                    }
                } else if (rexpr instanceof MathOpNode) {
                    InterpreterDataType rVal = mathOpNode((MathOpNode) rexpr);
                    if(rVal instanceof IntegerDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) == ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof IntNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) == ((IntNode) lexpr).getVal());
                    }
                    else if(rVal instanceof IntegerDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Integer.parseInt(rVal.ToString()) == ((RealNode) lexpr).getVal());
                    }
                    else if(rVal instanceof RealDataType && lexpr instanceof RealNode) {
                        return new BoolDataType(Float.parseFloat(rVal.ToString()) == ((RealNode) lexpr).getVal());
                    }
                    else if (rVal instanceof StringDataType && lexpr instanceof StringNode) {
                        return new BoolDataType(rVal.ToString().equals(((StringNode) lexpr).getVal()));
                    }
                } else {
                    throw new RuntimeException("Incompatible operands for =");
                }
            }
            default -> throw new RuntimeException("Unexpected boolean comparison operation type");
        }
        throw new SyntaxErrorException("Bad boolcompare");
    }

    /*
    Evaluates a math operation. Supports 5 operations: addition,
    subtraction, multiplication, division, and remainder. Can only do
    math operations on two operands of the same type
     */
    private InterpreterDataType mathOpNode(MathOpNode m) throws SyntaxErrorException{
        Node l = expression(m.left());
        Node r = expression(m.right());
        Token.tokenType operation = m.getOp();
        if(l instanceof StringNode && r instanceof StringNode) {
            if(operation != Token.tokenType.PLUS) {
                throw new SyntaxErrorException("Invalid operation on String type: " + operation.toString());
            }
            return new StringDataType((((StringNode) l).getVal()) + ((StringNode) r).getVal());
        } else if (l instanceof IntNode && r instanceof IntNode) {
            switch (operation) {
                case PLUS -> {
                    return new IntegerDataType(((IntNode) l).getVal() + ((IntNode)r).getVal());
                }
                case MINUS -> {
                    return new IntegerDataType(((IntNode) l).getVal() - ((IntNode) r).getVal());
                }
                case MUL ->  {
                    return new IntegerDataType(((IntNode) l).getVal() * ((IntNode) r).getVal());
                }
                case DIV -> {
                    return new IntegerDataType(((IntNode) l).getVal() / ((IntNode) r).getVal());
                }
                case MODULO -> {
                    return new IntegerDataType(((IntNode) l).getVal() % ((IntNode) r).getVal());
                }
            }
        } else if (l instanceof RealNode && r instanceof RealNode) {
            switch (operation) {
                case PLUS -> {
                    return new RealDataType(((RealNode) l).getVal() + ((RealNode)r).getVal());
                }
                case MINUS -> {
                    return new RealDataType(((RealNode) l).getVal() - ((RealNode)r).getVal());
                }
                case MUL ->  {
                    return new RealDataType(((RealNode) l).getVal() * ((RealNode)r).getVal());
                }
                case DIV -> {
                    return new RealDataType(((RealNode) l).getVal() / ((RealNode)r).getVal());
                }
                case MODULO -> {
                    return new RealDataType(((RealNode) l).getVal() % ((RealNode)r).getVal());
                }
            }
        } else if (l instanceof MathOpNode) {
            InterpreterDataType lVal = mathOpNode((MathOpNode) l);
            if(lVal instanceof IntegerDataType && r instanceof IntNode) {
                switch (operation) {
                    case PLUS -> {
                        return new IntegerDataType(Integer.parseInt(lVal.ToString()) + ((IntNode) r).getVal());
                    }
                    case MINUS -> {
                        return new IntegerDataType(Integer.parseInt(lVal.ToString()) - ((IntNode) r).getVal());
                    }
                    case MUL -> {
                        return new IntegerDataType(Integer.parseInt(lVal.ToString()) * ((IntNode) r).getVal());
                    }
                    case DIV -> {
                        return new IntegerDataType(Integer.parseInt(lVal.ToString()) / ((IntNode) r).getVal());
                    }
                    case MODULO -> {
                        return new IntegerDataType(Integer.parseInt(lVal.ToString()) % ((IntNode) r).getVal());
                    }
                }
            } else if (lVal instanceof RealDataType && r instanceof RealNode) {
                switch (operation) {
                    case PLUS -> {
                        return new RealDataType(Float.parseFloat(lVal.ToString()) + ((RealNode)r).getVal());
                    }
                    case MINUS -> {
                        return new RealDataType(Float.parseFloat(lVal.ToString()) - ((RealNode)r).getVal());
                    }
                    case MUL ->  {
                        return new RealDataType(Float.parseFloat(lVal.ToString()) * ((RealNode)r).getVal());
                    }
                    case DIV -> {
                        return new RealDataType(Float.parseFloat(lVal.ToString()) / ((RealNode)r).getVal());
                    }
                    case MODULO -> {
                        return new RealDataType(Float.parseFloat(lVal.ToString()) % ((RealNode)r).getVal());
                    }
                }
            }
        } else if (r instanceof MathOpNode) {
            InterpreterDataType rVal = mathOpNode((MathOpNode) r);
            if(rVal instanceof IntegerDataType && l instanceof IntNode) {
                switch (operation) {
                    case PLUS -> {
                        return new IntegerDataType(Integer.parseInt(rVal.ToString()) + ((IntNode) l).getVal());
                    }
                    case MINUS -> {
                        return new IntegerDataType(Integer.parseInt(rVal.ToString()) - ((IntNode) l).getVal());
                    }
                    case MUL -> {
                        return new IntegerDataType(Integer.parseInt(rVal.ToString()) * ((IntNode) l).getVal());
                    }
                    case DIV -> {
                        return new IntegerDataType(Integer.parseInt(rVal.ToString()) / ((IntNode) l).getVal());
                    }
                    case MODULO -> {
                        return new IntegerDataType(Integer.parseInt(rVal.ToString()) % ((IntNode) l).getVal());
                    }
                }
            } else if (rVal instanceof RealDataType && l instanceof RealNode) {
                switch (operation) {
                    case PLUS -> {
                        return new RealDataType(Float.parseFloat(rVal.ToString()) + ((RealNode)l).getVal());
                    }
                    case MINUS -> {
                        return new RealDataType(Float.parseFloat(rVal.ToString()) - ((RealNode)l).getVal());
                    }
                    case MUL ->  {
                        return new RealDataType(Float.parseFloat(rVal.ToString()) * ((RealNode)l).getVal());
                    }
                    case DIV -> {
                        return new RealDataType(Float.parseFloat(rVal.ToString()) / ((RealNode)l).getVal());
                    }
                    case MODULO -> {
                        return new RealDataType(Float.parseFloat(rVal.ToString()) % ((RealNode)l).getVal());
                    }
                }
            }
        } else {
            throw new RuntimeException("Invalid types for operands of MathOpNode");
        }
        return null;
    }

    /*
    Returns an expression
     */
    private Node expression(Node n) throws SyntaxErrorException{
        if(n instanceof MathOpNode) {
            mathOpNode((MathOpNode) n);
        } else if (n instanceof BoolCompNode) {
            booleanCompare((BoolCompNode) n);
        } else if(n instanceof VariableRefNode) {
            InterpreterDataType varRef = localVars.get(((VariableRefNode) n).getName());
            if(varRef instanceof IntegerDataType) {
                return new IntNode(((IntegerDataType) varRef).getVal());
            } else if (varRef instanceof RealDataType) {
                return new RealNode(((RealDataType) varRef).getVal());
            } else if (varRef instanceof StringDataType) {
                return new StringNode(((StringDataType) varRef).getVal());
            } else if (varRef instanceof CharacterDataType) {
                return new CharNode(((CharacterDataType) varRef).getVal());
            }
            else {
                throw new RuntimeException("Illegal variable reference type in math operation");
            }
        } else if (n instanceof IntNode) {
            return n;
        } else if (n instanceof RealNode) {
            return n;
        } else if (n instanceof StringNode) {
            return n;
        } else if (n instanceof CharNode) {
            return n;
        } else if (n instanceof BooleanNode) {
            return n;
        } else {
            throw new RuntimeException("Illegal Node type in expression()");
        }
        return n;
    }
}