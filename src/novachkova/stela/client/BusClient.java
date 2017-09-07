package novachkova.stela.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import novachkova.stela.server.BusTicketServer;
import novachkova.stela.util.Util;

public class BusClient {
	
	private InetAddress host;
	private int port;
	private static final String separator = BusTicketServer.SEPARATOR;
	
	public enum RequestType {
		DESTINATIONS, INCOME, SOLD_TICKETS, EMPTY_SEATS, METHOD, QUIT, BUY
	}
	
	public enum Method {
		ONLINE, CASHDESK
	}
	
	public BusClient(InetAddress host, int port) {
		super();
		this.host = host;
		this.port = port;
	}
	
	//get information about the destinations
	public String[] getDestinations() {
		return request(RequestType.DESTINATIONS.toString()).split(separator);
	}
	
	//request buying a ticket
	public int buyTicket(String destination, int seatNumber,Method buyMethod) {
		String response = request(Util.buildBuyRequest(RequestType.BUY.toString(),destination, seatNumber, buyMethod.toString(), separator));
		//if response is 0 no ticket was purchased, else response is the number of the bought seat
		try {
			return Integer.parseInt(response);
		} catch (NumberFormatException e) {
			System.err.println("Incorrected response from server to buyTicket request.");
			return 0;
		}
	}
	
	public void statistics() {
		System.out.println("Generated income: " + getGeneratedIncome());
		System.out.print("Count of sold tickets for each bus:\n" + getCountSoldTicketsForEachBus());
		System.out.print("Statistics for buy method for each bus:\n" + getBuyMethodStatForEachBus());
		System.out.print("Empty seats for each bus:\n" + getEmptySeatsForEachBus());
	}
	
	public void quit() {
		request(RequestType.QUIT.toString());
	}
	
	//send request to the server
	//return the response from the server
	private String request(String request) {
		String response = "";
		try(
			Socket socket = new Socket(host, port);
			PrintWriter out = new PrintWriter(socket.getOutputStream());
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		){
			//send request
			out.println(request);
			out.flush();
			
			//receive the response
			response = in.readLine();
		} catch (IOException e) {
			System.err.println("An error has occured in client method request. " + e.getMessage());
		}
		return response;
	}
	
	public int getGeneratedIncome() {
		String incomeFromServer = request(RequestType.INCOME.toString());
		try {
			return Integer.parseInt(incomeFromServer);
		} catch (NumberFormatException e) {
			System.err.println("Incorrect responce from server to getGeneratedIncome request.");
			return 0;
		}
		
	}
	
	public String getCountSoldTicketsForEachBus() {
		String statFromServer = request(RequestType.SOLD_TICKETS.toString());
		return Util.prettyPrintPrep(statFromServer, BusTicketServer.SEPARATOR);
	}
	
	public String getEmptySeatsForEachBus() {
		String statFromServer = request(RequestType.EMPTY_SEATS.toString());
		return Util.prettyPrintPrep(statFromServer, BusTicketServer.SEPARATOR);
	}
	
	public String getBuyMethodStatForEachBus() {
		String statFromServer = request(RequestType.METHOD.toString());
		return Util.prettyPrintPrep(statFromServer, BusTicketServer.SEPARATOR);
	}

}
