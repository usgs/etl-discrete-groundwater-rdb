package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

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

			if (! StringUtils.isEmpty(orgQualStr)) {
				try {

					List<String> aqQuals = new ArrayList<>();

					JsonParser parser = jsonFactory.createParser(orgQualStr);

					while (! parser.isClosed()) {

						JsonToken token = parser.nextToken();

						if (token != null && token.isScalarValue()) {
							String val = StringUtils.trimWhitespace(parser.getValueAsString());

							if (!StringUtils.isEmpty(val)) {
								aqQuals.add(StringUtils.trimWhitespace(parser.getValueAsString()));
							}
						}
					}

					if (aqQuals.size() > 0) {

						Optional<LevelStatusCode> lsc = LevelStatusCode.stream()
													  .filter(a -> a.isMapped())
								                      .filter(a -> qualifierPresent(a, aqQuals))
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

			if (! StringUtils.isEmpty(orgApprovalStr)) {
				if (orgApprovalStr.equals("1200")) {
					newApprovalStr = "A";   //Its approved!
				}
			}

			domObj.approvalLevel = newApprovalStr;
		}


	}

	public static boolean qualifierPresent(LevelStatusCode levelStatusCode, Collection<String> aqQualifiers) {
		return aqQualifiers.contains(levelStatusCode.getAqDescription())
				|| aqQualifiers.contains(levelStatusCode.getAqCode());
	}
}
