package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

/**
 * Simple data access object for state postal abbreviations.
 * @author duselman
 */
@Component
public class StatePostCodeDao {
	private static final Logger LOG = LoggerFactory.getLogger(StatePostCodeDao.class);

	@Autowired
	@Qualifier("jdbcTemplate")
	protected JdbcTemplate jdbcTemplate;

	@Value("classpath:sql/selectStatePostCode.sql")
	protected Resource selectQuery;

	/**
	 * Fetches the postal code of the given USA state name.
	 * Most location folders are states names, some are a collection of a few states
	 * @param state   full name of a USA state
	 * @return postal code for the given state
	 */
	public String getPostCode(String state) {
		try {
			String sql = new String(FileCopyUtils.copyToByteArray(selectQuery.getInputStream()));
			RowMapper<String> rowMapper = new RowMapper<String>() {
				@Override
				public String mapRow(ResultSet rs, int rowNum) throws SQLException {
					return rs.getString("state_post_cd");
				}

			};
			List<String> rows = jdbcTemplate.query(sql, rowMapper, state);
			if (rows.isEmpty()) {
				return "";
			}
			return rows.get(0);

		} catch (IOException e) {
			LOG.error("Unable to get SQL statement", e);
			throw new RuntimeException(e);
		}
	}
}
