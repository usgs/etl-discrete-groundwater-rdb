package gov.usgs.wma.waterdata.groundwater;

import java.io.Writer;

public class S3Bucket {

	public String createFilename(String tier, String suffix, String metadata) {
		return "ts" + "<tier>" + suffix + ".gw_lev_01.06." + "<date meta>" + ".full.rdb";
	}

	public Writer makeFile(String string) {
		// TODO Auto-generated method stub
		return null;
	}
}
