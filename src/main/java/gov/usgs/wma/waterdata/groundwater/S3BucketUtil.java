package gov.usgs.wma.waterdata.groundwater;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Manager class for S3 Bucket actions.
 * @author duselman
 */
@Component
public class S3BucketUtil {

	@Autowired
	private final Properties properties;

	S3BucketUtil(Properties properties) {
		this.properties = properties;
	}

	/**
	 * Simple helper method to construct an RDB filename.
	 * Sample file name: nwisca.gw_lev_01.06.20200715_030500.full.rdb
	 *
	 * @param suffix a label for the current file, typically the postal code name for the location folder.
	 * @return a file name for an RDB file.
	 */
	public String createFilename(String suffix) {
		String tier = "test";
		if ("QA".equals(properties.getTier())) {
			tier = "qa";
		} else if (properties.getTier().toLowerCase().startsWith("prod")) {
			tier = "pr";
		}
		String metadata = new SimpleDateFormat("YYYYMMdd_HHmmss")
				.format(Timestamp.valueOf(LocalDateTime.now()));

		// .gz added in temp file create
		// ts is not joined with a dot while all the others are joined by a dot
		return "ts" + String.join(".", tier, suffix.toUpperCase(), "gw_lev_01.06", metadata, "full.rdb");
	}

	/**
	 * Constructs a file writer to an S3 Bucket from the supplied serverless configuration.
	 * @param filename unique name for the RDB file
	 * @return writer instance
	 */
	public S3Bucket openS3(String filename) {
		try {
			File tempFile = File.createTempFile(filename, ".gz");
			return new S3Bucket(properties.getRegion(), properties.getBucket(), filename+".gz", tempFile);
		} catch (IOException e) {
			throw new RuntimeException("Cannot open temp file from the current runtime env.");
		}
	}
}
