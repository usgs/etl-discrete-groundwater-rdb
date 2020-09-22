package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

/**
 * Wrapper of utility methods to write an RDB file format.
 * @author duselman
 */
public class RdbWriter {
	private static final Logger LOG = LoggerFactory.getLogger(RdbWriter.class);
	
	private static final Set<String> BELOW_LAND_SURFACE= Set.of("72227", "72226", "72229", "72228", "72230",
"72231", "72232", "72019", "30210", "61055");

	private static final Set<String> ABOVE_DATUM = Set.of("72150", "62611", "62613", "62610", "62612",
"62600", "62601");

	protected Writer rdb;
	private long headerLineCount;
	private long dataLineCount;
	private final String DELIMITER="\t";

	public RdbWriter(Writer destination) {
		this.rdb = destination;
		initRows();
	}

	public long getHeaderRows() {
		return headerLineCount;
	}
	public long getDataRows() {
		return dataLineCount;
	}
	protected void initRows() {
		headerLineCount = dataLineCount = 0;
	}

	/**
	 * Writes the header of the RDB file. Currently this is fixed but could be jinja for flexibility.
	 */
	public RdbWriter writeHeader() {
		try {
			InputStream header = getClass().getResourceAsStream("/rdb/rdbHeader.txt");
			String head = new String(FileCopyUtils.copyToByteArray(header));
			headerLineCount = head.lines().count();
			rdb.append(head);
		} catch (IOException e) {
			LOG.error("Unable to get RDB Header template", e.getMessage());
			throw new RuntimeException(e);
		}
		return this;
	}

	static final DateTimeFormatter DATE_FORMAT = DateTimeFormat.forPattern("YYYYMMdd");
	static final DateTimeFormatter TIME_FORMAT = DateTimeFormat.forPattern("HHmm");
	static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormat.forPattern("dd-MMM-YYYY HH:mm:ss");

	/**
	 * Marshals GW samples as a RDB row.
	 * @param dgw the sample to write.
	 */
	public RdbWriter writeRow(DiscreteGroundWater dgw) {
		List<String> columns = new ArrayList<>();
		columns.add( validateValue(  5, dgw.agencyCode) );
		columns.add( validateValue( 15, dgw.siteIdentificationNumber) );
		String date = new DateTime(dgw.dateMeasuredRaw).toString(DATE_FORMAT);
		columns.add( validateValue(  8, date) );
		String time = new DateTime(dgw.dateMeasuredRaw).toString(TIME_FORMAT);
		columns.add( validateValue(  4, time) );

		if (ABOVE_DATUM.contains(dgw.parameterCode)) {
			columns.add( validateValue(  7, "") );
			columns.add( validateValue(  1, "S") ); // entry code for above Sea
			columns.add( validateValue( 10, dgw.verticalDatumCode) );
			columns.add( validateValue(  8, dgw.displayResult) );
		} else {
			columns.add( validateValue(  7, dgw.displayResult) );
			columns.add( validateValue(  1, "L") ); // entry code for below Land
			columns.add( validateValue( 10, "") );
			columns.add( validateValue(  8, "") );
		}
		columns.add( validateValue(  1, dgw.measurementSourceCode) );
		columns.add( validateValue(  5, dgw.measuringAgencyCode) );
		columns.add( validateValue(  1, dgw.levelAccuracyCode) );
		columns.add( validateValue(  1, dgw.siteStatusCode) );
		columns.add( validateValue(  1, dgw.measurementMethodCode) );
		// omitting date created, loader no longer references either
		columns.add( validateValue( 25, dgw.dateMeasured) );
		String dateMeasuredRaw = new DateTime(dgw.dateMeasuredRaw).toString(DATE_TIME_FORMAT).toUpperCase();
		columns.add( validateValue( 25, dateMeasuredRaw) );
		columns.add( validateValue(  1, dgw.dateTimeAccuracyCode) );
		columns.add( validateValue(  6, dgw.timezoneCode) );
		columns.add( validateValue( 25, dgw.timeMeasuredUtc) );
		columns.add( validateValue(  1, dgw.approvalStatusCode) );
		columns.add( validateValue(  5, dgw.parameterCode) );
		writeRow(columns);
		dataLineCount++;
		return this;
	}

	protected RdbWriter writeRow(List<String> columns) {
		try {
			String row = String.join(DELIMITER, columns.toArray(new String[] {}));
			rdb.append(row).append("\n");
		} catch (IOException e) {
			throw new RuntimeException("Error writing RDB row to stream.", e);
		}
		return this;
	}

	/**
	 * Helper method main purpose is length truncation.
	 * RDB files are not only tab delimited but also specify each.
	 * entry length. This writes only the given length value
	 * @param length max number of chars to write
	 * @param value characters to write, non-string values must be converted.
	 */
	protected String validateValue(int length, String value) {
		// Trim the value to the proper field length.
		String trimmedValue = value;
		if (value == null) {
			value = "";
		}
		if (value.length() > length) {
			trimmedValue = value.substring(0, length);
		}
		return trimmedValue;
	}
}
