package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DiscreteGroundWaterRowHandlerTest {

	ByteArrayOutputStream out;
	Writer destination;
	ResultSet mockRs;
	DiscreteGroundWater dgw;

	@BeforeEach
	public void setup() {
		out = new ByteArrayOutputStream();
		destination = new OutputStreamWriter(out);

		mockRs = Mockito.mock(ResultSet.class);
		dgw = makeDgw();
		try {
			Mockito.when(mockRs.getString("agency_cd")).thenReturn(dgw.agencyCode);
			Mockito.when(mockRs.getString("agency_code")).thenReturn(dgw.agencyCode);
			Mockito.when(mockRs.getString("approval_status_code")).thenReturn(dgw.approvalStatusCode);
			Mockito.when(mockRs.getString("date_measured")).thenReturn(dgw.dateMeasured);
			Mockito.when(mockRs.getTimestamp("date_measured_raw")).thenReturn(dgw.dateMeasuredRaw);
			Mockito.when(mockRs.getString("date_time_accuracy_code")).thenReturn(dgw.dateTimeAccuracyCode);
			Mockito.when(mockRs.getString("level_accuracy_code")).thenReturn(dgw.levelAccuracyCode);
			Mockito.when(mockRs.getString("measurement_method_code")).thenReturn(dgw.measurementMethodCode);
			Mockito.when(mockRs.getString("measurement_source_code")).thenReturn(dgw.measurementSourceCode);
			Mockito.when(mockRs.getString("measuring_agency_code")).thenReturn(dgw.measuringAgencyCode);
			Mockito.when(mockRs.getString("site_identification_number")).thenReturn(dgw.siteIdentificationNumber);
			Mockito.when(mockRs.getString("result_measure_qualifiers")).thenReturn(dgw.readingQualifiers);
			Mockito.when(mockRs.getString("time_measured_utc")).thenReturn(dgw.timeMeasuredUtc);
			Mockito.when(mockRs.getString("timezone_code")).thenReturn(dgw.timezoneCode);
			Mockito.when(mockRs.getString("vertical_datum_code")).thenReturn(dgw.verticalDatumCode);
			Mockito.when(mockRs.getString("parameter_code")).thenReturn(dgw.parameterCode);

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
		dgw.displayResult = "23.06";
		// entry code [S]see or [L]land
		dgw.verticalDatumCode = ""; // only if vertical measurement
		dgw.measurementSourceCode = "";
		dgw.measuringAgencyCode = "USGS";
		dgw.levelAccuracyCode = "2"; // two digits after decimal point
		dgw.readingQualifiers = ""; //Numeric code
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
	void testRowWriteGeneral() throws Exception {
		// SETUP
		RdbWriter rdbWriter = new RdbWriter(destination);
		List<Parameter> parameters = new ArrayList<>();
		Parameter p1 = new Parameter();
		p1.setParameterCode("30210");
		p1.setAboveDatum(false);
		p1.setBelowLandSurface(true);
		parameters.add(p1);
		DiscreteGroundWaterRowHandler rowHandler = new DiscreteGroundWaterRowHandler(rdbWriter, parameters);

		// ACTION UNDER TEST
		rowHandler.processRow(mockRs);

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
		assertTrue(writtenValue.contains("\t1200\t"));
		assertFalse(writtenValue.contains("\t1830\t"));
		assertTrue(writtenValue.contains("\t07-MAY-2007 18:30:47\t"));
		assertTrue(writtenValue.contains("\t01-MAY-2007 12:00:00\t")); // measured and UTC dates
		assertTrue(writtenValue.contains("\tUTC\t"));
		assertTrue(writtenValue.contains("\tD\t"));
		assertTrue(writtenValue.contains("\t30210")); // last value

		assertEquals(1, rdbWriter.getDataRowCount());
	}
}
