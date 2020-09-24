package gov.usgs.wma.waterdata.groundwater;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

public class Parameter {
	String parameterCode;
	boolean aboveDatum;
	boolean belowLandSurface;

	public String getParameterCode() {
		return parameterCode;
	}

	public void setParameterCode(String parameterCode) {
		this.parameterCode = parameterCode;
	}

	public boolean isAboveDatum() {
		return aboveDatum;
	}

	public void setAboveDatum(boolean aboveDatum) {
		this.aboveDatum = aboveDatum;
	}

	public boolean isBelowLandSurface() {
		return belowLandSurface;
	}

	public void setBelowLandSurface(boolean belowLandSurface) {
		this.belowLandSurface = belowLandSurface;
	}
}
