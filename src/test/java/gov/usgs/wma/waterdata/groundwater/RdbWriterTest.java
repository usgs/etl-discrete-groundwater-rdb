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
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class RdbWriterTest {

	public static final int HEADER_ROW_COUNT = 4;
	public static final int COLUMN_COUNT = 20;

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
		dgw.belowLandSurface = true;
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
		long count = rdbWriter.getHeaderRowCount();
		String writtenValue = out.toString();
		String[] headLines = rdbWriter.getHeaderRowsContent();

		// ASSERTIONS
		assertNotNull(writtenValue);
		assertTrue(writtenValue.contains("USGS"));
		assertTrue(writtenValue.contains("agency_cd"));
		assertTrue(writtenValue.contains("sl_lev_va"));
		assertTrue(writtenValue.contains("25ds"));
		assertTrue(writtenValue.contains("8ss"));

		// HEADER IS 'HEADER_ROW_COUNT' LINES LONG
		assertEquals(HEADER_ROW_COUNT, count);
		assertEquals(HEADER_ROW_COUNT, headLines.length);

		// LAST TWO ROWS SHOULD CONTAIN ONE TSV PER COLUMN
		String[] colNames = rdbWriter.getColumnNames();
		String[] colTypes = rdbWriter.getColumnTypes();
		assertEquals(colNames.length, colTypes.length, "Column names and types should have the same count");
		assertEquals(COLUMN_COUNT, colNames.length);
		assertEquals(COLUMN_COUNT, colTypes.length);
	}

	@Test
	void testRowWriteEmptyRow() {
		// SETUP
		DiscreteGroundWater dgw = new DiscreteGroundWater();

		//Some values just cannot be empty or null, so assign those values here
		dgw.aboveDatum = true;
		dgw.dateMeasured = "07-MAY-2007 18:30:47";
		LocalDateTime dateTime = LocalDateTime.of(2007, Month.MAY, 01, 12, 0);
		dgw.dateMeasuredRaw = Timestamp.valueOf(dateTime);

		rdbWriter.writeHeader();

		// ACTION UNDER TEST
		rdbWriter.writeRow(dgw);

		// POST SETUP
		String[] values = rdbWriter.getDataRowsContent()[0].split("\t", -1);

		assertEquals(1, rdbWriter.getDataRowCount());
		assertEquals(COLUMN_COUNT, values.length);

		for (int i = 0; i < COLUMN_COUNT; i++) {
			String val = values[i];
			String colName = rdbWriter.getColumnNames()[i];
			String colType = rdbWriter.getColumnTypes()[i];

			switch (colName) {
				case "lev_dt":
					assertTrue(val.matches("\\d{8}"), "lev_dt should match 'YYYYMMdd'");
					break;
				case "lev_tm":
					assertTrue(val.matches("\\d{4}"), "lev_dt should match 'HHmm'");
					break;
				case "lev_ent_cd":
					assertEquals("S", val);
					break;
				case "lev_md":
					assertEquals("07-MAY-2007 18:30:47", val);
					break;
				case "lev_dtm":
					assertEquals("01-MAY-2007 12:00:00", val);
					break;
				default:
					assertEquals("", val, "'" + val + "' should be an empty string (column index " + i + ", " + colName + ")");
			}

		}
	}

	@Test
	void testRowWriteGeneral() {
		// SETUP
		DiscreteGroundWater dgw = makeDgw();
		rdbWriter.writeHeader();

		// ACTION UNDER TEST
		rdbWriter.writeRow(dgw);

		// POST SETUP
		long count = rdbWriter.getDataRowCount();
		String[] writtenValues = rdbWriter.getDataRowsContent();
		String writtenValue = writtenValues[0];

		// ASSERTIONS
		assertEquals(1, writtenValues.length);
		assertTrue(writtenValues[0].startsWith("USGS\t"));  //Sanity check that the string actually starts as expected

		assertEquals("USGS", rdbWriter.getLastValueFor("agency_cd"));
		assertEquals("4042342342", rdbWriter.getLastValueFor("site_no"));
		assertEquals("2", rdbWriter.getLastValueFor("lev_acy_cd"));
		assertEquals("L", rdbWriter.getLastValueFor("lev_ent_cd"));
		assertEquals("USGS", rdbWriter.getLastValueFor("lev_agency_cd"));
		assertEquals("S", rdbWriter.getLastValueFor("lev_meth_cd"));
		assertEquals("T", rdbWriter.getLastValueFor("lev_age_cd"));
		assertEquals("20070501", rdbWriter.getLastValueFor("lev_dt"));
		assertEquals("1200", rdbWriter.getLastValueFor("lev_tm"));
		assertEquals("01-MAY-2007 12:00:00", rdbWriter.getLastValueFor("lev_dtm"));
		assertEquals("07-MAY-2007 18:30:47", rdbWriter.getLastValueFor("lev_md"));
		assertEquals("UTC", rdbWriter.getLastValueFor("lev_tz_cd"));
		assertEquals("D", rdbWriter.getLastValueFor("lev_dt_acy_cd"));
		assertEquals("30210", rdbWriter.getLastValueFor("parameter_code"));

	}
	@Test
	void testRowWriteBelowLand() {
		// SETUP
		DiscreteGroundWater dgw = makeDgw();

		// ACTION UNDER TEST
		rdbWriter.writeRow(dgw);

		// POST SETUP
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
		dgw.belowLandSurface = false;
		dgw.aboveDatum = true;
		dgw.verticalDatumCode = "W"; // fake test value
		dgw.displayResult = "430.23";

		// ACTION UNDER TEST
		rdbWriter.writeRow(dgw);

		// POST SETUP
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

		String headerRaw;
		ArrayList<String> dataRows = new ArrayList();

		public MockRdbWriter(Writer destination) {
			super(destination);
		}

		private void flush() {
			try {
				rdb.flush();
			} catch (IOException e) {
				e.printStackTrace(); //ignore
			}
		}
		@Override
		public RdbWriter writeHeader() {
			super.writeHeader();
			flush();
			headerRaw = out.toString();
			return this;
		}

		@Override
		protected RdbWriter writeRow(final List<String> columns) {
			super.writeRow(columns);

			flush();

			//Append the last line to our running list of dataRows
			String[] lines = out.toString().split("(\\r\\n|\\r|\\n)", -1);
			dataRows.add(lines[lines.length - 2]);  //A trailing newline is always added, so '-1' would be an empty row

			return this;
		}

		protected String[] getHeaderRowsContent() {
			String[] headLines = headerRaw.split("(\\r\\n|\\r|\\n)", 0); //0 drops any empty last row

			return headLines;
		}

		protected String[] getDataRowsContent() {
			return dataRows.toArray(new String[0]);
		}

		protected String[] getColumnNames() {
			String[] head = getHeaderRowsContent();
			String[] colNames = head[head.length - 2].split("\t", -1);
			return colNames;
		}

		protected String[] getColumnTypes() {
			String[] head = getHeaderRowsContent();
			String[] colTypes = head[head.length - 1].split("\\t", -1);
			return colTypes;
		}

		/* Find a value based on column name in the last row */
		protected String getLastValueFor(String colName) {
			String[] rows = getDataRowsContent();
			String[] vals = rows[rows.length - 1].split("\t", -1);
			String[] names = getColumnNames();

			for (int i = 0; i < names.length; i++) {
				if (colName.equals(names[i])) {
					return vals[i];
				}
			}

			return null;
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
