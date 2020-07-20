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
	private final DiscreteGroundWaterDao dao;
	private final LocationFolder locationFolderUtil;
	private final S3Bucket s3bucket;


	@Autowired
	public BuildRdbFile(DiscreteGroundWaterDao dao, LocationFolder locationFolder, S3Bucket s3bucket) {
		this.dao = dao;
		this.locationFolderUtil = locationFolder;
		this.s3bucket = s3bucket;
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

		// nwisca.gw_lev_01.06.20200715_030500.full.rdb
		String filename = s3bucket.createFilename("dev", suffix, "meta");

		try (Writer writer = s3bucket.openFile(filename)) {

			dao.sendDiscreteGroundWater(states, new RdbWriter(writer));

		} catch (IOException e) {
			throw new RuntimeException("Error opening S3 file stream.", e);
		}

		// TODO add something to the result object
		return result;
	}
}
