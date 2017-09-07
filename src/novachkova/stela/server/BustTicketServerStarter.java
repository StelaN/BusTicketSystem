package novachkova.stela.server;
import java.io.FileNotFoundException;
import java.io.IOException;

import novachkova.stela.exception.MissingArgumentException;

public class BustTicketServerStarter {
	
	private static int PORT = BusTicketServer.SERVER_PORT;
	
	private static String inputFile = null;
	private static String outputFile = null;
	
	public static void main(String[] args) {
		try {
			parseArguments(args);
		} catch (MissingArgumentException e) {
			System.err.println(e.getMessage());
			System.err.println("Using default values of the server for this parameter.");
		}
		
		try (BusTicketServer server = new BusTicketServer(PORT, inputFile, outputFile)){
			server.start();
		} catch (FileNotFoundException e) {
			System.err.println("Couldn't open the inputfile with the information about the busses! " + e.getMessage());
		} catch (IOException e) {
			System.err.println("An error has occured. " + e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			System.err.println("The server totally crashed.");
			e.printStackTrace();
		}
		
	}
	
	private static void parseArguments(String[] args) throws MissingArgumentException{
		for(int i = 0; i < args.length; i++) {
			switch(args[i]) {
			case "-p":
				try{
					PORT = Integer.parseInt(args[++i]);
				} catch (ArrayIndexOutOfBoundsException e){
					throw new MissingArgumentException("<portnumber>", "-p");
				} catch (NumberFormatException e) {
					System.err.println("Usage: -p <portnumber>");
				}
				break;
			case "-i":
				try {
					inputFile = args[++i];
				}catch(ArrayIndexOutOfBoundsException e){
					throw new MissingArgumentException("<inputfile>", "-i");
				}
				break;
			case "-o":
				try {
					outputFile = args[++i];
				}catch(ArrayIndexOutOfBoundsException e){
					throw new MissingArgumentException("<outputfile>", "-o");
				}
				break;
			default:
				System.err.println("Unknown argument " + args[i]);
			}
		}
	}

}
