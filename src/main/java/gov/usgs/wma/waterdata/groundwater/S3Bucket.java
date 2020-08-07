package gov.usgs.wma.waterdata.groundwater;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.zip.GZIPOutputStream;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.PutObjectResult;

/**
 * Manager class for S3 Bucket actions.
 * @author duselman
 */
public class S3Bucket implements AutoCloseable {

	protected String region;
	protected String bucket;
	protected String keyName;
	protected File file;
	protected Writer writer;

	S3Bucket(String region, String bucket, String keyName, File file) {
		this.region = region;
		this.bucket = bucket;
		this.keyName = keyName;
		this.file = file;
	}

	public String getKeyName() {
		return keyName;
	}

	@Override
	public void close() throws Exception {
		try {

			if (writer != null) {
				writer.close();
				sendS3();
			} //else there is no content
		} finally {
			if (file != null) {
				file.delete();
			}
		}
	}

	protected AmazonS3 buildS3() {
		return AmazonS3ClientBuilder.standard().withRegion(region).build();
	}
	protected PutObjectResult sendS3() {
		AmazonS3 s3 = buildS3();
		return s3.putObject(bucket, keyName, file);

		// this is not a stream, it is all in memory
		// s3.putObject(bucket, keyName, "content");
	}

	public Writer getWriter() {
		try {
			FileOutputStream fos = new FileOutputStream(file);
			GZIPOutputStream gzo = new GZIPOutputStream(fos);
			writer = new OutputStreamWriter(gzo);
			return writer;
		} catch (IOException ioe) {
			throw new RuntimeException("Cannot open temp file from the current runtime env.");
		}
	}
}
