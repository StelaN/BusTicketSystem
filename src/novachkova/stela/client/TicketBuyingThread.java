package novachkova.stela.client;

import novachkova.stela.client.BusClient.Method;

public class TicketBuyingThread extends Thread {
	
	private String destination;
	private int seatNumber;
	private Method buyMethod;
	private BusClient client;
	public TicketBuyingThread(BusClient client, String destination, int seat, Method buy) {
		super();
		this.client = client;
		this.destination = destination;
		this.seatNumber = seat;
		this.buyMethod = buy;
	}
	
	@Override
	public void run() {
		client.buyTicket(destination, seatNumber, buyMethod);
	}

}
