package novachkova.stela.client;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

import novachkova.stela.client.BusClient.Method;
import novachkova.stela.exception.MissingArgumentException;
import novachkova.stela.exception.UnableToConnectException;
import novachkova.stela.server.BusTicketServer;

public class BusClientStarter {

	private static InetAddress HOSTNAME;
	private static int PORT = BusTicketServer.SERVER_PORT;
	private static boolean runSimulation = true;
	private static String destination = "nowhere";
	private static int ticket = 0;
	private static Method buyMethod = Method.ONLINE;
	
	private static  final int CLIENTS_ONLINE = 30;
	private static final int CLIENTS_CASHDESK = 10;
	
	public static void main(String[] args) throws UnknownHostException {
		HOSTNAME = InetAddress.getLocalHost();
		try {
			parseArguments(args);
		} catch (MissingArgumentException e) {
			System.err.println(e.getMessage());
			System.err.println("Usage: -h <hostname> -p <portnumber> -s / -d <destination> [-t <ticketNumber>] -b <online/cashdesk>");
			//return;
		} catch (UnableToConnectException e) {
			System.err.println(e.getMessage());
		}
		//System.out.println("Connecting to server " + HOSTNAME + " on port " + PORT);
			
		if(runSimulation == false) {
			BusClient client = new BusClient(HOSTNAME, PORT);
			client.buyTicket(destination, ticket, buyMethod);
			client.statistics();
			
		} else {
			//run simulation: for every destination run 40 clients (10 on cash desk and 30 online)
			simulation();
		}
		
	}
	
	private static void simulation() {
		System.out.println("Starting simulation.");
		BusClient client = new BusClient(HOSTNAME, PORT);
		String[] destinations = client.getDestinations();
		ArrayList<Thread> threadArray = new ArrayList<>();
		for(String destination: destinations) {
			for(int i = 0; i < CLIENTS_CASHDESK; i++) {
				TicketBuyingThread th = new TicketBuyingThread(client, destination, randomSeat() , Method.CASHDESK);
				th.start();
				threadArray.add(th);
			}
			for(int i = 0; i < CLIENTS_ONLINE; i++) {
				TicketBuyingThread th = new TicketBuyingThread(client, destination, randomSeat(), Method.ONLINE);
				th.start();
				threadArray.add(th);
			}
		}
		for(int i = 0; i < threadArray.size(); i++) {
			try {
				threadArray.get(i).join();
			} catch (InterruptedException e) {
				System.err.println("Couldn't join thread.");
				e.printStackTrace();
			}
		}
		client.statistics();
	}
	
	//generate random seat number [1:50]
	private static int randomSeat() {
		Random randomGenerator = new Random();
		return randomGenerator.nextInt(50) + 1;
	}
	
	/**
	 * @param args
	 * @throws MissingArgumentException
	 * @throws UnableToConnectException
	 */
	private static void parseArguments(String[] args) throws MissingArgumentException, UnableToConnectException {
		for(int i = 0; i < args.length; i++) {
			switch(args[i]) {
			case "-s" :
				runSimulation = true;
				break;
			case "-h":
				try{
					HOSTNAME = InetAddress.getByName(args[++i]);
				} catch (ArrayIndexOutOfBoundsException e){
					throw new MissingArgumentException("<hostname>", "-h");
				} catch (UnknownHostException e) {
					throw new UnableToConnectException("Unknown host.");
				}
				break;
			case "-p":
				try {
					PORT = Integer.parseInt(args[++i]);
				} catch (ArrayIndexOutOfBoundsException e){
					throw new MissingArgumentException("<portnumber>", "-p");
				} catch (NumberFormatException e) {
					throw new UnableToConnectException("Incorrectly set port number.");
				}
				break;
			case "-d":
				try {
					destination = args[++i];
				} catch (ArrayIndexOutOfBoundsException e){
					throw new MissingArgumentException("<destination>", "-d");
				}
				break;
			case "-t":
				try {
					ticket = Integer.parseInt(args[++i]);
				} catch (ArrayIndexOutOfBoundsException e){
					throw new MissingArgumentException("<ticket>", "-t");
				} catch (NumberFormatException e) {
					System.err.println("Incorect ticket number format after command -t. Buying first available seat.");
				}
				break;
			case "-b":
				try {
					String methodString = args[++i];
					if( methodString.equalsIgnoreCase(Method.ONLINE.toString())) {
						buyMethod = Method.ONLINE;
					} else if(methodString.equalsIgnoreCase(Method.CASHDESK.toString())){
						buyMethod = Method.CASHDESK;
					} else {
						System.err.println("Unknown buy method. Buying ticket on online.");
					}
				} catch (ArrayIndexOutOfBoundsException e){
					System.err.println("Buying ticket online.");
					throw new MissingArgumentException("<online/cashdesk>", "-b");
				}
			default:
				System.err.println("Unknown argument " + args[i]);
			}
		}
	}

}
