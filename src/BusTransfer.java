
// This class simply contains the data related to a bus transfer
// to some stop. It is a type of bus route.
public class BusTransfer implements BusRoute {
	private final BusStop destination;
	private final int type;
	private final double minTravelTime;

	// Constructor
	public BusTransfer(BusStop destination, int type, double minTravelTime) {
		this.destination = destination;
		this.type = type;
		this.minTravelTime = minTravelTime;
	}

	// A bus transfer can have a cost of 2 or minTravelTime/100
	public double getCost() {
		if (type == 0)
			return 2;
		return minTravelTime / 100;
	}

	// Getters
	public BusStop getDestinationStop() { return destination; }
	public int getType() { return type; }
	public double getMinTravelTime() { return minTravelTime; }
}
