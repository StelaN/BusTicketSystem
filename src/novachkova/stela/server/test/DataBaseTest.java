package novachkova.stela.server.test;

import java.io.FileNotFoundException;
import org.junit.Test;
import static org.junit.Assert.*;

import novachkova.stela.server.BusTicketServer;
import novachkova.stela.server.DataBase;

public class DataBaseTest {
	
	@Test(expected = FileNotFoundException.class)
	public void testInitialiseDataBaseWithIncorectFileShouldThrowException() throws FileNotFoundException {
		new DataBase("unknown");
	}
	
	@Test
	public void testGetDestinationsShouldReturnAllDestinations() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		assertEquals("Destinations should be Plovdiv" + BusTicketServer.SEPARATOR + "Stara Zagora for the test file", "Plovdiv" + BusTicketServer.SEPARATOR +"Stara Zagora", testDB.getDestinations());
	}

	@Test
	public void testTakeSeatWithInvalidDestinationShouldReturn0() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		assertEquals("Destination Nowhere should not exist and return seat number 0 taken", 0, testDB.takeSeat("Nowhere", 1));
	}
	
	@Test
	public void testTakeSeatWithFullBussShouldReturn0() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		testDB.takeSeat("Plovdiv", 1);
		assertEquals("Bus to destination Plovdiv should be full and return seat number 0 taken", 0, testDB.takeSeat("Plovdiv", 1));
	}

	@Test
	public void testTakeSeatWithCorrectParametersShouldReturnSeatNumber() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		assertEquals("Bus to destination Stara Zagora should return seat number 3 taken", 3, testDB.takeSeat("Stara Zagora", 3));
	}

	@Test
	public void testTakeSeatShouldReturnRandomSeatNumber() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		testDB.takeSeat("Stara Zagora", 1);
		assertNotSame("Bus to destination Stara Zagora should return random seat number", 1, testDB.takeSeat("Stara Zagora", 1));
		assertNotSame("Bus to destination Stara Zagora should return random seat number", 0, testDB.takeSeat("Stara Zagora", 0));
		assertNotSame("Bus to destination Stara Zagora should return random seat number", 10, testDB.takeSeat("Stara Zagora", 10));
	}
	
	@Test
	public void testBusFullShouldReturnFalse() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		assertFalse(testDB.busFull("Plovdiv"));
	}
	
	@Test
	public void testBusFullShouldReturnTrue() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		testDB.takeSeat("Plovdiv", 1);
		assertTrue(testDB.busFull("Plovdiv"));
	}
	
	@Test
	public void testBusExistShouldReturnFalse() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		assertFalse("Bus to nowhere doesn't exist", testDB.busExist("nowhere"));
	}
	
	@Test
	public void testBusExistShouldReturnTrue() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		assertTrue("Bus to Plovdiv exists", testDB.busExist("Plovdiv"));
	}
	
	@Test
	public void testGetCountSoldTicketsShouldReturnZeroForAllLines() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		assertEquals("Plovdiv: 0" + BusTicketServer.SEPARATOR + "Stara Zagora: 0",testDB.getCountSoldTickets());
	}
	
	/*@Test
	public void testBusExistAndNotFullShouldReturnFalse() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		assertFalse("Bus doesn't exist", testDB.busExistAndNotFull("nowhere"));
		testDB.takeSeat("Plovdiv", 1);
		assertFalse("Bus is full.", testDB.busExistAndNotFull("Plovdiv"));
	}
	
	@Test
	public void testBusExistAndNotFullShouldReturnTrue() throws FileNotFoundException {
		DataBase testDB = new DataBase("testInput.txt");
		assertTrue("Bus exists and is not full", testDB.busExistAndNotFull("Stara Zagora"));
	}*/
	
}
