package assignment2;

import edu.utexas.cs.sam.io.SamTokenizer;
import edu.utexas.cs.sam.io.Tokenizer;
import edu.utexas.cs.sam.io.Tokenizer.TokenType;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LiveOak2Compiler
{
	public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.err.println("Usage: java ProgramName inputFile outputFile");
            System.exit(1);
        }

        String inFileName = args[0];
        String outFileName = args[1];

        try {
            String samCode = compiler(inFileName);

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(outFileName))) {
                writer.write(samCode);
            }
        } catch (IOException e) {
            System.err.println("Error processing files: " + e.getMessage());
            e.printStackTrace();
        }
	}

	static String compiler(String fileName)
	{
		//returns SaM code for program in file
		try
		{
			SamTokenizer f = new SamTokenizer(fileName, SamTokenizer.TokenizerOptions.PROCESS_STRINGS);
			String pgm = getProgram(f);
			return pgm;
		}
		catch (Exception e)
		{
			System.out.println("Fatal error: could not compile program");
			return "STOP\n";
		}
	}

	static String getProgram(SamTokenizer f)
	{
		try
		{
			String pgm="";
			while(f.peekAtKind()!=TokenType.EOF)
			{
				pgm+= getMethod(f);
			}
			return pgm;
		}
		catch(Exception e)
		{
			System.out.println("Fatal error: could not compile program");
			return "STOP\n";
		}
	}
	static String getMethod(SamTokenizer f)
	{
		//TODO: add code to convert a method declaration to SaM code.
		//TODO: add appropriate exception handlers to generate useful error msgs.
		f.check("int"); //must match at begining
		String methodName = f.getString();
		f.check ("("); // must be an opening parenthesis
		String formals = getFormals(f);
		f.check(")");  // must be an closing parenthesis
		//You would need to read in formals if any
		//And then have calls to getDeclarations and getStatements.
		return null;
	}

	static String getExp(SamTokenizer f)
	{
        switch (f.peekAtKind()) {
            case INTEGER: //E -> integer
                return "PUSHIMM " + f.getInt() + "\n";

            case OPERATOR:
            {
            }
        default:   return "ERROR\n";
        }
	}

	static String getFormals(SamTokenizer f){
        return null;
	}
}
