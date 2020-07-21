package gov.usgs.wma.waterdata.groundwater;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.Writer;
import java.nio.file.Files;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;

class S3BucketTest {

	Properties properties;
	S3BucketUtil s3util;
	S3Bucket s3;
	String filename = "test-filename";

	@BeforeEach
	public void setup() {
		properties = new Properties();
		properties.setRegion("JUNIT-WEST");
		properties.setBucket("JUNIT-S3");
		properties.setTier("TEST"); // TEST, QA, and PROD-INTERNAL

		s3util = new S3BucketUtil(properties);
		s3 = s3util.openS3(filename);
	}

	@AfterEach
	public void cleanup() {
		if (s3 != null) {
			deleteFile(s3.file);
		}
	}
	protected void deleteFile(File file) {
		if (file != null) {
			file.delete();
		}
	}

	@Test
	void testOpenS3File() throws Exception {
		// ASSERTIONS
		assertNotNull(s3);
		assertEquals(properties.bucket, s3.bucket);
		assertEquals(properties.region, s3.region);
		assertEquals(filename, s3.keyName);
		assertNotNull(s3.file);
		String tmpPath = s3.file.getAbsolutePath().toLowerCase();
		assertTrue(tmpPath.contains("tmp") || tmpPath.contains("temp"));

		assertEquals(true, s3.disposeFile); // default state is to dispose of the tmp file
		assertNull(s3.writer); // initial state is without a writer
	}

	@Test
	void testDisposeFile_set() throws Exception {
		// SETUP

		// ACTION UNDER TEST
		s3.setDisposeFile(false);

		// ASSERTIONS
		assertNotNull(s3);
		assertFalse(s3.disposeFile);
	}

	@Test
	void testAccessingWritter() throws Exception {
		// SETUP
		String writeThis = "write something";

		// ACTION UNDER TEST
		Writer writer = s3.getWriter();
		writer.write(writeThis);
		writer.close();

		List<String> lines = Files.readAllLines(s3.file.toPath());

		// ASSERTIONS
		assertTrue(lines.contains(writeThis));
	}


	@Test
	void testCloseAndTransportToS3() throws Exception {
		// SETUP
		PutObjectResult mockResult = new PutObjectResult();

		S3Bucket mockS3bucket = new S3Bucket(properties.region, properties.bucket, filename,
				File.createTempFile(filename, "rdb")) {
			@Override
			protected AmazonS3 buildS3() {
				AmazonS3 mocks3 = Mockito.mock(AmazonS3.class);
				mockResult.setETag("MOCK-RAN");
				when(mocks3.putObject(bucket, keyName, file)).thenReturn(mockResult);
				//				putObject(bucket, keyName, file);
				return mocks3;
			}
		};

		try {
			// ACTION UNDER TEST
			Writer writer = mockS3bucket.getWriter();
			String writeThis = "write something";
			writer.write(writeThis);
			mockS3bucket.close();

			// ASSERTIONS
			assertFalse(mockS3bucket.file.exists());
			assertEquals("MOCK-RAN", mockResult.getETag());

		} finally {
			deleteFile(mockS3bucket.file);
		}
	}
}