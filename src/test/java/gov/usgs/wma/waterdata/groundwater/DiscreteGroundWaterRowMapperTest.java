package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import org.joda.time.DateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DiscreteGroundWaterRowMapperTest {

	ByteArrayOutputStream out;
	Writer destination;
	ResultSet mrs;
	DiscreteGroundWater dgw;

	@BeforeEach
	public void setup() {
		out = new ByteArrayOutputStream();
		destination = new OutputStreamWriter(out);

		mrs = Mockito.mock(ResultSet.class);
		dgw = makeDgw();
		try {
			Mockito.when(mrs.getString("agency_cd")).thenReturn(dgw.agencyCode);
			Mockito.when(mrs.getString("agency_code")).thenReturn(dgw.agencyCode);
			Mockito.when(mrs.getString("approval_status_code")).thenReturn(dgw.approvalStatusCode);
			Mockito.when(mrs.getString("date_measured")).thenReturn(dgw.dateMeasured);
			Mockito.when(mrs.getTimestamp("date_measured_raw")).thenReturn(dgw.dateMeasuredRaw);
			Mockito.when(mrs.getString("date_time_accuracy_code")).thenReturn(dgw.dateTimeAccuracyCode);
			Mockito.when(mrs.getString("level_accuracy_code")).thenReturn(dgw.levelAccuracyCode);
			Mockito.when(mrs.getString("level_feet_above_vertical_datum")).thenReturn(dgw.levelFeetAboveVerticalDatum);
			Mockito.when(mrs.getString("level_feet_below_land_surface")).thenReturn(dgw.levelFeetBelowLandSurface);
			Mockito.when(mrs.getString("measurement_method_code")).thenReturn(dgw.measurementMethodCode);
			Mockito.when(mrs.getString("measurement_source_code")).thenReturn(dgw.measurementSourceCode);
			Mockito.when(mrs.getString("measuring_agency_code")).thenReturn(dgw.measuringAgencyCode);
			Mockito.when(mrs.getString("site_identification_number")).thenReturn(dgw.siteIdentificationNumber);
			Mockito.when(mrs.getString("site_status_code")).thenReturn(dgw.siteStatusCode);
			Mockito.when(mrs.getString("time_measured_utc")).thenReturn(dgw.timeMeasuredUtc);
			Mockito.when(mrs.getString("timezone_code")).thenReturn(dgw.timezoneCode);
			Mockito.when(mrs.getString("vertical_datum_code")).thenReturn(dgw.verticalDatumCode);
			Mockito.when(mrs.getString("parameter_code")).thenReturn(dgw.parameterCode);

		} catch (SQLException e) {
			throw new RuntimeException("Error mocking resultset", e);
		}
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

		dgw.parameterCode = "12345";

		return dgw;
	}

	@Test
	void testRowWriteGeneral() throws Exception {
		// SETUP
		RdbWriter rdbWriter = new RdbWriter(destination);
		DiscreteGroundWaterRowMapper rowHandler = new DiscreteGroundWaterRowMapper(rdbWriter);

		// ACTION UNDER TEST
		String written = rowHandler.mapRow(mrs, 1);

		// POST SETUP
		destination.close();
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
		assertFalse(writtenValue.contains("\t1200\t"));
		assertFalse(writtenValue.contains("\t1830\t"));
		assertTrue(writtenValue.contains("\t07-MAY-2007 18:30:47\t"));
		assertTrue(writtenValue.contains("\t01-MAY-2007 12:00:00\t")); // measured and UTC dates
		assertTrue(writtenValue.contains("\tUTC\t"));
		assertTrue(writtenValue.contains("\tD\t"));
		assertTrue(writtenValue.contains("\t12345")); // last value

		assertEquals(1, rdbWriter.getDataRows());
		assertEquals("written", written);
	}

	@Test
	void testSimpleDateFormat_1982_12_31_ERROR() {
		// SETUP
		LocalDateTime dateTime = LocalDateTime.of(1982, Month.DECEMBER, 31, 12, 0);
		dgw.dateMeasuredRaw = Timestamp.valueOf(dateTime);

		// ACTION UNDER TEST
		String actual = new SimpleDateFormat("YYYYMMdd").format(dgw.dateMeasuredRaw);

		// ASSERTIONS - these pass if there is a bug in SimpleDateFormat
		assertEquals("1982-12-31T12:00", dateTime.toString());
		assertEquals("19831231", actual, "Java lib error formats as 1983 but should be 1982");
		assertNotEquals("19821231", actual, "Java lib error formats as 1983 but should be 1982");
	}
	@Test
	void testDateTimeFormat_1982_12_31_ERROR() {
		// SETUP
		LocalDateTime dateTime = LocalDateTime.of(1982, Month.DECEMBER, 31, 12, 0);
		dgw.dateMeasuredRaw = Timestamp.valueOf(dateTime);
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
		System.out.println(jodaDT);
		org.joda.time.format.DateTimeFormatter testFormat = org.joda.time.format.DateTimeFormat.forPattern("YYYYMMdd");

		// ACTION UNDER TEST
		String actual = jodaDT.toString(testFormat);

		// ASSERTIONS - these pass if there is a bug in SimpleDateFormat
		//		assertEquals("1982-12-31T12:00", dateTime.toString());
		assertEquals("19821231", actual);
		assertNotEquals("19831231", actual);
	}
}
