package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.text.SimpleDateFormat;

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

	protected Writer rdb;
	private long headerLineCount;
	private long dataLineCount;
	private String sepChar="";

	public RdbWriter(Writer dest) {
		this.rdb = dest;
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
			LOG.error("Unable to get RDB Header template", e);
			throw new RuntimeException(e);
		}
		return this;
	}

	/**
	 * Marshals GW samples as a RDB row.
	 * @param dgw the sample to write.
	 */
	public RdbWriter writeRow(DiscreteGroundWater dgw) {
		sepChar = "";
		writeValue(  5, dgw.agencyCode);
		sepChar = "\t";
		writeValue( 15, dgw.siteIdentificationNumber);
		writeValue(  8, new SimpleDateFormat("YYYYMMdd").format(dgw.dateMeasuredRaw));

		String time = new SimpleDateFormat("HHmm").format(dgw.dateMeasuredRaw);
		if ("1200".endsWith(time)) {
			time = "";
		}
		writeValue(  4, time);

		if (StringUtils.isEmpty(dgw.levelFeetBelowLandSurface)) {
			writeValue(  1, "S"); // entry code for above Sea
			writeValue( 10, dgw.verticalDatumCode);
			writeValue(  8, dgw.levelFeetAboveVerticalDatum);
		} else {
			writeValue(  7, dgw.levelFeetBelowLandSurface);
			writeValue(  1, "L"); // entry code for below Land
		}
		writeValue(  1, dgw.measurementSourceCode);
		writeValue(  5, dgw.measuringAgencyCode);
		writeValue(  1, dgw.levelAccuracyCode);
		writeValue(  1, dgw.siteStatusCode);
		writeValue(  1, dgw.measurementMethodCode);
		//writeValue( 25, dgw.createdDate); // omitted, loader no longer references
		writeValue( 25, dgw.dateMeasured);
		writeValue( 25, new SimpleDateFormat("dd-MMM-YYYY HH:mm:ss").format(dgw.dateMeasuredRaw).toUpperCase());
		writeValue(  1, dgw.dateTimeAccuracyCode);
		writeValue(  6, dgw.timezoneCode);
		writeValue( 25, dgw.timeMeasuredUtc);
		writeValue(  1, dgw.approvalStatusCode);
		// TODO pCode to follow after comparing the current format to existing.
		writeValue(  1, "\n");

		dataLineCount++;
		return this;
	}

	/**
	 * Helper method main purpose is length truncation.
	 * RDB files are not only tab delimited but also specify each.
	 * entry length. This writes only the given length value
	 * @param length max number of chars to write
	 * @param value characters to write, non-string values must be converted.
	 */
	protected RdbWriter writeValue(int length, String value) {
		try {
			// Trim the value to the proper field length.
			String trimmedValue = value;
			if (value == null) {
				value = "";
			}
			if (value.length() > length) {
				trimmedValue = value.substring(0, length);
			}
			rdb.append(sepChar).append(trimmedValue);
		} catch (IOException e) {
			throw new RuntimeException("Error writing RDB row to stream.", e);
		}
		return this;
	}

	public RdbWriter flush() {
		try {
			rdb.flush();
		} catch (IOException e) {
			// do not care about flush exceptions
		}
		return this;
	}

	public RdbWriter close() {
		try {
			flush();
			rdb.close();
		} catch (IOException e) {
			// do not care about close exceptions
		}
		return this;
	}
}
