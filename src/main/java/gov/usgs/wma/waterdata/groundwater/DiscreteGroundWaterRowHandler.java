package gov.usgs.wma.waterdata.groundwater;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowCallbackHandler;

public class DiscreteGroundWaterRowHandler implements RowCallbackHandler {

	protected DiscreteGroundWaterRowMapper rowMap;
	protected RdbWriter writer;
	protected int rowNum;

	public DiscreteGroundWaterRowHandler(RdbWriter writer) {
		this.writer = writer;
		rowNum = 0;
		rowMap = new DiscreteGroundWaterRowMapper();
	}

	@Override
	public void processRow(ResultSet rs) throws SQLException {
		DiscreteGroundWater dgw = rowMap.mapRow(rs, ++rowNum);
		writer.writeRow(dgw);
	}
}
