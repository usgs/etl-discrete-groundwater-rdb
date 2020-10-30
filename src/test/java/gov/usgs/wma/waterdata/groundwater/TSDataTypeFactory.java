package gov.usgs.wma.waterdata.groundwater;

import org.dbunit.dataset.datatype.DataType;
import org.dbunit.dataset.datatype.DataTypeException;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Types;

public class TSDataTypeFactory extends PostgresqlDataTypeFactory {
	private static final Logger logger = LoggerFactory.getLogger(TSDataTypeFactory.class);

	@Override
	public DataType createDataType(int sqlType, String sqlTypeName) throws DataTypeException {
		logger.debug("createDataType(sqlType={}, sqlTypeName={})",
				String.valueOf(sqlType), sqlTypeName);

		if (sqlType == Types.OTHER && ("json".equals(sqlTypeName) || "jsonb".equals(sqlTypeName))) {
			return new JsonType(); // support PostgreSQL json/jsonb
		} else {
			return super.createDataType(sqlType, sqlTypeName);
		}

	}
}
