import java.nio.file.Files;
import java.nio.file.Path;

public class Shank {
	private static void usage() {
		System.out.println("USAGE: Shank.java <filename>");
	}
	public static void main(String[] args) throws SyntaxErrorException, java.io.IOException{
		String input;
		//input = "define helloworld()\n\tconstants p = 3.14\n\tWrite \"Hello World\"\ndefine goodbyeworld()\n\tWrite \"Goodbye World\"\n";
		//input = "define add(var x: char)\n\tconstants p = 3.14\nx := (((3) * -4) -(-5)) > 1";
		input = "define a(x: char)\n\tWrite x\ndefine b()\n\ta('a')\n";
//		if(args.length != 2) {
//			usage();
//			System.exit(0);
//		}
		//Path filePath = Path.of(args[1]);
		//input = Files.readString(filePath);
		Interpreter i = new Interpreter(input);
	}
}
