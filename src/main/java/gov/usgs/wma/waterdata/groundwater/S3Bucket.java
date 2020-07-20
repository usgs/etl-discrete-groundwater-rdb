package gov.usgs.wma.waterdata.groundwater;

import java.io.Writer;

/**
 * Manager class for S3 Bucket actions.
 * @author duselman
 */
public class S3Bucket {

	/**
	 * Simple helper method to construct an RDB filename.
	 * @param tier     deploy tier name. I.E. ts for test, qa for quality assurance, etc.
	 * @param suffix   a label for the current file, typically the postal code name for the location folder.
	 * @param metadata a date or data count number to identify this file from future files.
	 * @return a file name for an RDB file.
	 */
	public String createFilename(String tier, String suffix, String metadata) {
		return "ts" + "<tier>" + suffix + ".gw_lev_01.06." + metadata + ".full.rdb";
	}

	/**
	 * Constructs a file writer to an S3 Bucket from the supplied serverless configuration.
	 * @param filename unique name for the RDB file
	 * @return writer instance
	 */
	public Writer openFile(String filename) {
		// TODO Auto-generated method stub
		return null;
	}
}
