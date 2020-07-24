package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BuildRdbFileTest {

	final String STATE  = "Wisconsin";
	final String POSTCD = "WI";
	final String POSTCD_BAD = "";
	final String FILENM = "mock-file-name";
	RdbWriter writer;
	OutputStream out;
	Writer dest;
	RequestObject req;
	List<String> stateAsList;

	@BeforeEach
	public void beforeEach() {
		req = new RequestObject();
		req.locationFolder = STATE;
		stateAsList = List.of(STATE);
		out = new ByteArrayOutputStream();
		dest = new OutputStreamWriter(out);
		writer = new RdbWriter(dest) {
			@Override
			public long getDataRows() {
				return 6;
			}
		};
	}

	@Test
	void testHappyPath() {
		// SETUP

		S3Bucket mockS3b = Mockito.mock(S3Bucket.class);
		Mockito.when(mockS3b.getWriter()).thenReturn(dest);

		S3BucketUtil mockS3 = Mockito.mock(S3BucketUtil.class);
		Mockito.when(mockS3.createFilename(POSTCD)).thenReturn(FILENM);
		Mockito.when(mockS3.openS3(FILENM)).thenReturn(mockS3b);

		DiscreteGroundWaterDao mockDao = Mockito.mock(DiscreteGroundWaterDao.class);

		LocationFolder mockLoc = Mockito.mock(LocationFolder.class);
		Mockito.when(mockLoc.toStates(STATE)).thenReturn(stateAsList);
		Mockito.when(mockLoc.filenameDecorator(STATE)).thenReturn(POSTCD);

		BuildRdbFile builder = new BuildRdbFile() {
			@Override
			protected RdbWriter createRdbWriter(Writer dest) {
				if (dest != writer.rdb) {
					throw new RuntimeException("For the unit test to be correctly setup the writers must be the same.");
				}
				return writer;
			}
		};
		builder.dao = mockDao;
		builder.s3BucketUtil = mockS3;
		builder.locationFolderUtil = mockLoc;

		// ACTION UNDER TEST
		ResultObject res = builder.apply(req);

		// ASSERTIONS
		assertEquals(4, writer.getHeaderRows(), "The header should be written in the RDB file builder.");
		assertEquals(6, res.getCount(), "The result object should contain the number of rows written.");
		Mockito.verify(mockDao, Mockito.atLeastOnce()).sendDiscreteGroundWater(stateAsList, writer);
		Mockito.verify(mockS3b, Mockito.atLeastOnce()).getWriter();
		Mockito.verify(mockS3,  Mockito.atLeastOnce()).createFilename(POSTCD);
		Mockito.verify(mockS3,  Mockito.atLeastOnce()).openS3(FILENM);
		Mockito.verify(mockLoc, Mockito.atLeastOnce()).toStates(STATE);
		Mockito.verify(mockLoc, Mockito.atLeastOnce()).filenameDecorator(STATE);
	}

	@Test
	void testBadLocationFolder() {
		// SETUP

		S3Bucket mockS3b = Mockito.mock(S3Bucket.class);
		Mockito.when(mockS3b.getWriter()).thenReturn(dest);

		S3BucketUtil mockS3 = Mockito.mock(S3BucketUtil.class);
		Mockito.when(mockS3.createFilename(POSTCD)).thenReturn(FILENM);
		Mockito.when(mockS3.openS3(FILENM)).thenReturn(mockS3b);

		DiscreteGroundWaterDao mockDao = Mockito.mock(DiscreteGroundWaterDao.class);

		LocationFolder mockLoc = Mockito.mock(LocationFolder.class);
		Mockito.when(mockLoc.toStates(STATE)).thenReturn(stateAsList);
		Mockito.when(mockLoc.filenameDecorator(STATE)).thenReturn(POSTCD_BAD);

		BuildRdbFile builder = new BuildRdbFile() {
			@Override
			protected RdbWriter createRdbWriter(Writer dest) {
				if (dest != writer.rdb) {
					throw new RuntimeException("For the unit test to be correctly setup the writers must be the same.");
				}
				return writer;
			}
		};
		builder.dao = mockDao;
		builder.s3BucketUtil = mockS3;
		builder.locationFolderUtil = mockLoc;

		// ACTION UNDER TEST
		assertThrows(RuntimeException.class, ()->builder.apply(req) );

		// ASSERTIONS
		assertEquals(0, writer.getHeaderRows(), "The header should NOT be written in the RDB file builder for bad location folder.");
		Mockito.verify(mockDao, Mockito.never()).sendDiscreteGroundWater(stateAsList, writer);
		Mockito.verify(mockS3b, Mockito.never()).getWriter();
		Mockito.verify(mockS3,  Mockito.never()).createFilename(POSTCD);
		Mockito.verify(mockS3,  Mockito.never()).openS3(FILENM);
		Mockito.verify(mockLoc, Mockito.atLeastOnce()).toStates(STATE);
		Mockito.verify(mockLoc, Mockito.atLeastOnce()).filenameDecorator(STATE);
	}

	@Test
	void testIOException() {
		// SETUP

		S3Bucket mockS3b = Mockito.mock(S3Bucket.class);
		Mockito.when(mockS3b.getWriter()).thenReturn(dest);

		S3BucketUtil mockS3 = Mockito.mock(S3BucketUtil.class);
		Mockito.when(mockS3.createFilename(POSTCD)).thenReturn(FILENM);
		Mockito.when(mockS3.openS3(FILENM)).thenReturn(mockS3b);

		DiscreteGroundWaterDao mockDao = Mockito.mock(DiscreteGroundWaterDao.class);

		LocationFolder mockLoc = Mockito.mock(LocationFolder.class);
		Mockito.when(mockLoc.toStates(STATE)).thenReturn(stateAsList);
		Mockito.when(mockLoc.filenameDecorator(STATE)).thenReturn(POSTCD);

		BuildRdbFile builder = new BuildRdbFile() {
			@Override
			protected RdbWriter createRdbWriter(Writer dest) {
				RdbWriter writer = Mockito.mock(RdbWriter.class);
				Mockito.when(writer.writeHeader()).thenThrow(new IOException("Unit test IOE"));
				return writer;
			}
		};
		builder.dao = mockDao;
		builder.s3BucketUtil = mockS3;
		builder.locationFolderUtil = mockLoc;

		// ACTION UNDER TEST
		assertThrows(RuntimeException.class, ()->builder.apply(req), "IOE converted to Runtime");

		// ASSERTIONS
		assertEquals(0, writer.getHeaderRows(), "The header should NOT be written in the RDB file builder for bad location folder.");
		Mockito.verify(mockLoc, Mockito.atLeastOnce()).toStates(STATE);
		Mockito.verify(mockLoc, Mockito.atLeastOnce()).filenameDecorator(STATE);
		Mockito.verify(mockS3b, Mockito.atLeastOnce()).getWriter();
		Mockito.verify(mockS3,  Mockito.atLeastOnce()).createFilename(POSTCD);
		Mockito.verify(mockS3,  Mockito.atLeastOnce()).openS3(FILENM);
		Mockito.verify(mockDao, Mockito.never()).sendDiscreteGroundWater(stateAsList, writer);
	}
}
