package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Collection;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LocationFolderTest {

	StatePostCodeDao mockDao;
	LocationFolder locationFolder;

	@BeforeEach
	public void setup() {
		mockDao = mock(StatePostCodeDao.class);
		when(mockDao.getPostCode("Wisconsin")).thenReturn("WI");

		locationFolder = new LocationFolder(mockDao);
	}


	@Test
	void testLocationFolderToStates() {
		// SETUP
		List<String> expect = List.of("Wisconsin");

		// ACTION UNDER TEST
		List<String> actual = locationFolder.toStates("Wisconsin");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}
	@Test
	void testLocationFolderToStates_DC() {
		// SETUP
		List<String> expect = List.of("Maryland","Delaware","District of Columbia");

		// ACTION UNDER TEST
		List<String> actual = locationFolder.toStates("MD-DE-DC");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}
	@Test
	void testLocationFolderToStates_MA() {
		// SETUP
		List<String> expect = List.of("Massachusetts","Rhode Island");

		// ACTION UNDER TEST
		List<String> actual = locationFolder.toStates("MA-RI");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}
	@Test
	void testLocationFolderToStates_VT() {
		// SETUP
		List<String> expect = List.of("New Hampshire", "Vermont");

		// ACTION UNDER TEST
		List<String> actual = locationFolder.toStates("NH-VT");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}

	@Test
	void testLocationFolderToPostalCode() {
		// SETUP
		String expect = "WI";

		// ACTION UNDER TEST
		String actual = locationFolder.filenameDecorator("Wisconsin");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}

	@Test
	void testLocationFolderToPostalCode_VT() {
		// SETUP
		String expect = "NH-VT";

		// ACTION UNDER TEST
		String actual = locationFolder.filenameDecorator(expect);

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}

	@Test
	void testLocationFolderToPostalCode_DC() {
		// SETUP
		String expect = "MD-DE-DC";

		// ACTION UNDER TEST
		String actual = locationFolder.filenameDecorator(expect);

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}

	@Test
	void testLocationFolderToPostalCode_MA() {
		// SETUP
		String expect = "MA-RI";

		// ACTION UNDER TEST
		String actual = locationFolder.filenameDecorator(expect);

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}


	@Test
	void testLocationFolderToPostalCode_PI() {
		// SETUP
		String expect = "PI";

		// ACTION UNDER TEST
		String actual = locationFolder.filenameDecorator("Pacific Islands");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}


	@Test
	void testLocationFoldersLoad() {
		// SETUP

		// ACTION UNDER TEST
		Collection<String> actual = locationFolder.getLocationFolders();

		// ASSERTIONS
		assertNotNull(actual);
		// At the time of this test, Pacific Islands contained no sites.
		assertEquals(48, actual.size(), "Expect 47 location folders plus 1 for Pacific Islands for a total of 48");
	}

}
