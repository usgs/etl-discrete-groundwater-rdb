package gov.usgs.wma.waterdata.groundwater;


import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

/**
 * Simple SpringFramework RowMapper to GW ORM object
 * @author duselman
 */
public class DiscreteGroundWaterRowMapper implements RowMapper<String> {

	private RdbWriter writer;

	public DiscreteGroundWaterRowMapper(RdbWriter writer) {
		this.writer = writer;
	}

	/**
	 * Translates JDBC RowSet row to ORM instance.
	 */
	@Override
	public String mapRow(ResultSet rs, int rowNum) throws SQLException {
		DiscreteGroundWater discreteGroundWater = new DiscreteGroundWater();
		discreteGroundWater.agencyCode = rs.getString("agency_code");
		discreteGroundWater.approvalStatusCode = rs.getString("approval_status_code");
		discreteGroundWater.dateMeasured = rs.getString("date_measured");
		discreteGroundWater.dateMeasuredRaw = rs.getTimestamp("date_measured_raw");
		discreteGroundWater.dateTimeAccuracyCode = rs.getString("date_time_accuracy_code");
		discreteGroundWater.levelAccuracyCode = rs.getString("level_accuracy_code");
		discreteGroundWater.levelFeetAboveVerticalDatum = rs.getString("level_feet_above_vertical_datum");
		discreteGroundWater.levelFeetBelowLandSurface = rs.getString("level_feet_below_land_surface");
		discreteGroundWater.measurementMethodCode = rs.getString("measurement_method_code");
		discreteGroundWater.measurementSourceCode = rs.getString("measurement_source_code");
		discreteGroundWater.measuringAgencyCode = rs.getString("measuring_agency_code");
		discreteGroundWater.siteIdentificationNumber = rs.getString("site_identification_number");
		discreteGroundWater.siteStatusCode = rs.getString("site_status_code");
		discreteGroundWater.timeMeasuredUtc = rs.getString("time_measured_utc");
		discreteGroundWater.timezoneCode = rs.getString("timezone_code");
		discreteGroundWater.verticalDatumCode = rs.getString("vertical_datum_code");
		discreteGroundWater.parameterCode = rs.getString("parameter_code");

		writer.writeRow(discreteGroundWater);
		return "written";
	}
}
