package gov.usgs.wma.waterdata.groundwater;

import javax.sql.DataSource;

import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class DbConfig {

	@Bean
	@ConfigurationProperties(prefix="spring.datasource-transform")
	@Primary
	@Profile("default")
	public DataSourceProperties dataSourcePropertiesTransform() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties(prefix="spring.datasource-transform")
	@Primary
	public DataSource dataSourceTransform() {
		return dataSourcePropertiesTransform().initializeDataSourceBuilder().build();
	}

	@Bean
	@Primary
	public JdbcTemplate jdbcTemplateTransform() {
		return new JdbcTemplate(dataSourceTransform());
	}

	@Bean
	@ConfigurationProperties(prefix="spring.datasource-observation")
	public DataSourceProperties dataSourceObservationProperties() {
		return new DataSourceProperties();
	}

	@Bean
	@ConfigurationProperties(prefix="spring.datasource-observation")
	public DataSource dataSourceObservation() {
		return dataSourceObservationProperties().initializeDataSourceBuilder().build();
	}

	@Bean
	public JdbcTemplate jdbcTemplateObservation() {
		return new JdbcTemplate(dataSourceObservation());
	}
}