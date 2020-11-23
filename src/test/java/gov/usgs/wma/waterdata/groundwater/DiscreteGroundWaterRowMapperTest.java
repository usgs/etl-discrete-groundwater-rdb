package gov.usgs.wma.waterdata.groundwater;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DiscreteGroundWaterRowMapperTest {

	ByteArrayOutputStream out;
	Writer destination;
	ResultSet mockRs;
	DiscreteGroundWater dgw;

	@BeforeEach
	public void setup() {
		out = new ByteArrayOutputStream();
		destination = new OutputStreamWriter(out);

		String resultQual = "[\"" + LevelStatusCode.FOREIGN.getAqDescription() + "\",\""
				                    + LevelStatusCode.BELOW.getAqDescription() + "\"]"; // <- This is the value that should be found

		mockRs = Mockito.mock(ResultSet.class);
		dgw = makeDgw();
		try {
			Mockito.when(mockRs.getString("agency_cd")).thenReturn(dgw.agencyCode);
			Mockito.when(mockRs.getString("agency_code")).thenReturn(dgw.agencyCode);
			Mockito.when(mockRs.getString("approval_level")).thenReturn(dgw.approvalLevel);
			Mockito.when(mockRs.getString("date_measured")).thenReturn(dgw.dateMeasured);
			Mockito.when(mockRs.getTimestamp("date_measured_raw")).thenReturn(dgw.dateMeasuredRaw);
			Mockito.when(mockRs.getString("date_time_accuracy_code")).thenReturn(dgw.dateTimeAccuracyCode);
			Mockito.when(mockRs.getString("level_accuracy_code")).thenReturn(dgw.levelAccuracyCode);
			Mockito.when(mockRs.getString("measurement_method_code")).thenReturn(dgw.measurementMethodCode);
			Mockito.when(mockRs.getString("measurement_source_code")).thenReturn(dgw.measurementSourceCode);
			Mockito.when(mockRs.getString("measuring_agency_code")).thenReturn(dgw.measuringAgencyCode);
			Mockito.when(mockRs.getString("site_identification_number")).thenReturn(dgw.siteIdentificationNumber);
			Mockito.when(mockRs.getString("result_measure_qualifiers")).thenReturn(resultQual);
			Mockito.when(mockRs.getString("time_measured_utc")).thenReturn(dgw.timeMeasuredUtc);
			Mockito.when(mockRs.getString("timezone_code")).thenReturn(dgw.timezoneCode);
			Mockito.when(mockRs.getString("vertical_datum_code")).thenReturn(dgw.verticalDatumCode);
			Mockito.when(mockRs.getString("parameter_code")).thenReturn(dgw.parameterCode);
			Mockito.when(mockRs.getString("display_result")).thenReturn(dgw.displayResult);
			

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
		dgw.readingQualifiers = LevelStatusCode.BELOW.getNwisCode();
		dgw.measurementMethodCode = "S"; // S, R, or V
		dgw.dateMeasured = "07-MAY-2007 18:30:47";
		LocalDateTime dateTime = LocalDateTime.of(2007, Month.MAY, 01, 12, 0);
		dgw.dateMeasuredRaw = Timestamp.valueOf(dateTime);
		dgw.dateTimeAccuracyCode = "D"; // [D]day or [M]minute
		dgw.timezoneCode = "UTC";
		dgw.timeMeasuredUtc = "01-MAY-2007 12:00:00"; // UTC
		dgw.approvalLevel = "900"; // Only 1200 is approved - all others are non-approved.

		dgw.parameterCode = "30210";

		return dgw;
	}

	@Test
	void testResultSetMapping() throws SQLException {
		// SETUP
		DiscreteGroundWaterRowMapper rowMapper = new DiscreteGroundWaterRowMapper();

		// ACTION UNDER TEST
		DiscreteGroundWater actual = rowMapper.mapRow(mockRs, 0);

		// ASSERTIONS
		assertEquals(dgw.agencyCode, actual.agencyCode);
		assertEquals(dgw.siteIdentificationNumber, actual.siteIdentificationNumber);
		assertEquals(dgw.verticalDatumCode, actual.verticalDatumCode); // only if vertical measurement
		assertEquals(dgw.measurementSourceCode, actual.measurementSourceCode);
		assertEquals(dgw.measuringAgencyCode, actual.measuringAgencyCode);
		assertEquals(dgw.levelAccuracyCode, actual.levelAccuracyCode); // two digits after decimal point
		assertEquals(dgw.readingQualifiers, actual.readingQualifiers); // Numeric or empty
		assertEquals(dgw.measurementMethodCode, actual.measurementMethodCode); // S, R, or V
		assertEquals(dgw.dateMeasured, actual.dateMeasured);
		assertEquals(dgw.dateMeasuredRaw, actual.dateMeasuredRaw);
		assertEquals(dgw.dateTimeAccuracyCode, actual.dateTimeAccuracyCode); // [D]day or [M]minute
		assertEquals(dgw.timezoneCode, actual.timezoneCode);
		assertEquals(dgw.timeMeasuredUtc, actual.timeMeasuredUtc); // UTC
		assertEquals("P", actual.approvalLevel); //The mapping is a business rule
		assertEquals(dgw.parameterCode, actual.parameterCode);
		assertEquals(dgw.displayResult, actual.displayResult);
		
	}
}
