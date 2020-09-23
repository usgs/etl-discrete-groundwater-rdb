package gov.usgs.wma.waterdata.groundwater;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class ParameterRowMapper implements RowMapper<Parameter> {

	/**
	 * Translates JDBC RowSet row to ORM instance.
	 */
	@Override
	public Parameter mapRow(ResultSet rs, int rowNum) throws SQLException {
		Parameter parameter = new Parameter();
		parameter.parameterCode = rs.getString("parm_cd");
		parameter.aboveDatum = rs.getBoolean("above_datum");
		parameter.belowLandSurface = rs.getBoolean("below_land_surface");
		return parameter;
	}
}
