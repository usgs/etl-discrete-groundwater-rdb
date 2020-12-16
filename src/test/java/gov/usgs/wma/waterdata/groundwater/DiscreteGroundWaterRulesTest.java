package gov.usgs.wma.waterdata.groundwater;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DiscreteGroundWaterRulesTest {

	DiscreteGroundWater dgw;
	DiscreteGroundWaterRules rules = new DiscreteGroundWaterRules();

	@BeforeEach
	public void beforeEach() {
		dgw = new DiscreteGroundWater();
		dgw.readingQualifiers = "[\"" + LevelStatusCode.ABOVE.getAqDescription() + "\"]";
		dgw.approvalLevel = "1200";
		dgw.measuringAgencyCode = "USGS";
		dgw.verticalDatumCode = "MSL";
	}


	//
	//Rule:  The AQ measurement reading qualifiers (multi-values) are mapped to a single NWISWeb lev_status_cd.

	@Test
	void qualifiersSingleRecognizedValueIsMapped() throws Exception {
		rules.apply(dgw);
		assertEquals(LevelStatusCode.ABOVE.getNwisCode(), dgw.readingQualifiers);
	}

	@Test
	void qualifiersMultipleRecognizedValuesMapsFirstOneInEnumOrder() throws Exception {

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
	void qualifiersMultipleRecognizedValuesMapsFirstOneInEnumOrderAndIgnoresJunk() throws Exception {

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
	void qualifiersMultipleRecognizedValuesMapsFirstOneInEnumOrderCodeOrDescription() throws Exception {

		dgw.readingQualifiers = "["
				                        + "\"JUNK junk ;lkj;lkjakhsd\","
				                        + "\"" +  LevelStatusCode.FOREIGN.getAqDescription() + "\","
				                        + "\"" +  LevelStatusCode.ABOVE.getAqCode() + "\"," //This is the first in the enum
				                        + "\"" +  LevelStatusCode.TIDE_AFFECTED.getAqDescription() + "\","
				                        + "\"JUNK junk ;lkj;lkjakhsd\""
				                        + "]";

		rules.apply(dgw);
		assertEquals(LevelStatusCode.ABOVE.getNwisCode(), dgw.readingQualifiers);
	}

	@Test
	void qualifiersNoMatchedValuesResultsInEmpty() throws Exception {

		dgw.readingQualifiers = "["
				                        + "\"JUNK junk ;lkj;lkjakhsd\","
				                        + "\"JUNK junk ;lkj;lkjakhsd\""
				                        + "]";

		rules.apply(dgw);
		assertEquals("", dgw.readingQualifiers);
	}

	@Test
	void qualifiersEmptyStringResultsInEmpty() throws Exception {

		dgw.readingQualifiers = "";

		rules.apply(dgw);
		assertEquals("", dgw.readingQualifiers);
	}

	@Test
	void qualifiersNullStringResultsInEmpty() throws Exception {

		dgw.readingQualifiers = null;

		rules.apply(dgw);
		assertEquals("", dgw.readingQualifiers);
	}

	@Test
	void qualifiersEmptyJsonArrayResultsInEmpty() throws Exception {

		dgw.readingQualifiers = "[]";

		rules.apply(dgw);
		assertEquals("", dgw.readingQualifiers);
	}

	@Test
	void qualifiersEmptyJsonObjectResultsInEmpty() throws Exception {

		dgw.readingQualifiers = "{}";

		rules.apply(dgw);
		assertEquals("", dgw.readingQualifiers);
	}

	//
	//Rule:  Only the "1200" approvalLevel is considered approved.

	@Test
	void approval1200IsApproved() throws Exception {
		rules.apply(dgw);
		assertEquals("A", dgw.approvalLevel);
	}

	@Test
	void approval900IsNotApproved() throws Exception {
		dgw.approvalLevel = "900";
		rules.apply(dgw);
		assertEquals("P", dgw.approvalLevel);
	}

	@Test
	void approvalRandomJunkIsNotApproved() throws Exception {
		dgw.approvalLevel = "asdf9wehasdlkjhadsf\\;\"DROP TABLE DUAL;";
		rules.apply(dgw);
		assertEquals("P", dgw.approvalLevel);
	}

	@Test
	void approvalNullIsNotApproved() throws Exception {
		dgw.approvalLevel = null;
		rules.apply(dgw);
		assertEquals("P", dgw.approvalLevel);
	}

	@Test
	void approvalEmptyIsNotApproved() throws Exception {
		dgw.approvalLevel = "";
		rules.apply(dgw);
		assertEquals("P", dgw.approvalLevel);
	}

	@Test
	void approvalSingleSpaceIsNotApproved() throws Exception {
		dgw.approvalLevel = " ";
		rules.apply(dgw);
		assertEquals("P", dgw.approvalLevel);
	}

	@Test
	void approvalWhitespaceSpecialCharsAreNotApproved() throws Exception {
		dgw.approvalLevel = "\t\n\r";
		rules.apply(dgw);
		assertEquals("P", dgw.approvalLevel);
	}

	//
	//measurementSourceCode Rule

	@Test
	void sourceCodeUsgsAgencyMapsTo_S() {
		rules.apply(dgw);   //"USGS" agency set in beforeEach
		assertEquals("S", dgw.measurementSourceCode);
	}

	@Test
	void sourceCodeNonUSGSAgencyMapsTo_A() {
		dgw.measuringAgencyCode = "XYZ";
		rules.apply(dgw);
		assertEquals("A", dgw.measurementSourceCode);
	}

	@Test
	void sourceCodeNullAgencyMapsTo_Emptyu() {
		dgw.measuringAgencyCode = null;
		rules.apply(dgw);
		assertEquals("", dgw.measurementSourceCode);
	}

	@Test
	void sourceCodeEmptyAgencyMapsTo_Emptyu() {
		dgw.measuringAgencyCode = "";
		rules.apply(dgw);
		assertEquals("", dgw.measurementSourceCode);
	}

	@Test
	void sourceCodeWhitespaceAgencyMapsTo_Emptyu() {
		dgw.measuringAgencyCode = "\t ";
		rules.apply(dgw);
		assertEquals("", dgw.measurementSourceCode);
	}

	//
	// all "MSL" datums are "LMSL" datums Rule

	@Test
	void verticalDatumCode_MSL_MapsTo_LMSL() {
		rules.apply(dgw); // "MSL" vertical datum code set in beforeEach
		assertEquals("LMSL", dgw.verticalDatumCode);
	}

	@Test
	void verticalDatumCode_NonMSL_MapsTo_nonMSL() {
		dgw.verticalDatumCode = "NGVD29";
		rules.apply(dgw);
		assertEquals("NGVD29", dgw.verticalDatumCode);
	}

	@Test
	void verticalDatumCode_Null_MapsTo_EmptyString() {
		dgw.verticalDatumCode = null;
		rules.apply(dgw);
		assertEquals("", dgw.verticalDatumCode);
	}

	@Test
	void verticalDatumCode_Empty_MapsTo_EmptyString() {
		dgw.verticalDatumCode = "";
		rules.apply(dgw);
		assertEquals("", dgw.verticalDatumCode);
	}

	@Test
	void verticalDatumCode_Whitespace_MapsTo_EmptyString() {
		dgw.verticalDatumCode =  "\t ";
		rules.apply(dgw);
		assertEquals("", dgw.verticalDatumCode);
	}

	@Test
	void verticalDatumCode_WhitespaceMSL_MapsTo_LMSL() {
		dgw.verticalDatumCode =  "\t MSL ";
		rules.apply(dgw);
		assertEquals("LMSL", dgw.verticalDatumCode);
	}

	@Test
	void verticalDatumCode_LMSL_MapsTo_LMSL() {
		dgw.verticalDatumCode =  "LMSL";
		rules.apply(dgw);
		assertEquals("LMSL", dgw.verticalDatumCode);
	}
}
