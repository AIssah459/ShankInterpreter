

public class Shank {
	public static void main(String[] args) {
		String input;
		input = "define func(var x: integer, y:string)\n\tconstants pi = 3.14\n\tconstants name = \"Gary\"\n\tvariables a, b, c: integer\n\tx[5] := (((3) * -4) -(-5)) > 1\n\ty := 3 = 5";
		//input = "define f(var x: char)\n\tconstants p = 3.14\nx := (((3) * -4) -(-5)) > 1";
		Parser p = new Parser(input);
		try {
			System.out.print(p.parse());
		} catch (SyntaxErrorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
