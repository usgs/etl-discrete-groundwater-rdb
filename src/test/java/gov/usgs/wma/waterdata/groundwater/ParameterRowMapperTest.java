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

class ParameterRowMapperTest {

	ByteArrayOutputStream out;
	Writer destination;
	ResultSet mockRs;
	Parameter p;

	@BeforeEach
	public void setup() {
		out = new ByteArrayOutputStream();
		destination = new OutputStreamWriter(out);
		mockRs = Mockito.mock(ResultSet.class);
		p = makeParameter();
		try {
			Mockito.when(mockRs.getString("parm_cd")).thenReturn(p.parameterCode);
			Mockito.when(mockRs.getBoolean("above_datum")).thenReturn(p.aboveDatum);
			Mockito.when(mockRs.getBoolean("below_land_surface")).thenReturn(p.belowLandSurface);
		} catch (SQLException e) {
			throw new RuntimeException("Error mocking resultset", e);
		}
	}

	Parameter makeParameter() {
		Parameter p = new Parameter();
		p.setParameterCode("30210");
		p.setAboveDatum(false);
		p.setBelowLandSurface(true);
		return p;
	}

	@Test
	void testResultSetMapping() throws SQLException {
		// SETUP
		ParameterRowMapper rowMapper = new ParameterRowMapper();

		// ACTION UNDER TEST
		Parameter actual = rowMapper.mapRow(mockRs, 0);

		// ASSERTIONS

		assertEquals(p.parameterCode, actual.parameterCode);
		assertEquals(p.aboveDatum, actual.aboveDatum);
		assertEquals(p.belowLandSurface, actual.belowLandSurface);

	}
}
