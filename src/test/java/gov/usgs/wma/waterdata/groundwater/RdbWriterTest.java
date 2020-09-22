package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RdbWriterTest {

	ByteArrayOutputStream out;
	Writer destination;
	MockRdbWriter rdbWriter;

	@BeforeEach
	public void setup() {
		out = new ByteArrayOutputStream();
		destination = new OutputStreamWriter(out);
		rdbWriter = new MockRdbWriter(destination);
	}

	DiscreteGroundWater makeDgw() {
		DiscreteGroundWater dgw = new DiscreteGroundWater();
		dgw.agencyCode = "USGS";
		dgw.siteIdentificationNumber = "4042342342";
		// date
		// time
		dgw.displayResult = "23.06";
		// entry code [S]see or [L]land
		dgw.verticalDatumCode = ""; // only if vertical measurement
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

		dgw.parameterCode = "30210";

		return dgw;
	}

	@Test
	void testColumnWritenTrimmedLength() {
		String givenValue = "valuePlusMore";

		// ACTION UNDER TEST
		String actual = rdbWriter.validateValue(5, givenValue);

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals("value", actual);
	}

	@Test
	void testColumnWritenFullLength() {
		String givenValue = "value";

		// ACTION UNDER TEST
		String actual = rdbWriter.validateValue(15, givenValue);

		// ASSERTIONS
		assertNotNull(actual);
		assertEquals("value", actual);
	}

	@Test
	void testHeaderWriten() {
		// ACTION UNDER TEST
		rdbWriter.writeHeader();

		// POST SETUP
		long count = rdbWriter.close().getHeaderRows();
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
	void testRowWriteGeneral() {
		// SETUP
		DiscreteGroundWater dgw = makeDgw();

		// ACTION UNDER TEST
		rdbWriter.writeRow(dgw);

		// POST SETUP
		long count = rdbWriter.close().getDataRows();
		String writtenValue = out.toString();

		// ASSERTIONS
		assertNotNull(writtenValue);
		assertTrue(writtenValue.startsWith("USGS\t")); // first USGS value
		assertTrue(writtenValue.contains("\tUSGS\t")); // second USGS value
		assertTrue(writtenValue.contains("\t4042342342\t"));
		assertTrue(writtenValue.contains("\t2\t"));
		assertTrue(writtenValue.contains("\tS\t"));
		assertTrue(writtenValue.contains("\tT\t"));
		assertTrue(writtenValue.contains("\t200705"));
		assertTrue(writtenValue.contains("\t20070501\t"));
		assertTrue(writtenValue.contains("\t1200\t"));
		assertFalse(writtenValue.contains("\t1830\t"));
		assertTrue(writtenValue.contains("\t07-MAY-2007 18:30:47\t"));
		assertTrue(writtenValue.contains("\t01-MAY-2007 12:00:00\t")); // measured and UTC dates
		assertTrue(writtenValue.contains("\tUTC\t"));
		assertTrue(writtenValue.contains("\tD\t"));
		assertTrue(writtenValue.contains("\t30210")); // last value

		assertEquals(1, count);
	}
	@Test
	void testRowWriteBelowLand() {
		// SETUP
		DiscreteGroundWater dgw = makeDgw();

		// ACTION UNDER TEST
		rdbWriter.writeRow(dgw);

		// POST SETUP
		rdbWriter.close();
		String writtenValue = out.toString();

		// ASSERTIONS
		assertTrue(writtenValue.contains("\t23.06\tL\t"));
	}

	@Test
	void testRowWriteAboveSea() {
		// SETUP
		DiscreteGroundWater dgw = makeDgw();
		// entry code [S]see or [L]land
		dgw.parameterCode = "72150";
		dgw.verticalDatumCode = "W"; // fake test value
		dgw.displayResult = "430.23";

		// ACTION UNDER TEST
		rdbWriter.writeRow(dgw);

		// POST SETUP
		rdbWriter.close();
		String writtenValue = out.toString();

		// ASSERTIONS
		assertTrue(writtenValue.contains("\tS\tW\t430.23\t"));
		
	}

	@Test
	void testHeaderWriteIOE() throws Exception {
		// SETUP
		Writer mockWriter = Mockito.mock(Writer.class);
		Mockito.when(mockWriter.append(Mockito.any(CharSequence.class))).thenThrow(new IOException());
		RdbWriter rdbWriter = new RdbWriter(destination);
		rdbWriter.rdb = mockWriter;

		// ACTION UNDER TEST
		// ASSERTIONS
		assertThrows(RuntimeException.class, ()->rdbWriter.writeHeader());
	}
	@Test
	void testRowWriteIOE() throws Exception {
		// SETUP
		Writer mockWriter = Mockito.mock(Writer.class);
		Mockito.when(mockWriter.append(Mockito.any(CharSequence.class))).thenThrow(new IOException());
		RdbWriter rdbWriter = new RdbWriter(destination);
		rdbWriter.rdb = mockWriter;
		DiscreteGroundWater dgw = makeDgw();

		// ACTION UNDER TEST
		// ASSERTIONS
		assertThrows(RuntimeException.class, ()->rdbWriter.writeRow(dgw));
	}

	@Test
	void testRowWriteEofIoe() {
		// SETUP
		Writer mockWriter = new BufferedWriter(destination) {
			@Override
			public Writer append(CharSequence csq) throws IOException {
				if (csq.equals("\n")) {
					throw new IOException("Test exception block");
				}
				return this;
			}
		};
		// This mock action failed to produce an exception for testing
		// Writer mockWriter = Mockito.mock(Writer.class);
		// Mockito.when(mockWriter.append("\n")).thenThrow(new IOException());
		// Mockito.when(mockWriter.append(Mockito.any(CharSequence.class))).thenReturn(mockWriter);
		RdbWriter rdbWriter = new RdbWriter(destination);
		rdbWriter.rdb = mockWriter;
		DiscreteGroundWater dgw = makeDgw();

		// ACTION UNDER TEST
		// ASSERTIONS
		assertThrows(RuntimeException.class, ()->rdbWriter.writeRow(dgw));
	}

	// Helper class to manage closing the Writer.
	// These methods used to be located in the superclass.
	// They were added only for testing and thus moved here.
	class MockRdbWriter extends RdbWriter {
		public MockRdbWriter(Writer destination) {
			super(destination);
		}

		protected MockRdbWriter flush() {
			try {
				rdb.flush();
			} catch (IOException e) {
				// do not care about flush exceptions
			}
			return this;
		}

		protected MockRdbWriter close() {
			try {
				flush();
				rdb.close();
			} catch (IOException e) {
				// do not care about close exceptions
			}
			return this;
		}
	}

	@Test
	void testSimpleDateFormat_1982_12_31_ERROR() {
		// SETUP
		LocalDateTime dateTime = LocalDateTime.of(1982, Month.DECEMBER, 31, 12, 0);

		// ACTION UNDER TEST
		String actual = new SimpleDateFormat("YYYYMMdd").format(Timestamp.valueOf(dateTime));

		// ASSERTIONS - these pass if there is a bug in SimpleDateFormat
		assertEquals("1982-12-31T12:00", dateTime.toString());
		assertEquals("19831231", actual, "Java lib error formats as 1983 but should be 1982");
		assertNotEquals("19821231", actual, "Java lib error formats as 1983 but should be 1982");
	}
	@Test
	void testDateTimeFormat_1982_12_31_ERROR() {
		// SETUP
		LocalDateTime dateTime = LocalDateTime.of(1982, Month.DECEMBER, 31, 12, 0);
		DateTimeFormatter format = DateTimeFormatter.ofPattern("YYYYMMdd");

		// ACTION UNDER TEST
		String actual = dateTime.format(format);

		// ASSERTIONS - these pass if there is a bug in SimpleDateFormat
		assertEquals("1982-12-31T12:00", dateTime.toString());
		assertNotEquals("19821231", actual, "Java lib error formats as 1983 but should be 1982");
		assertEquals("19831231", actual, "Java lib error formats as 1983 but should be 1982");
	}
	@Test
	void testJodaFormat_1982_12_31()  {
		// SETUP
		org.joda.time.format.DateTimeFormatter construction = org.joda.time.format.DateTimeFormat.forPattern("YYYY-MM-dd HH:mm");
		org.joda.time.DateTime jodaDT = DateTime.parse("1982-12-31 12:00", construction);
		org.joda.time.format.DateTimeFormatter testFormat = org.joda.time.format.DateTimeFormat.forPattern("YYYYMMdd");

		// ACTION UNDER TEST
		String actual = jodaDT.toString(testFormat);

		// ASSERTIONS - these pass if there is a bug in SimpleDateFormat
		//		assertEquals("1982-12-31T12:00", dateTime.toString());
		assertEquals("19821231", actual);
		assertNotEquals("19831231", actual);
	}
}
