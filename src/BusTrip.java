import java.time.LocalTime;

// This class simply stores data related to a trip that a
// bus makes between two stops. It is a type of bus route.
public class BusTrip implements BusRoute {
	private final int id;
	private final LocalTime arrivalTime;
	private final LocalTime departureTime;
	private final BusStop origin;
	private final BusStop destination;
	private final int stopSequence;
	private final String stopHeadsign;
	private final int pickupType;
	private final int dropoffType;
	private final double shapeDistTraveled;

	// Constructor
	public BusTrip(
		int id,
		LocalTime arrivalTime, LocalTime departureTime,
		BusStop origin, BusStop destination, int stopSequence, String stopHeadsign,
		int pickupType, int dropoffType, double shapeDistTraveled) {
		this.id = id;
		this.arrivalTime = arrivalTime;
		this.departureTime = departureTime;
		this.origin = origin;
		this.destination = destination;
		this.stopSequence = stopSequence;
		this.stopHeadsign = stopHeadsign;
		this.pickupType = pickupType;
		this.dropoffType = dropoffType;
		this.shapeDistTraveled = shapeDistTraveled;
	}

	// A bus trip always has a cost of 1
	public double getCost() {
		return 1;
	}

	// Getters
	public int getID() { return id; }
	public LocalTime getArrivalTime() { return arrivalTime; }
	public LocalTime getDepartureTime() { return departureTime; }
	public BusStop getOriginStop() { return origin; }
	public BusStop getDestinationStop() { return destination; }
	public int getStopSequence() { return stopSequence; }
	public String getStopHeadsign() { return stopHeadsign; }
	public int getPickupType() { return pickupType; }
	public int getDropoffType() { return dropoffType; }
	public double getShapeDistTraveled() { return shapeDistTraveled; }
}
