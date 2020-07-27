package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
	void testLocationFolderToStates() throws Exception {
		// SETUP
		List<String> expect = List.of("Wisconsin");

		// ACTION UNDER TEST
		List<String> actual = locationFolder.toStates("Wisconsin");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}
	@Test
	void testLocationFolderToStates_DC() throws Exception {
		// SETUP
		List<String> expect = List.of("Maryland","Delaware","District of Columbia");

		// ACTION UNDER TEST
		List<String> actual = locationFolder.toStates("MD-DE-DC");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}
	@Test
	void testLocationFolderToStates_MA() throws Exception {
		// SETUP
		List<String> expect = List.of("Massachusetts","Rhode Island");

		// ACTION UNDER TEST
		List<String> actual = locationFolder.toStates("MA-RI");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}
	@Test
	void testLocationFolderToStates_VT() throws Exception {
		// SETUP
		List<String> expect = List.of("New Hampshire", "Vermont");

		// ACTION UNDER TEST
		List<String> actual = locationFolder.toStates("NH-VT");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}

	@Test
	void testLocationFolderToPostalCode() throws Exception {
		// SETUP
		String expect = "WI";

		// ACTION UNDER TEST
		String actual = locationFolder.filenameDecorator("Wisconsin");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}

	@Test
	void testLocationFolderToPostalCode_VT() throws Exception {
		// SETUP
		String expect = "NH-VT";

		// ACTION UNDER TEST
		String actual = locationFolder.filenameDecorator(expect);

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}

	@Test
	void testLocationFolderToPostalCode_DC() throws Exception {
		// SETUP
		String expect = "MD-DE-DC";

		// ACTION UNDER TEST
		String actual = locationFolder.filenameDecorator(expect);

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}

	@Test
	void testLocationFolderToPostalCode_MA() throws Exception {
		// SETUP
		String expect = "MA-RI";

		// ACTION UNDER TEST
		String actual = locationFolder.filenameDecorator(expect);

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}


	@Test
	void testLocationFolderToPostalCode_PI() throws Exception {
		// SETUP
		String expect = "PI";

		// ACTION UNDER TEST
		String actual = locationFolder.filenameDecorator("Pacific Islands");

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals(expect, actual);
	}
}
