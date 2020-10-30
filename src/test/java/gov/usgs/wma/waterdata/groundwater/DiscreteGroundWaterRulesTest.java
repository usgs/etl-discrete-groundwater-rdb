package gov.usgs.wma.waterdata.groundwater;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DiscreteGroundWaterRulesTest {

	DiscreteGroundWater dgw;
	DiscreteGroundWaterRules rules = new DiscreteGroundWaterRules();

	@BeforeEach
	public void beforeEach() {
		dgw = new DiscreteGroundWater();
		dgw.readingQualifiers = "[\"" + LevelStatusCode.ABOVE.getAqDescription() + "\"]";
	}


	@Test
	void singleRecognizedValueIsMapped() throws Exception {
		rules.apply(dgw);
		assertEquals(LevelStatusCode.ABOVE.getNwisCode(), dgw.readingQualifiers);
	}

	@Test
	void multipleRecognizedValuesMapsFirstOneInEnumOrder() throws Exception {

		dgw.readingQualifiers = "["
				                        + "\"" + LevelStatusCode.SALINE.getAqDescription() + "\","
										+ "\"" +  LevelStatusCode.FOREIGN.getAqDescription() + "\","
				                        + "\"" +  LevelStatusCode.ABOVE.getAqDescription() + "\"," //This is the first in the enum
				                        + "\"" +  LevelStatusCode.TIDE_AFFECTED.getAqDescription() + "\""
				                        + "]";

		rules.apply(dgw);
		assertEquals(LevelStatusCode.ABOVE.getNwisCode(), dgw.readingQualifiers);
	}

	@Test
	void multipleRecognizedValuesMapsFirstOneInEnumOrderAndIgnoresJunk() throws Exception {

		dgw.readingQualifiers = "["
				                        + "\"JUNK junk ;lkj;lkjakhsd\","
				                        + "\"" +  LevelStatusCode.FOREIGN.getAqDescription() + "\","
				                        + "\"" +  LevelStatusCode.ABOVE.getAqDescription() + "\"," //This is the first in the enum
				                        + "\"" +  LevelStatusCode.TIDE_AFFECTED.getAqDescription() + "\","
				                        + "\"JUNK junk ;lkj;lkjakhsd\""
				                        + "]";

		rules.apply(dgw);
		assertEquals(LevelStatusCode.ABOVE.getNwisCode(), dgw.readingQualifiers);
	}

	@Test
	void noMatchedValuesResultsInEmpty() throws Exception {

		dgw.readingQualifiers = "["
				                        + "\"JUNK junk ;lkj;lkjakhsd\","
				                        + "\"JUNK junk ;lkj;lkjakhsd\""
				                        + "]";

		rules.apply(dgw);
		assertEquals("", dgw.readingQualifiers);
	}

	@Test
	void EmptyStringResultsInEmpty() throws Exception {

		dgw.readingQualifiers = "";

		rules.apply(dgw);
		assertEquals("", dgw.readingQualifiers);
	}

	@Test
	void NullStringResultsInEmpty() throws Exception {

		dgw.readingQualifiers = null;

		rules.apply(dgw);
		assertEquals("", dgw.readingQualifiers);
	}

	@Test
	void emptyJsonArrayResultsInEmpty() throws Exception {

		dgw.readingQualifiers = "[]";

		rules.apply(dgw);
		assertEquals("", dgw.readingQualifiers);
	}

	@Test
	void emptyJsonObjectResultsInEmpty() throws Exception {

		dgw.readingQualifiers = "{}";

		rules.apply(dgw);
		assertEquals("", dgw.readingQualifiers);
	}

}
