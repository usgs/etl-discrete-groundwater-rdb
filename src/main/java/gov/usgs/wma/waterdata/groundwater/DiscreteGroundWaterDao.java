package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

/**
 * Ground water observation data access
 *
 * @author duselman
 */
@Component
public class DiscreteGroundWaterDao {
	private static final Logger LOG = LoggerFactory.getLogger(DiscreteGroundWaterDao.class);

	@Autowired
	@Qualifier("jdbcTemplate")
	protected JdbcTemplate jdbcTemplate;

	@Value("classpath:sql/selectDiscreteGroundWater.sql")
	protected Resource selectQuery;

	/**
	 * Fetches GW data from the database and converts it to a list of the ORM instance.
	 * @param states list of state names to fetch.
	 * 				 Most location folders are states names, some are a collection of a few states
	 * @param writer instance that will write each row to an RDB file
	 */
	public void sendDiscreteGroundWater(List<String> states, RdbWriter writer) {
		DiscreteGroundWaterRowMapper rowMapper = new DiscreteGroundWaterRowMapper(writer);

		try {
			String sql = new String(FileCopyUtils.copyToByteArray(selectQuery.getInputStream()));
			NamedParameterJdbcTemplate namedParamJdbcTemplate = new NamedParameterJdbcTemplate(jdbcTemplate);
			Map<String, List<String>> params = Collections.singletonMap("states", states);

			namedParamJdbcTemplate.query(sql, params, rowMapper);
		} catch (IOException e) {
			LOG.error("Unable to get SQL statement", e);
			throw new RuntimeException(e);
		}
	}
}
