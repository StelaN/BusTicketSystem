package novachkova.stela.server;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import novachkova.stela.util.Util;

public class DataBase {
	private String inputFile;
	
	private ArrayList<Bus> busses;
	private Map<String, AtomicBoolean[]> seatsAvailability;
	private Map<String, AtomicInteger > soldOnline;
	private Map<String, AtomicInteger > soldCashDesk;
	private AtomicInteger income = new AtomicInteger(0);
	
	public DataBase(String inputFile) throws FileNotFoundException {
		this.inputFile = inputFile;
		this.busses = new ArrayList<>();
		this.seatsAvailability = new HashMap<>();
		this.soldOnline = new HashMap<>();
		this.soldCashDesk = new HashMap<>();
		loadData();
		System.out.println("Database loaded!");
	}
	
	public ArrayList<Bus> getBusses() {
		return busses;
	}

	public Map<String, AtomicBoolean[]> getSeatsAvailability() {
		return seatsAvailability;
	}
	
	public int getIncome() {
		return income.get();
	}
	
	public void incOnlineSoldCounter(String bus) {
		soldOnline.get(bus).incrementAndGet();
	}
	
	public void incCashDeskSoldCounter(String bus) {
		soldCashDesk.get(bus).incrementAndGet();
	}
	
	//Attempts to take a seat with seatNumber for a bus to destination
	//returns 0 if it's impossible and the number of the seat taken if it's possible
	public int takeSeat(String destination, int seatNumber) {
		Bus bus = getBusByDestination(destination);
		if(!busExist(destination) || busFull(destination)) {
			return 0;
		}
		int seatIndex = seatNumber - 1;
		AtomicBoolean[] seatsInBus = seatsAvailability.get(destination);
		if(seatNumber <= bus.getSeats() && seatNumber > 0 && seatsInBus[seatIndex].compareAndSet(false, true)) {
			//the seat is empty -> take it
			income.addAndGet(bus.getTicketPrice());
			return seatNumber;
		} else {
			//Impossible seat number requested or the seat is taken -> assign random seat
			//the bus is not full so there is at least one empty seat to assign
			for(int i = 0; i < seatsInBus.length; i ++) {
				if( seatsInBus[i].compareAndSet(false, true)) {
					income.addAndGet(bus.getTicketPrice());
					//the real ticket number is the index in the array + 1
					return i + 1;
				}
			}
		}
		//it should't get to here
		return -1;
	}
	
	public synchronized boolean busFull(String busDestination) {
		AtomicBoolean[] seats = seatsAvailability.get(busDestination);
		for(AtomicBoolean seatTaken : seats) {
			//if the seat is not taken the bus is not full
			if(seatTaken.compareAndSet(false, false)) {
				return false;
			}
		}
		return true;
	}
	
	public boolean busExist(String bus) {
		return getBusByDestination(bus) != null;
	}
	
	public String getDestinations() {
		StringBuilder destinations = new StringBuilder();
		for(Bus bus: busses) {
			destinations.append(bus.getDestination() + BusTicketServer.SEPARATOR);
		}
		return Util.deleteLast(destinations, BusTicketServer.SEPARATOR).toString();
	}
	
	public String getCountSoldTickets() {
		StringBuilder stat = new StringBuilder();
		for(Bus bus: busses) {
			String destination = bus.getDestination();
			AtomicInteger countTaken = new AtomicInteger(0);
			for(AtomicBoolean seatTaken: seatsAvailability.get(destination)){
				if(seatTaken.compareAndSet(true, true)) {
					countTaken.incrementAndGet();
				}
			}
			stat.append(destination + ": " + countTaken.get() + BusTicketServer.SEPARATOR);
		}
		return Util.deleteLast(stat, BusTicketServer.SEPARATOR).toString();
	}
	
	public String getEmptySeats() {
		StringBuilder stat = new StringBuilder();
		for(Bus bus: busses) {
			String destination = bus.getDestination();
			if(busFull(destination) == true) {
				stat.append(destination + ": 0");
			} else {
				AtomicBoolean[] seats = seatsAvailability.get(destination);
				stat.append(destination + ": ");
				for(int i = 0; i < seats.length; i++) {
					if(seats[i].compareAndSet(false, false)) {
						stat.append(i+1 + ",");
					}
				}
				Util.deleteLast(stat, ",");
			}
			stat.append(BusTicketServer.SEPARATOR);
		}
		return Util.deleteLast(stat, BusTicketServer.SEPARATOR).toString();
	}

	public String getBuyMethodStatistics() {
		StringBuilder stat = new StringBuilder();
		for(Bus bus: busses) {
			String destination = bus.getDestination();
			stat.append(destination + ":" + soldOnline.get(destination) + " tickets sold online, " +  
						soldCashDesk.get(destination) +" tickets sold on cash desk" + BusTicketServer.SEPARATOR);
		}
		return Util.deleteLast(stat, BusTicketServer.SEPARATOR).toString();
	}
	
	private void loadData() throws FileNotFoundException {
		File filePath = new File(this.inputFile);
		FileReader inFile = new FileReader(filePath);
		BufferedReader buffer = new BufferedReader(inFile);
		String line;
		try {
			while((line = buffer.readLine()) != null){
				//parse the line
				parseBusInfo(line.trim());
			}
			inFile.close();
		} catch (IOException e) {
			System.err.println("Problem with reading form file " + inputFile);
			e.printStackTrace();
		}
	}
	
	private void parseBusInfo(String busString) {
		String[] busInfo = busString.split(BusTicketServer.SEPARATOR);
		if(busInfo.length == 3) {
			try {
				int busSeats = Integer.parseInt(busInfo[1].trim());
				int busTicket = Integer.parseInt(busInfo[2].trim());
				Bus newBus = new Bus(busInfo[0], busSeats, busTicket);
				busses.add(newBus);
				seatsAvailability.put(newBus.getDestination(),new AtomicBoolean[newBus.getSeats()]);
				for(int i = 0; i < newBus.getSeats(); i++) {
					seatsAvailability.get(newBus.getDestination())[i] = new AtomicBoolean(false);
				}
				soldOnline.put(newBus.getDestination(), new AtomicInteger(0));
				soldCashDesk.put(newBus.getDestination(), new AtomicInteger(0));
			} catch(NumberFormatException e) {
				System.err.println("Problem with the number of seats or the ticket price in line " + busString + ". Ommitiing line!");
			}
		} else {
			//wrong input in the line; not accurate bus information
			//ignore this line of input
			System.err.println("Problem parsing line " + busString + ". Ommitiing line!");
		}
	}
	
	private Bus getBusByDestination(String destination) {
		for(Bus bus : busses) {
			String busDestination = bus.getDestination();
			if(busDestination.equalsIgnoreCase(destination)) {
				return bus;
			}
		}
		return null;
	}
	
}
