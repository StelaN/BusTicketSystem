package novachkova.stela.server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import novachkova.stela.client.BusClient.RequestType;
import novachkova.stela.util.Util;
import novachkova.stela.client.BusClient.Method;

public class BusTicketServer implements AutoCloseable {
	public static final String SEPARATOR = "-";
	
	private String inputFile = "input.txt";
	private String outputFile = "output.doc";
	public static int SERVER_PORT = 4444; 
	
	private DataBase dbBusses;
	private ServerSocket serverSocket;
	private Map<Socket, ClientConnectionThread> clients;
	private ConcurrentLinkedQueue<String> cashDeskQueue;
	
	private class ClientConnectionThread extends Thread {
			private Socket socket;
			private volatile boolean run = true;
			
			private static final long ONLINE_WAITING_TIME = 200;
			private static final long CASHDESK_WAITING_TIME = 1000;
			
			public ClientConnectionThread(Socket socket) {
				this.socket = socket;
			}
			
			@Override
			public void run() {
				//System.out.println("Client " + socket + " connected");
	
				try (
					BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintWriter printWriter = new PrintWriter(socket.getOutputStream())
				) {
					while(run) {
						String request = reader.readLine();
						String response = null;
						if (request == null || request.equals(RequestType.QUIT.toString())) {
							// The connection is broken. Exit the thread
							break;
						} else if(request.equals(RequestType.DESTINATIONS.toString())) {
							//return the destinations of the busses split with '-'
							response = dbBusses.getDestinations();
						} else if(request.equals(RequestType.INCOME.toString())) {
							//return the income of the soled tickets
							response = dbBusses.getIncome() + "";
						} else if(request.equals(RequestType.SOLD_TICKETS.toString())) {
							//return the count of sold tickets for each bus
							response = dbBusses.getCountSoldTickets();
						} else if(request.equals(RequestType.EMPTY_SEATS.toString())) {
							//return the the numbers of the empty seats for each bus
							response = dbBusses.getEmptySeats();
						} else if(request.equals(RequestType.METHOD.toString())) {
							//return the count of tickets sold online and on cash desk for each bus
							response = dbBusses.getBuyMethodStatistics();
						} else if(request.startsWith(RequestType.BUY.toString())){
							//sell the requested ticket for the requested destination, return 0 or ticket number
							if(isCashDesk(request)) {
								response = sellTicketCashDesk(requestedDestination(request), requestedTicket(request));
							} else {
								response = sellTicketOnline(requestedDestination(request), requestedTicket(request));
							}
						} else {
							response = "Unknown request!";
						}
						
						// Write back to the client
						printWriter.println(response);
						printWriter.flush();
	
					}
				} catch (IOException e) {
					System.err.println("An error occurred while reading or writing to the client. " + e.getMessage());
				} catch (InterruptedException e) {
					System.err.println("The sleeping thread was interrupted" + e.getMessage());
				}
				//System.out.println("Number of connections: " + countClients());
				//System.out.println("Client " + socket + " disconnected");				
				clients.remove(socket);
			}
			
			private synchronized String sellTicketOnline(String destination, int seatNumber) throws InterruptedException {
				if(!busExist(destination)) {
					return "0";
				}
				if(!busFull(destination)) {
					Thread.sleep(ONLINE_WAITING_TIME);
				}
				int soldTicket = dbBusses.takeSeat(destination, seatNumber);
				if(soldTicket > 0) {
					dbBusses.incOnlineSoldCounter(destination);
					writeToFile(soldTicket, destination, Method.ONLINE.toString());
				}
				return soldTicket + "";
			}
			
