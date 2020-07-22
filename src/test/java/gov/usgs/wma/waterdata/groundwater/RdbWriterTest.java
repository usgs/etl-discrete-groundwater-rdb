package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RdbWriterTest {

	ByteArrayOutputStream out;
	Writer dest;

	@BeforeEach
	public void setup() {
		out = new ByteArrayOutputStream();
		dest = new OutputStreamWriter(out);
	}

	DiscreteGroundWater makeDgw() {
		DiscreteGroundWater dgw = new DiscreteGroundWater();
		dgw.agencyCode = "USGS";
		dgw.siteIdentificationNumber = "4042342342";
		// date
		// time
		dgw.levelFeetBelowLandSurface = "23.06";
		// entry code [S]see or [L]land
		dgw.verticalDatumCode = ""; // only if vertical measurement
		dgw.levelFeetAboveVerticalDatum = "";  // usually only one set in the file
		dgw.measurementSourceCode = "";
		dgw.measuringAgencyCode = "USGS";
		dgw.levelAccuracyCode = "2"; // two digits after decimal point
		dgw.siteStatusCode = ""; // R, S or blank
		dgw.measurementMethodCode = "S"; // S, R, or V
		dgw.dateMeasured = "07-MAY-2007 18:30:47";
		LocalDateTime dateTime = LocalDateTime.of(2007, Month.MAY, 01, 12, 0);
		dgw.dateMeasuredRaw = Timestamp.valueOf(dateTime);
		dgw.dateTimeAccuracyCode = "D"; // [D]day or [M]minute
		dgw.timezoneCode = "UTC";
		dgw.timeMeasuredUtc = "01-MAY-2007 12:00:00"; // UTC
		dgw.approvalStatusCode = "T"; // T or R

		dgw.parameterCode = "NOT IMPL YET";

		//07-MAY-2007 18:30:47	01-MAY-2007 12:00:00

		return dgw;
	}

	@Test
	void testColumnWritenTrimmedLength() throws Exception {
		String givenValue = "valuePlusMore";

		// ACTION UNDER TEST
		new RdbWriter(dest).writeValue(5, givenValue).close();

		// POST SETUP
		String writtenValue = out.toString();

		// ASSERTIONS
		assertNotNull(writtenValue);
		assertEquals("value\t", writtenValue);
	}

	@Test
	void testColumnWritenFullLength() throws Exception {
		String givenValue = "value";

		// ACTION UNDER TEST
		new RdbWriter(dest).writeValue(15, givenValue).close();

		// POST SETUP
		String writtenValue = out.toString();

		// ASSERTIONS
		assertNotNull(writtenValue);
		assertEquals("value\t", writtenValue);
	}

	@Test
	void testHeaderWriten() throws Exception {
		// ACTION UNDER TEST
		long count = new RdbWriter(dest).writeHeader().close().getHeaderRows();

		// POST SETUP
		String writtenValue = out.toString();

		// ASSERTIONS
		assertNotNull(writtenValue);
		assertTrue(writtenValue.contains("USGS"));
		assertTrue(writtenValue.contains("agency_cd"));
		assertTrue(writtenValue.contains("sl_lev_va"));
		assertTrue(writtenValue.contains("25ds"));
		assertTrue(writtenValue.contains("8ss"));

		assertEquals(4, count);
	}

	@Test
	void testRowWriteGeneral() throws Exception {
		// SETUP
		DiscreteGroundWater dgw = makeDgw();

		// ACTION UNDER TEST
		long count = new RdbWriter(dest).writeRow(dgw).close().getDataRows();

		// POST SETUP
		String writtenValue = out.toString();

		// ASSERTIONS
		assertNotNull(writtenValue);
		assertTrue(writtenValue.contains("\tUSGS\t"));
		assertTrue(writtenValue.contains("\t4042342342\t"));
		assertTrue(writtenValue.contains("\t2\t"));
		assertTrue(writtenValue.contains("\tS\t"));
		assertTrue(writtenValue.contains("\tT\t"));
		assertTrue(writtenValue.contains("\t200705"));
		assertTrue(writtenValue.contains("\t20070501\t"));
		assertTrue(!writtenValue.contains("\t1200\t"));
		assertTrue(!writtenValue.contains("\t1830\t"));
		assertTrue(writtenValue.contains("\t07-MAY-2007 18:30:47\t"));
		assertTrue(writtenValue.contains("\t01-MAY-2007 12:00:00\t")); // measured and UTC dates
		assertTrue(writtenValue.contains("\tUTC\t"));
		assertTrue(writtenValue.contains("\tD\t"));

		assertEquals(1, count);
	}
	@Test
	void testRowWriteBelowLand() throws Exception {
		// SETUP
		DiscreteGroundWater dgw = makeDgw();

		// ACTION UNDER TEST
		new RdbWriter(dest).writeRow(dgw).flush();

		// POST SETUP
		String writtenValue = out.toString();

		// ASSERTIONS
		assertTrue(writtenValue.contains("\t23.06\tL\t"));
	}

	@Test
	void testRowWriteAboveSea() throws Exception {
		// SETUP
		DiscreteGroundWater dgw = makeDgw();
		dgw.levelFeetBelowLandSurface = "";
		// entry code [S]see or [L]land
		dgw.verticalDatumCode = "W"; // fake test value
		dgw.levelFeetAboveVerticalDatum = "430.23";

		// ACTION UNDER TEST
		new RdbWriter(dest).writeRow(dgw).flush();

		// POST SETUP
		String writtenValue = out.toString();

		// ASSERTIONS
		assertTrue(writtenValue.contains("\tS\tW\t430.23\t"));
	}
}
