package novachkova.stela.server.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import novachkova.stela.util.Util;

@RunWith(Parameterized.class)
public class UtilTest {
	
	@Parameter(0)
    public String first;
    @Parameter(1)
    public int second;
    @Parameter(2)
    public String third;
    @Parameter(3)
    public String separator;


    // creates the test data
    @Parameters
    public static Collection<Object[]> data() {
        Object[][] data = new Object[][] { {"Plovdiv" , 20, "ONLINE", "-"}, {"Varna", 3, "", " next "}, {"", 4, "CASHDESK", " "}, {"Burgas", 10, "ONLINE", ""} };
        return Arrays.asList(data);
    }

	@Test
	public void testDeleteLastCharShouldDeleteLastChar() {
		assertEquals("The output should be 'test'", "test", Util.deleteLastChar(new StringBuilder("test1")).toString());
		assertEquals("The output should be ''", "", Util.deleteLastChar(new StringBuilder(" ")).toString());
	}
	
	@Test
	public void testDeleteLastCharShouldNotCrashWithEmpltyInput() {
		assertEquals("The output should be ''", "", Util.deleteLastChar(new StringBuilder()).toString());
	}

	@Test
	public void testDeleteLastShouldNotChangeInputIfStringNotFound() {
		StringBuilder sb = new StringBuilder("testing");
		assertSame("The output should be the same as the input", sb, Util.deleteLast(sb, "not found"));
		assertEquals("The output should be the same as the input", "testing", Util.deleteLast(new StringBuilder("testing"), "").toString());
	}
	
	@Test
	public void testDeleteLastShouldNotCrashWithEmptyInput() {
		assertEquals("The output should be ''", "", Util.deleteLast(new StringBuilder(), "testing").toString());
	}
	
	@Test
	public void testDeleteLastShouldDeleteLastOcurrenceOfString() {
		assertEquals("The output should be 'testing '", "testing ", Util.deleteLast(new StringBuilder("testing testing"), "testing").toString());
		assertEquals("The output should be 'testing again'", "testing again", Util.deleteLast(new StringBuilder("testing testing again"), "testing ").toString());
		assertEquals("The output should be ''", "", Util.deleteLast(new StringBuilder("testing"), "testing").toString());
		assertEquals("The output should be ' again'", " again", Util.deleteLast(new StringBuilder("testing again"), "testing").toString());
	}

	@Test
	public void testBuildStringRequestShouldBuildAccurateString() {
		assertEquals("BUY" + separator + first + separator + second + separator + third, Util.buildBuyRequest("BUY",first, second, third, separator));
	}

	@Test
	public void testPrettyPrintPrepShouldNotChangeInputIfSeparatorEmpty() {
		assertEquals("The output should be the same as the input", "test", Util.prettyPrintPrep("test", ""));
	}
	
	@Test
	public void testPrettyPrintPrepShouldAddNewLineIfSeparatorNotFound() {
		assertEquals("The output should be the input and new line","test\n", Util.prettyPrintPrep("test", "-"));
	}
	
	@Test
	public void testPrettyPrintPrepShouldReturnStringWithNewLinesInsteadOfSeparators() {
		assertEquals("This\nis\na\ntest\n", Util.prettyPrintPrep("This-is-a-test", "-"));
	}

}