			private synchronized String sellTicketCashDesk(String destination, int seat) throws InterruptedException {
				//TODO: put cash desk buyers in queue
				/*cashDeskQueue.add(this.getId());
				while(this.getId() != cashDeskQueue.peek()) {
					try {
						wait();
					} catch (InterruptedException e) {
						System.err.println("Thread " + this.getId() + " was interrupted.");
						e.printStackTrace();
					}
				}
				cashDeskQueue.poll();
				notifyAll();*/
				
				if(!busExist(destination)){
					return "0";
				}
				if(!busFull(destination)) {
					Thread.sleep(CASHDESK_WAITING_TIME);
				}
				int soldTicket = dbBusses.takeSeat(destination, seat);
				if(soldTicket > 0) {
					dbBusses.incCashDeskSoldCounter(destination);
					writeToFile(soldTicket, destination, Method.CASHDESK.toString());
				}
				return soldTicket + "";
			}
			
			private boolean isCashDesk(String request) {
				return requestedBuyMethod(request).equals(Method.CASHDESK.toString());
			}

			private synchronized boolean busExist(String bus) {
				return dbBusses.busExist(bus);
			}
			
			private synchronized boolean busFull(String destination) {
				return dbBusses.busFull(destination);
			}
			
			
			private String countClients() {
				return Integer.toString(clients.size());
			}
			
			//format of the request: BUY-destination-seatNumber-buyMethod (ONLINE/CASHDESK)
			private String requestedDestination(String request) {
				return Util.parseBuyRequest(request, SEPARATOR)[1];
			}
			
			//format of the request: BUY-destination-seatNumber-buyMethod (ONLINE/CASHDESK)
			private int requestedTicket(String request) {
				String ticketString = Util.parseBuyRequest(request, SEPARATOR)[2];
				try { 
					return Integer.parseInt(ticketString);
				} catch (NumberFormatException e) {
					return 0;
				}
			}
			
			//format of the request: BUY-destination-seatNumber-buyMethod (ONLINE/CASHDESK)
			private String requestedBuyMethod(String request) {
				return Util.parseBuyRequest(request, SEPARATOR)[3];
			}
			
			private synchronized void writeToFile(int seat, String destination, String buyMethod) {
				try {
					File filePath = null;
					if(new File(outputFile).isDirectory()) {
						filePath = new File(outputFile, "output.doc");
					} else {
						filePath = new File(outputFile);
					}
					FileWriter filewriter = new FileWriter(filePath,true);
					PrintWriter output = new PrintWriter(filewriter);
					output.printf("Successful purchase of seat %d to %s. The ticket was bought %s.\n", seat, destination, buyMethod);
					output.close();
				} catch (IOException e) {
					System.err.println("Couldn't file to write " + outputFile + ". " + e.getMessage());
				}
			}
			
			public void stopThread() {
				run = false;
				if (socket != null) {
					try {
						socket.close();
					} catch (IOException e) {
						// Nothing that we can do
					}
				}
			}
		}
	
	
	
	public BusTicketServer(int port, String inputFile, String outputFile) throws IOException {
		super();
		if(port != -1) {
			BusTicketServer.SERVER_PORT = port;
		}
		this.serverSocket = new ServerSocket(SERVER_PORT);
		this.clients = new HashMap<>();
		this.cashDeskQueue = new ConcurrentLinkedQueue<>();
		if(inputFile != null) {
			this.inputFile = inputFile;
		} 
		if(outputFile != null) {
			this.outputFile = outputFile;
		}
		this.dbBusses = new DataBase(this.inputFile); 
	}
	
	public static int getServerPort() {
		return SERVER_PORT;
	}
	
	public static String getSeparator() {
		return SEPARATOR;
	}
	
	// Allow the server to accept new connections.
	public void start() throws IOException {
		while (true) {
			// Accepts new clients
			Socket socket = serverSocket.accept();
			// Processes the clients into a new thread
			ClientConnectionThread clientThread = new ClientConnectionThread(socket);
			clients.put(socket, clientThread);

			// We set the thread as a daemon. If only daemon threads are left the java application will exit
			clientThread.setDaemon(true);
			clientThread.start();
		}
	}

	@Override
	public void close() throws Exception {
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.err.println("Could not close the server socket. " + e.getMessage());
			}
		}
		// Stop all client connections
		for (ClientConnectionThread client : clients.values()) {
			client.stopThread();
		}
		clients.clear();
		System.out.println("Server closed!");
	}
}
