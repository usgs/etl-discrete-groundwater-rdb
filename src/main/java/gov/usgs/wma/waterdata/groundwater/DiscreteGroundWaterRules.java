package gov.usgs.wma.waterdata.groundwater;

import com.fasterxml.jackson.core.*;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;
import java.util.stream.Stream;

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

					List<String> aqQuals = new ArrayList();

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

						Optional<LevelStatusCode> lsc = Stream.of(LevelStatusCode.values())
													  .filter(a -> a.isMapped())
								                      .filter(a -> aqQuals.contains(a.getAqDescription()))
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



	}
}
