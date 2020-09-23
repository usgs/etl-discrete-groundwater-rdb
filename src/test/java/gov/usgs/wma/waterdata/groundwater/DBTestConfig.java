package gov.usgs.wma.waterdata.groundwater;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import org.springframework.jdbc.core.JdbcTemplate;

@TestConfiguration
public class DBTestConfig {

	// Transform DB connection
	@Value("${TRANSFORM_SCHEMA_NAME}")
	private String transformSchemaName;

	@Bean
	@Primary
	@ConfigurationProperties(prefix="spring.datasource-transform")
	public DataSourceProperties dataSourcePropertiesTransform() {
		return new DataSourceProperties();
	}

	@Bean
	@Primary
	@ConfigurationProperties(prefix="spring.datasource-transform")
	public DataSource dataSourceTransform() {
		return dataSourcePropertiesTransform().initializeDataSourceBuilder().build();
	}

	@Bean
	@Primary
	public JdbcTemplate jdbcTemplateTransform() {
		return new JdbcTemplate(dataSourceTransform());
	}

	// Observation DB connection
	@Value("${OBSERVATION_SCHEMA_NAME}")
	private String observationSchemaName;

	@Bean
	@ConfigurationProperties(prefix="spring.datasource-observation")
	public DataSourceProperties dataSourcePropertiesObservation() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties(prefix="spring.datasource-observation")
	public DataSource dataSourceObservation() {
		return dataSourcePropertiesObservation().initializeDataSourceBuilder().build();
	}

	@Bean
	public JdbcTemplate jdbcTemplateObservation() {
		return new JdbcTemplate(dataSourceObservation());
	}

	@Bean
	public DatabaseConfigBean dbUnitDatabaseConfig() {
		DatabaseConfigBean dbUnitDbConfig = new DatabaseConfigBean();
		dbUnitDbConfig.setDatatypeFactory(new TSDataTypeFactory());
		dbUnitDbConfig.setAllowEmptyFields(true);
		dbUnitDbConfig.setTableType(new String[] {"PARTITIONED TABLE", "TABLE"});
		return dbUnitDbConfig;
	}

	@Bean
	public DatabaseDataSourceConnectionFactoryBean transform() throws SQLException {
		DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection = new DatabaseDataSourceConnectionFactoryBean();
		dbUnitDatabaseConnection.setDatabaseConfig(dbUnitDatabaseConfig());
		dbUnitDatabaseConnection.setDataSource(dataSourceTransform());
		dbUnitDatabaseConnection.setSchema(transformSchemaName);
		return dbUnitDatabaseConnection;
	}

	@Bean
	public DatabaseDataSourceConnectionFactoryBean observation() throws SQLException {
		DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection = new DatabaseDataSourceConnectionFactoryBean();
		dbUnitDatabaseConnection.setDatabaseConfig(dbUnitDatabaseConfig());
		dbUnitDatabaseConnection.setDataSource(dataSourceObservation());
		dbUnitDatabaseConnection.setSchema(observationSchemaName);
		return dbUnitDatabaseConnection;
	}
}
