import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

// This class simply stores data related to a bus stop,
// including the stop itself and any transfers out of it.
public class BusStop {
	private final int id;
	private final int code;
	private final String name;
	private final String description;
	private final double latitude;
	private final double longitude;
	private final String url;
	private final String zone;
	private final int locationType;
	private final String parentStation;

	// A list of trips leaving this stop
	private final List<BusTrip> outgoingTrips;

	// A list of transfers leaving this stop
	private final List<BusTransfer> outgoingTransfers;

	// Constructor
	public BusStop(
		int id, int code, String name, String description,
		double latitude, double longitude, String url, String zone,
		int locationType, String parentStation) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.description = description;
		this.latitude = latitude;
		this.longitude = longitude;
		this.url = url;
		this.zone = zone;
		this.locationType = locationType;
		this.parentStation = parentStation;
		outgoingTrips = new ArrayList<>();
		outgoingTransfers = new ArrayList<>();
	}

	// Add a trip from this stop to another
	public void addTrip(BusTrip newTrip) {
		outgoingTrips.add(newTrip);
	}

	// Add a transfer from this stop to another
	public void addTransfer(BusTransfer newTransfer) {
		outgoingTransfers.add(newTransfer);
	}

	// Return a list of all trips and transfers leaving this station
	public List<BusRoute> getAllRoutes() {
		List<BusRoute> allRoutes = new ArrayList<>(outgoingTrips.size() + outgoingTransfers.size());
		allRoutes.addAll(outgoingTrips);
		allRoutes.addAll(outgoingTransfers);
		return allRoutes;
	}

	// Getters
	public int getID() { return id; }
	public int getCode() { return code; }
	public String getName() { return name; }
	public String getDescription() { return description; }
	public double getLatitude() { return latitude; }
	public double getLongitude() { return longitude; }
	public String getUrl() { return url; }
	public String getZone() { return zone; }
	public int getLocation() { return locationType; }
	public String getParentStation() { return parentStation; }
	public List<BusTrip> getTrips() { return outgoingTrips; }
	public List<BusTransfer> getTransfers() { return outgoingTransfers; }

}
