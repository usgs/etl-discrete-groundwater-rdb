package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

public class RdbWriter {
	private static final Logger LOG = LoggerFactory.getLogger(RdbWriter.class);

	@Value("classpath:rdbHeader.txt")
	protected Resource header;

	private Writer rdb;

	public RdbWriter(Writer dest) {
		this.rdb = dest;
	}

	public void writeHeader() {
		try {
			String head = new String(FileCopyUtils.copyToByteArray(header.getInputStream()));
			rdb.append(head);
		} catch (IOException e) {
			LOG.error("Unable to get SQL statement", e);
			throw new RuntimeException(e);
		}
	}

	public void writeRow(DiscreteGroundWater dgw) {
		writeValue(  5, dgw.agencyCode);
		writeValue( 15, dgw.siteIdentificationNumber);
		writeValue(  8, new SimpleDateFormat("YYYYMMDD").format(dgw.dateMeasuredRaw));
		writeValue(  4, new SimpleDateFormat("HHMM").format(dgw.dateMeasuredRaw));

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
		//writeValue( 25, dgw.createdDate); // omitted
		writeValue( 25, dgw.dateMeasured);
		writeValue( 25, new SimpleDateFormat("DD-MMM-YYYY HH:MM:SS").format(dgw.dateMeasuredRaw));
		writeValue(  1, dgw.dateTimeAccuracyCode);
		writeValue(  6, dgw.timezoneCode);
		writeValue( 25, dgw.timeMeasuredUtc);
		writeValue(  1, dgw.approvalStatusCode);
		// TODO pCode to follow
		writeValue(  1, "\n");
	}
	public void writeValue(int length, String value) {
		try {
			rdb.append(value).append('\t');
		} catch (IOException e) {
			throw new RuntimeException("Error writing RDB row to stream.", e);
		}
	}
}
