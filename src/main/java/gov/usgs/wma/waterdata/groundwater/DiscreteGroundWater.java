package gov.usgs.wma.waterdata.groundwater;

import java.sql.Timestamp;

//import java.math.BigDecimal;
//import org.postgis.PGgeometry;

public class DiscreteGroundWater {

	//site_no
	protected String     siteIdentificationNumber;
	//agency_cd
	protected String     agencyCode;
	//lev_dt			 (date of date_measured_raw)
	//lev_tm			 (hour and minute of date_measured_raw)
	//lev_va
	protected String     levelFeetBelowLandSurface;
	//lev_ent_cd		 L or S for below Land or above Sea
	//lev_datum_cd
	//sl_datum_cd
	protected String     verticalDatumCode;
	//sl_lev_va
	protected String     levelFeetAboveVerticalDatum;
	//lev_src_cd
	protected String     measurementSourceCode;
	//lev_agency_cd
	protected String     measuringAgencyCode;
	//lev_acy_cd
	protected String     levelAccuracyCode;
	//lev_status_cd
	protected String     siteStatusCode;
	//lev_meth_cd
	protected String     measurementMethodCode;
	//lev_cr			<empty or omit, this field is no longer loaded>
	//lev_md
	protected String     dateMeasured;
	//lev_dtm
	protected Timestamp  dateMeasuredRaw;
	//lev_dt_acy_cd
	protected String     dateTimeAccuracyCode;
	//lev_tz_cd
	protected String     timezoneCode;
	//lev_utc_dt
	protected String     timeMeasuredUtc;
	//lev_age_cd
	protected String     approvalStatusCode;
	// new field for RDB
	protected String     parameterCode;
}
