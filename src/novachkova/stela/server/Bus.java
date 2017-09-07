package novachkova.stela.server;

public class Bus {
	private String destination;
	private int seats;
	private int ticketPrice;
	
	public Bus(String destination, int seats, int ticketPrice) {
		super();
		this.destination = destination;
		this.seats = seats;
		this.ticketPrice = ticketPrice;
	}

	public String getDestination() {
		return destination;
	}

	public int getSeats() {
		return seats;
	}

	public int getTicketPrice() {
		return ticketPrice;
	}

	@Override
	public String toString() {
		return "Bus [destination=" + destination + ", seats=" + seats + ", ticketPrice=" + ticketPrice + "]";
	}
	
	
}
