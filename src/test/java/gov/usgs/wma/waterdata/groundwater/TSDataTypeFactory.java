package gov.usgs.wma.waterdata.groundwater;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TSDataTypeFactory extends PostgresqlDataTypeFactory {
	private static final Logger logger = LoggerFactory.getLogger(TSDataTypeFactory.class);

	@Override
	public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
		logger.debug("createDataType(sqlType={}, sqlTypeName={})",
                sqlType, sqlTypeName);

		return super.createDataType(sqlType, sqlTypeName);
	}
}
