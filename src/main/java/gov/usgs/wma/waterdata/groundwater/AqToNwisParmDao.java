package gov.usgs.wma.waterdata.groundwater;

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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class AqToNwisParmDao {
	private static final Logger LOG = LoggerFactory.getLogger(AqToNwisParmDao.class);

	@Autowired
	@Qualifier("jdbcTemplateTransform")
	protected JdbcTemplate jdbcTemplate;

	@Value("classpath:sql/selectAqToNwisParm.sql")
	protected Resource selectQuery;

	public List<Parameter> getParameters() {
		List<Parameter> rtn = Arrays.asList();
		try {
			String sql = "select d.parm_cd, d.above_datum, d.below_land_surface from aq_to_nwis_parm d";
			//String sql = new String(FileCopyUtils.copyToByteArray(selectQuery.getInputStream()));
			rtn = jdbcTemplate.query(
					sql,
					new ParameterRowMapper()
			);
		} catch (EmptyResultDataAccessException e) {
			LOG.info("Couldn't find parameter data - {} ", e.getLocalizedMessage());
		} catch (Exception e) {
			LOG.error("Unable to get SQL statement", e);
			throw new RuntimeException(e);
		}
		return rtn;
	}
}
