package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

/**
 * AWS Entry point and orchestration of GW RDB file export.
 *
 * @author duselman
 */
public class BuildRdbFile implements Function<RequestObject, ResultObject> {

	private static final Logger LOG = LoggerFactory.getLogger(BuildRdbFile.class);

	@Autowired
	private S3BucketUtil s3BucketUtil;

	@Autowired
	private DiscreteGroundWaterDao dao;

	@Autowired
	private LocationFolder locationFolderUtil;




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
		return processRequest(locationFolder);
	}

	/**
	 * Orchestration of AWS lambda processing.
	 * Fleshes out the location folder into a list of states.
	 * Translates the location folder into a file decorator.
	 * Fetches the GW Data and writes it to an S3 Bucket file.
	 *
	 * @param request an AQTS location folder
	 * @return TBD (writes file to S3 Bucket)
	 */
	protected ResultObject processRequest(String locationFolder) {
		LOG.debug("the request object location folder: {}", locationFolder);
		ResultObject result = new ResultObject();

		List<String> states = LocationFolder.locationFolderToSates(locationFolder);

		String suffix = locationFolderUtil.locationFolderFilenameDecorator(locationFolder);
		if (StringUtils.isEmpty(suffix)) {
			throw new RuntimeException("Given location folder has no state entry: " + locationFolder);
		}
		// TODO need to know how to create filename metadata
		String filename = s3BucketUtil.createFilename(suffix, "meta");

		try (   S3Bucket s3bucket = s3BucketUtil.openS3(filename);
				Writer writer = s3bucket.getWriter();) {

			RdbWriter rdbWriter = new RdbWriter(writer).writeHeader();
			dao.sendDiscreteGroundWater(states, rdbWriter);

			result.setCount( (int)rdbWriter.getDataRows() );

		} catch (IOException e) {
			throw new RuntimeException("Error opening S3 file stream to file, " + filename, e);
		} catch (Exception e) {
			throw new RuntimeException("Error processing file to S3 related to file, " + filename, e);
		}

		// currently returning the rows count written to the file
		return result;
	}
}
