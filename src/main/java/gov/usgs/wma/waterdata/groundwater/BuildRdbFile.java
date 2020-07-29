package gov.usgs.wma.waterdata.groundwater;

import java.io.Writer;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * AWS Entry point and orchestration of GW RDB file export.
 *
 * @author duselman
 */
@Component
public class BuildRdbFile implements Function<RequestObject, ResultObject> {

	private static final Logger LOG = LoggerFactory.getLogger(BuildRdbFile.class);

	@Autowired
	protected S3BucketUtil s3BucketUtil;

	@Autowired
	protected DiscreteGroundWaterDao dao;

	@Autowired
	protected LocationFolder locationFolderUtil;

	@Autowired
	protected Properties properties;


	public BuildRdbFile() {
	}

	/**
	 * Entry point of AWS lambda processing.
	 *
	 * @param request an AQTS location folder
	 * @return TBD (writes file to S3 Bucket)
	 */
	@Override
	public  ResultObject apply(RequestObject request) {
		String locationFolder = request.getLocationFolder();
		if ("ALL".equals(locationFolder)) {
			return new InvokeAll().invoke(properties, locationFolderUtil.getFolders());
		}
		return processRequest(locationFolder);
	}

	/**
	 * Orchestration of AWS lambda processing.
	 * Fleshes out the location folder into a list of states.
	 * Translates the location folder into a file decorator.
	 * Fetches the GW Data and writes it to an S3 Bucket file.
	 *
	 * @param locationFolder an AQTS location folder
	 * @return result number of rows written to RDB file
	 */
	protected ResultObject processRequest(String locationFolder) {
		LOG.debug("the request object location folder: {}", locationFolder);
		ResultObject result = new ResultObject();

		List<String> states = locationFolderUtil.toStates(locationFolder);

		String suffix = locationFolderUtil.filenameDecorator(locationFolder);
		if (StringUtils.isEmpty(suffix)) {
			throw new RuntimeException("Given location folder has no state entry: " + locationFolder);
		}
		String filename = s3BucketUtil.createFilename(suffix);

		try (S3Bucket s3bucket = s3BucketUtil.openS3(filename);) {

			Writer writer = s3bucket.getWriter();
			RdbWriter rdbWriter = createRdbWriter(writer).writeHeader();
			dao.sendDiscreteGroundWater(states, rdbWriter);

			result.setCount( (int)rdbWriter.getDataRows() );
			result.setFilename(s3bucket.getKeyName());
		} catch (Exception e) {
			throw new RuntimeException("Error writing RDB file to S3, " + filename, e);
		}

		// currently returning the rows count written to the file
		return result;
	}

	protected RdbWriter createRdbWriter(Writer dest) {
		return new RdbWriter(dest);
	}
}
