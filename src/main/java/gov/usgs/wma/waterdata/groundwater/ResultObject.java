package gov.usgs.wma.waterdata.groundwater;

/**
 * AWS lambda response interface object.
 * Place holder for response abject.
 * @author duselman
 */
public class ResultObject {
	private Integer count;
	private String filename;

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
}
