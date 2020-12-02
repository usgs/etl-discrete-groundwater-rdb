package gov.usgs.wma.waterdata.groundwater;

import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Simple SpringFramework RowMapper to GW ORM object
 * @author duselman
 */
public class DiscreteGroundWaterRowMapper implements RowMapper<DiscreteGroundWater> {

	DiscreteGroundWaterRules rules = new DiscreteGroundWaterRules();

	/**
	 * Translates JDBC RowSet row to ORM instance.
	 */
	@Override
	public DiscreteGroundWater mapRow(ResultSet rs, int rowNum) throws SQLException {
		DiscreteGroundWater discreteGroundWater = new DiscreteGroundWater();
		discreteGroundWater.agencyCode = rs.getString("agency_code");
		discreteGroundWater.approvalLevel = rs.getString("approval_level");
		discreteGroundWater.dateMeasured = rs.getString("date_measured");
		discreteGroundWater.dateMeasuredRaw = rs.getTimestamp("date_measured_raw");
		discreteGroundWater.dateTimeAccuracyCode = rs.getString("date_time_accuracy_code");
		discreteGroundWater.levelAccuracyCode = rs.getString("level_accuracy_code");
		discreteGroundWater.measurementMethodCode = rs.getString("measurement_method_code");
		discreteGroundWater.measuringAgencyCode = rs.getString("measuring_agency_code");
		discreteGroundWater.siteIdentificationNumber = rs.getString("site_identification_number");
		discreteGroundWater.readingQualifiers = rs.getString("result_measure_qualifiers");
		discreteGroundWater.timeMeasuredUtc = rs.getString("time_measured_utc");
		discreteGroundWater.timezoneCode = rs.getString("timezone_code");
		discreteGroundWater.verticalDatumCode = rs.getString("vertical_datum_code");
		discreteGroundWater.parameterCode = rs.getString("parameter_code");
		discreteGroundWater.displayResult = rs.getString("display_result");

		rules.apply(discreteGroundWater);

		return discreteGroundWater;
	}
}
