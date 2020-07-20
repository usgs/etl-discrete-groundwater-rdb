package gov.usgs.wma.waterdata.groundwater;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

/**
 * Manager class for S3 Bucket actions.
 * @author duselman
 */
public class S3Bucket implements AutoCloseable {

	private String region;
	private String bucket;
	private String keyName;
	private File file;
	private FileWriter writer;
	private boolean preserveFile = false;

	S3Bucket(String region, String bucket, String keyName, File file) {
		this.region = region;
		this.bucket = bucket;
		this.keyName = keyName;
		this.file = file;
	}

	@Override
	public void close() throws Exception {
		try {
			writer.close();
			AmazonS3 s3 = AmazonS3ClientBuilder.standard().withRegion(region).build();
			s3.putObject(bucket, keyName, file);
		} finally {
			if (!preserveFile) {
				file.delete();
			}
		}
	}

	public void setPreserveFile(boolean preserveFile) {
		this.preserveFile = preserveFile;
	}

	public Writer getWriter() {
		try {
			writer = new FileWriter(file);
			return writer;
		} catch (IOException ioe) {
			throw new RuntimeException("Cannot open temp file from the current runtime env.");
		}
	}
}
