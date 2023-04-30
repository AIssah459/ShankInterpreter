import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public abstract class InterpreterDataType {
    public abstract String ToString();
    public abstract void FromString(String input);
}

class IntegerDataType extends InterpreterDataType {

    int val;

    public IntegerDataType(int i) {
        val = i;
    }

    public int getVal() {
        return val;
    }

    public void setVal(int val) {
        this.val = val;
    }

    public String ToString() {
        return ""+val;
    }

    public void FromString(String input) {
        val = Integer.parseInt(input);
    }
}

class RealDataType extends InterpreterDataType {

    float val;

    public RealDataType(float f) {
        val = f;
    }

    public float getVal() {
        return val;
    }

    public void setVal(float val) {
        this.val = val;
    }

    public String ToString() {
        return ""+val;
    }

    public void FromString(String input) {
        val = Float.parseFloat(input);
    }
}

class ArrayDataType<T extends InterpreterDataType> extends InterpreterDataType {
    public String ToString() {
        return null;
    }

    private T[] elements;

    public T get(int index) {
        return elements[index];
    }

    public int length() {
        return elements.length;
    }

    public void FromString(String input) {

    }
}

class StringDataType extends InterpreterDataType {

    private String val;

    public StringDataType(String s) {
        val = s;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public String ToString() {
        return val;
    }

    public void FromString(String input) {
        val = input;
    }
}

class CharacterDataType extends InterpreterDataType {

    char val;

    public CharacterDataType(char c) {
        val = c;
    }

    public char getVal() {
        return val;
    }

    public void setVal(char val) {
        this.val = val;
    }

    public String ToString() {
        return ""+val;
    }

    public void FromString(String input) throws RuntimeException {
        if(input.length() > 1) {
            throw new RuntimeException("Cannot convert multichar String to CharacterDataType");
        }
        val = input.charAt(0);
    }
}

class BoolDataType extends InterpreterDataType {

    boolean val;

    public BoolDataType(boolean b) {
        val = b;
    }

    public boolean getVal() {
        return val;
    }

    public void setVal(boolean val) {
        this.val = val;
    }

    public String ToString() {
        return String.valueOf(val);
    }

    public void FromString(String input) throws RuntimeException{
        if(input == "true") {
            val = true;
        }
        else if(input == "false") {
            val = false;
        }
        else {
            throw new RuntimeException("Couldn't convert String " + input + " to boolean.");
        }
    }
}

class BuiltInFunction extends FunctionNode {


}

class BuiltInRead extends BuiltInFunction {

    public BuiltInRead() {
        name = "Read";
    }
    public boolean isVariadic() {
        return true;
    }
    public void execute(ArrayList<InterpreterDataType> d) {
        for (InterpreterDataType p: d) {
            Scanner s = new Scanner(System.in);
            p.FromString(s.next());
        }
    }
}

class BuiltInWrite extends BuiltInFunction {

    public BuiltInWrite() {
        name = "Write";
    }
    public boolean isVariadic() {
        return true;
    }
    public void execute(ArrayList<InterpreterDataType> d) {
        for(int i = 0; i < d.size() - 1; i++) {
            System.out.print(d.get(i).ToString() + " ");
        }
        System.out.println(d.get(d.size()-1).ToString());
    }
}

class BuiltInRight extends BuiltInFunction {

    public BuiltInRight() {
        name = "Right";
    }

    public void execute(final StringDataType in, final IntegerDataType length, StringDataType out) throws StringIndexOutOfBoundsException{
        out.FromString(in.getVal().substring(in.getVal().length() - (length.getVal() + 1), in.getVal().length() - 1));
    }
}

class BuiltInLeft extends BuiltInFunction {

    public BuiltInLeft() {
        name = "Left";
    }

    public void execute(final StringDataType in, final IntegerDataType length, StringDataType out) throws StringIndexOutOfBoundsException{
        out.FromString(in.getVal().substring(0,length.getVal()));
    }
}

class BuiltInSubstring extends BuiltInFunction {

    public BuiltInSubstring() {
        name = "Substring";
    }

    public void execute(final StringDataType in, final IntegerDataType index, final IntegerDataType length, StringDataType out) throws StringIndexOutOfBoundsException{
        out.FromString(in.getVal().substring(index.getVal(), index.getVal()+length.getVal()));
    }
}

class BuiltInSqrt extends BuiltInFunction {

    public BuiltInSqrt() {
        name = "SquareRoot";
    }

    public void execute(final RealDataType arg, RealDataType out) {
        out.setVal((float)Math.sqrt(arg.getVal()));
    }
}

class BuiltInRandom extends BuiltInFunction {

    public BuiltInRandom() {
        name = "GetRandom";
    }

    public void execute(IntegerDataType out) {
        Random r = new Random();
        out.setVal(r.nextInt());
    }
}

class BuiltInIntToReal extends BuiltInFunction {

    public BuiltInIntToReal() {
        name = "IntegerToReal";
    }

    public void execute(final IntegerDataType in, RealDataType out) {
        out.setVal((float) in.getVal());
    }
}

class BuiltInRealToInt extends BuiltInFunction {

    public BuiltInRealToInt() {
        name = "RealToInteger";
    }

    public void execute(final RealDataType in, IntegerDataType out) {
        out.setVal((int) in.getVal());
    }
}

class BuiltInStart extends BuiltInFunction {

    public BuiltInStart() {
        name = "Start";
    }

    public void execute(final ArrayDataType<InterpreterDataType> arr, InterpreterDataType p) {
        p = arr.get(0);
    }
}

class BuiltInEnd extends BuiltInFunction {

    public BuiltInEnd() {
        name = "End";
    }

    public void execute(final ArrayDataType<InterpreterDataType> arr, InterpreterDataType p) {
        p = arr.get(arr.length() - 1);
    }
}
