package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
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
	@Qualifier("jdbcTemplateObservation")
	protected JdbcTemplate jdbcTemplate;

	@Value("classpath:sql/getDiscreteGroundWater.sql")
	protected Resource selectQuery;

	/**
	 * Fetches GW data from the database and converts it to a list of the ORM instance.
	 * @param states list of state names to fetch.
	 * 				  Most location folders are states names, some are a collection of a few states
	 *
	 * @return list of discrete ground water measurements
	 */
	public void sendDiscreteGroundWater(List<String> states, RdbWriter writer) {
		DiscreteGroundWaterRowMapper rowMapper = new DiscreteGroundWaterRowMapper(writer);

		try {
			String sql = new String(FileCopyUtils.copyToByteArray(selectQuery.getInputStream()));
			jdbcTemplate.query(sql, rowMapper, Collections.singletonMap("states", states));

		} catch (EmptyResultDataAccessException e) {
			LOG.info(e.getLocalizedMessage());
		} catch (IOException e) {
			LOG.error("Unable to get SQL statement", e);
			throw new RuntimeException(e);
		}
	}
}
