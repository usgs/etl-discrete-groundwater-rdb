package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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

	@Override
	public  ResultObject apply(RequestObject request) {
		return processRequest(request);
	}

	protected ResultObject processRequest(RequestObject request) {
		String locationFolder = request.getLocationFolder();
		LOG.debug("the request object location folder: {}", locationFolder);
		ResultObject result = new ResultObject();

		List<String> states = LocationFolder.locationFolderToSates(locationFolder);
		List<DiscreteGroundWater> levels = dao.selectDiscreteGroundWater(states);

		String suffix = locationFolderUtil.locationFolderFilenameDecorator(locationFolder);
		String filename = s3bucket.createFilename("dev", suffix, "meta");
		// nwisca.gw_lev_01.06.20200715_030500.full.rdb
		try (Writer writer = s3bucket.makeFile(filename)) {

			RdbWriter rdbWriter = new RdbWriter(writer);
			rdbWriter.writeHeader();
			levels.stream().forEach(dgw -> rdbWriter.writeRow(dgw));

		} catch (IOException e) {
			throw new RuntimeException("Error opening S3 file stream.", e);
		}

		// TODO add something to the result object
		return result;
	}
}
