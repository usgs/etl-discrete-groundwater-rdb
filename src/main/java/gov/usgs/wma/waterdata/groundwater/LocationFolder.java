package gov.usgs.wma.waterdata.groundwater;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.FileCopyUtils;

/**
 * Utility functions to facilitate AQTS location folder to state names and abbreviations.
 *
 * Note: This list comes from the retriever. If there are new, different, or updates to
 * to the location folders then this class will require an update to match.
 *
 * @author duselman
 */
@Component
public class LocationFolder {

	private StatePostCodeDao dao;

	@Autowired
	public LocationFolder(StatePostCodeDao dao) {
		this.dao = dao;
	}

	/**
	 * Converts an AQTS location folder to a collection of states. Most location folders
	 * map to state names; however, there are a few that are not state names and contain a
	 * collection of states. This returns the list for multiple states in a location folder
	 * or the location folder as the singular entry as default.
	 *
	 * @param locationFolder usually state names or collection of state names or abbreviations
	 * @return list of state names in the given location folder
	 */
	public List<String> toStates(String locationFolder) {
		List<String> states = new ArrayList<>();

		// if this changes from single use lambda to some library then pull these to static class level
		if ("MD-DE-DC".equals(locationFolder)) {
			states.add("Maryland");
			states.add("Delaware");
			states.add("District of Columbia");

		} else if ("MA-RI".equals(locationFolder)) {
			states.add("Massachusetts");
			states.add("Rhode Island");

		} else if ("NH-VT".equals(locationFolder)) {
			states.add("New Hampshire");
			states.add("Vermont");

		} else {
			// the default is to use the location folder (a state name) as the singular entry.
			states.add(locationFolder);
		}
		return states;
	}

	/**
	 * Converts the location folder into an NWISWeb file descriptor abbreviations. This is
	 * usually the state postal code but in some rare cases it is a special string. Especially,
	 * for those location folders that contains multiple states. One other is for location
	 * folders not found in the state table. The state table contains some extras like
	 * "Puerto Rico" but not all.
	 *
	 * As noted on the class, the data was derived from the retriever. In that file was the
	 * "Pacific Islands" defined as a location folder but not found in the observations state
	 * table. (line 10 below)
	 *    10: {'AI_Loc'   : 'Pacific Islands' , 'Loc_Abbrev'  : 'PI'       },
	 * This class returns "PI" for this exceptional case. It can be updated for others as well.
	 *
	 * @param locationFolder usually state name or a collection of state names or abbreviations
	 * @return the postal codes for a location folder
	 */
	public String filenameDecorator(String locationFolder) {
		if ("MD-DE-DC".equals(locationFolder)) {
			return locationFolder;
		} else if ("MA-RI".equals(locationFolder)) {
			return locationFolder;
		} else if ("NH-VT".equals(locationFolder)) {
			return locationFolder;
		} else if ("Pacific Islands".equals(locationFolder)) {
			return "PI";
		}
		return dao.getPostCode(locationFolder);
	}

	/**
	 * Utility function that loads the location folders from a resource file.
	 * The file was generated from the location folders file from the retriever project.
	 * This file will require updates if the AQTS program adds location folders.
	 * If this becomes a regular occurrence then a refactor will be useful at that time.
	 * For now, there have been few (if any) additional location folders added in years.
	 * @return a collection of all the location folders.
	 */
	public List<String> getLocationFolders() {
		try {
			InputStream locations = getClass().getResourceAsStream("/rdb/locationFolders.txt");
			String folders = new String(FileCopyUtils.copyToByteArray(locations));
			List<String> locationFolders = folders.lines().collect(Collectors.toList());
			return locationFolders;
		} catch (IOException e) {
			throw new RuntimeException("Failed to load locaiton folders resource.", e);
		}
	}

}
