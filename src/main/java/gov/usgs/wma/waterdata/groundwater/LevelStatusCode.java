package gov.usgs.wma.waterdata.groundwater;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Stream;

/**
 * Represents the mapping from Aquarius TS Result Qualifiers to NWISWeb 'lev_status_cd' in the RDB.
 * See https://internal.cida.usgs.gov/jira/browse/IOW-667 for details on the mapping.
 */
public enum LevelStatusCode {

	STATIC("1", "Static", "Static", true, true),
	BELOW("2", "Below", "True value is below reported value due to local conditions", true, true),
	ABOVE("3", "Above", "True value is above reported value due to local conditions", true, true),
	TIDE_AFFECTED("4", "GWTideAffected", "Groundwater level affected by tide", true, true),
	SW_AFFECTED("5", "GWSWAffected", "Groundwater level affected by surface water", true, true),
	NO_MEASUREMENT("6", "NoMeasurement", "Measurement unable to be obtained due to local conditions", true, true),
	SALINE("7", "Saline", "Groundwater level affected by brackish or saline water", true, true),
	FOREIGN("8", "ForeignSubstance", "Foreign substance was present on the surface of the water", true, true),
	REVISED(null, "Revised", "Value was revised after publication as an approved value", true, false),
	CROSS_SECTION_WEIGHTED(null, "CrossSectionAreaWeightedMean", "Area-weighted mean from horizontal cross-section measurements", true, false),
	CROSS_SECTION_SIMPLE(null, "CrossSectionSimpleMean", "Simple mean from horizontal cross-section measurements", true, false),
	UNKNOWN(null, "", "", false, false);



	private String aqCode;
	private String aqDescription;
	private String nwisCode;
	private boolean real;
	private boolean mapped;

	private LevelStatusCode(String nwisCode, String aqCode, String aqDescription, boolean real, boolean mapped) {
		this.aqCode = aqCode;
		this.aqDescription = aqDescription;
		this.nwisCode = nwisCode;
		this.real = real;
		this.mapped = mapped;
	}

	/**
	 * An ordered stream of all enum values.
	 * @return
	 */
	public static Stream<LevelStatusCode> stream() {
		return Arrays.stream(LevelStatusCode.values());
	}

	/**
	 * The Aquarius code for this measurement status.
	 * @return
	 */
	public String getAqCode() {
		return aqCode;
	}

	/**
	 * The Aquarius description for this measurement status.
	 * @return
	 */
	public String getAqDescription() {
		return aqDescription;
	}

	/**
	 * The NWISWeb code for this measurement status.
	 * @return
	 */
	public String getNwisCode() {
		return nwisCode;
	}

	/**
	 * Is this a recognized value? (UNKNOWN is not)
	 * @return
	 */
	public boolean isReal() {
		return real;
	}

	/**
	 * Is this an Aquarius measurement status that has a corresponding NWISWeb code?
	 * Not all AQ codes are mapped to NWISWeb codes.
	 * @return
	 */
	public boolean isMapped() {
		return mapped;
	}

	public boolean isPresent(Collection<String> aqQualifiers) {
		return aqQualifiers.contains(getAqDescription()) 
				|| aqQualifiers.contains(getAqCode());
	}

}
