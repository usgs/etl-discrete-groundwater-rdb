package gov.usgs.wma.waterdata.groundwater;

import com.fasterxml.jackson.core.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Applies business rules to a domain object.
 * It is assumed that this object can be modified.
 */
public class DiscreteGroundWaterRules {

	//Threadsafe factory
	private static final JsonFactory jsonFactory = new JsonFactory();

	/**
	 * Apply business rules to a DiscreteGroundWater, modifying it in place.
	 * @param domObj
	 */
	public void apply(DiscreteGroundWater domObj) {

		//Rule:  The AQ measurement reading qualifiers (multi-values) are mapped to a single NWISWeb lev_status_cd.
		//If the AQ qualifier(s) are not recognized as valid AQ qualifiers, it is ignored.
		//If the AQ qualifier(s) are empty or just not one that is mapped, it is assumed to be an unqualified measurement.
		//Ref:  https://internal.cida.usgs.gov/jira/browse/IOW-667
		//
		//There is no order or preference among the multi-valued AQ qualifiers to select a single one for NWISWeb.
		//To be repeatable, it is assumed that the qualifiers will be matched in the order of the LevelStatusCode enum,
		//not in order of the values in the db column (order is not repeatable within a JSONB object).
		{
			String orgQualStr = StringUtils.trimWhitespace(domObj.readingQualifiers);
			String newQualStr = "";   //acceptable default value if no other found

			if (StringUtils.hasText(orgQualStr)) {
				try {

					List<String> aqQuals = new ArrayList<>();

					JsonParser parser = jsonFactory.createParser(orgQualStr);

					while (! parser.isClosed()) {

						JsonToken token = parser.nextToken();

						if (token != null && token.isScalarValue()) {
							String val = StringUtils.trimWhitespace(parser.getValueAsString());

							if (StringUtils.hasText(val)) {
								aqQuals.add(StringUtils.trimWhitespace(parser.getValueAsString()));
							}
						}
					}

					if (aqQuals.size() > 0) {

						Optional<LevelStatusCode> lsc = LevelStatusCode.stream()
													  .filter(a -> a.isMapped())
								                      .filter(a -> a.isPresent(aqQuals))
								                      .findFirst();

						if (lsc.isPresent()) {
							newQualStr = lsc.get().getNwisCode();
						}

					}

				} catch (IOException e) {
					throw new RuntimeException(e);
				}

			}

			domObj.readingQualifiers = newQualStr;
		}

		//Rule:  Only the Aquarius TS "1200" approvalLevel is considered approved.  All others values or no value is
		// considered provisional.  "A" is the NWISWeb code for Approved, "P" for Provisional (ie not approved).
		//If the approvalLevel is not recognized as valid, it is ignored and the value considered provisional.
		//Ref:  https://internal.cida.usgs.gov/jira/browse/IOW-666
		//NWISWeb Approval codes:  https://help.waterdata.usgs.gov/code/lev_age_cd?fmt=html
		//AQTS Approval Levels:  (need to be logged into the AQTS system, but they are listed in the ticket)
		{
			String orgApprovalStr = StringUtils.trimWhitespace(domObj.approvalLevel);
			String newApprovalStr = "P";   //provisional default value if no other found

			if (StringUtils.hasText(orgApprovalStr)) {
				if (orgApprovalStr.equals("1200")) {
					newApprovalStr = "A";   //Its approved!
				}
			}

			domObj.approvalLevel = newApprovalStr;
		}

		//Rule:  measurementSourceCode (lev_src_cd in the rdb) is determined based on the collecting agency:
		//If the measuring_agency_code is 'USGS', the lev_src_cd is 'S'
		//If the measuring_agency_code is anything other than 'USGS', lev_src_cd is 'A'.
		//In all other cases, lev_src_cd is left empty
		//
		//Ref:  https://internal.cida.usgs.gov/jira/browse/IOW-737
		//Complete list of legacy NWISWeb lev_src_cd's (of which we are only mapping to 2 of them) :
		//  https://help.waterdata.usgs.gov/code/water_level_src_cd_query?fmt=html
		{
			String agency = StringUtils.trimWhitespace(domObj.measuringAgencyCode);
			String srcCode = "";    //Default for empty or null

			if (StringUtils.hasText(agency)) {
				if (agency.equals("USGS")) {
					srcCode = "S";   //Measured by the reporting agency
				} else {
					srcCode = "A";  //Measured by some other agency
				}
			}

			domObj.measurementSourceCode = srcCode;
		}

		// Rule:  All datums that are currently named "MSL" are in varying stages of being migrated to "LSML", but there
		// are records that still contain the "MSL" datum.  We want to change all those to "LMSL".
		//
		// Note that this rule can probably be removed in the future when migration to "LMSL" is fully realized.
		// This is a belt-and-suspenders approach to make sure we do not feed NWISWeb "MSL" datums.
		//
		// Ref:  https://internal.cida.usgs.gov/jira/browse/IOW-775
		{
			String incomingDatum = StringUtils.trimWhitespace(domObj.verticalDatumCode);
			String outgoingDatum = "";    // Default for empty or null

			if (StringUtils.hasText(incomingDatum)) { // make sure we're dealing with actual values
				if (incomingDatum.equals("MSL")) {
					outgoingDatum = "LMSL";   // Any actual value that is MSL = LMSL now
				} else {
					outgoingDatum = incomingDatum; // Otherwise if it's any other valid datum value, leave it alone
				}
			}

			domObj.verticalDatumCode = outgoingDatum;
		}

	}
}
