package gov.usgs.wma.waterdata.groundwater;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Manager class for S3 Bucket actions.
 * @author duselman
 */
@Component
public class S3BucketUtil {

	@Autowired
	private Properties properties;

	S3BucketUtil(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Simple helper method to construct an RDB filename.
	 * Sample file name: nwisca.gw_lev_01.06.20200715_030500.full.rdb
	 *
	 * @param tier     deploy tier name. I.E. ts for test, qa for quality assurance, etc.
	 * @param suffix   a label for the current file, typically the postal code name for the location folder.
	 * @param metadata a date or data count number to identify this file from future files.
	 * @return a file name for an RDB file.
	 */
	public String createFilename(String suffix, String metadata) {
		String tier = "test";
		if ("QA".equals(properties.getTier())) {
			tier = "qa";
		} else if (properties.getTier().toLowerCase().startsWith("prod")) {
			tier = "pr";
		}

		// .rdb added in temp file create
		return "ts" + tier +"."+ suffix.toUpperCase() +".gw_lev_01.06."+ metadata +".full";
	}

	/**
	 * Constructs a file writer to an S3 Bucket from the supplied serverless configuration.
	 * @param filename unique name for the RDB file
	 * @return writer instance
	 */
	public S3Bucket openS3(String filename) {
		try {
			File tempFile = File.createTempFile(filename, ".rdb");
			return new S3Bucket(properties.getRegion(), properties.getBucket(), filename, tempFile);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open temp file from the current runtime env.");
		}
	}
}
