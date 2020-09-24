package gov.usgs.wma.waterdata.groundwater;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.RowCallbackHandler;

public class DiscreteGroundWaterRowHandler implements RowCallbackHandler {

	protected DiscreteGroundWaterRowMapper rowMap;
	protected RdbWriter writer;
	protected int rowNum;
	protected List<Parameter> parameters;

	public DiscreteGroundWaterRowHandler(RdbWriter writer, List<Parameter> parameters) {
		this.writer = writer;
		rowNum = 0;
		rowMap = new DiscreteGroundWaterRowMapper();
		this.parameters = parameters;

	}

	@Override
	public void processRow(ResultSet rs) throws SQLException {
		DiscreteGroundWater dgw = rowMap.mapRow(rs, ++rowNum);
		for (Parameter parameter: parameters) {
			if (parameter.parameterCode.equals(dgw.parameterCode)) {
				dgw.aboveDatum = parameter.aboveDatum;
				dgw.belowLandSurface = parameter.belowLandSurface;
			}
		}
		writer.writeRow(dgw);
	}
}
