package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
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

@Component
public class DiscreteGroundWaterDao {
	private static final Logger LOG = LoggerFactory.getLogger(DiscreteGroundWaterDao.class);

	@Autowired
	@Qualifier("jdbcTemplateObservation")
	protected JdbcTemplate jdbcTemplate;

	@Value("classpath:sql/getDiscreteGroundWater.sql")
	protected Resource selectQuery;

	// Most location folders are states names, some are a collection of a few states
	public List<DiscreteGroundWater> selectDiscreteGroundWater(List<String> states) {
		try {
			String sql = new String(FileCopyUtils.copyToByteArray(selectQuery.getInputStream()));
			List<DiscreteGroundWater> rows = jdbcTemplate.query(sql,
					new DiscreteGroundWaterRowMapper(), Collections.singletonMap("states", states));
			return rows;

		} catch (EmptyResultDataAccessException e) {
			LOG.info(e.getLocalizedMessage());
		} catch (IOException e) {
			LOG.error("Unable to get SQL statement", e);
			throw new RuntimeException(e);
		}
		return new LinkedList<>();
	}
}
