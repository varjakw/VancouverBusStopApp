import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Comparator;

// This class is used to return data associated
// with a shortest path found between to bus stops.
final class ShortestPath {
	private final List<BusStop> path;
	private final double cost;

	// Constructor
	public ShortestPath(List<BusStop> path, double cost) {
		this.path = path;
		this.cost = cost;
	}

	// Getters
	public List<BusStop> getPath() { return path; }
	public double getCost() { return cost; }
}

// Provides static methods for find the shortest
// path between two bu stops via trips and transfers.
public class BusShortestPathFinder {

	// Find the shortest path between origin and destination using Dijkstra's algorithm
	static ShortestPath Dijkstra(BusStop origin, BusStop destination) throws Exception {

		// A map between a bus stop and the total cost to get there from origin
		Map<BusStop, Double> pathCosts = new HashMap<>();

		// A map from a bus stop to another bus stop leading towards origin
		Map<BusStop, BusStop> paths = new HashMap<>();

		// A queue of unevaluated stops ordered by total cost from origin
		Comparator<BusStop> orderByPathCost = (BusStop a, BusStop b) -> Double.compare(pathCosts.get(a), pathCosts.get(b));
		PriorityQueue<BusStop> unevaluatedStops = new PriorityQueue<>(orderByPathCost);

		// Add the origin stop to begin
		pathCosts.put(origin, 0.0);
		unevaluatedStops.add(origin);

		// Loop until there are no more unevaluated stops
		while(!unevaluatedStops.isEmpty()) {

			// Get the next closest unevaluated stop and relax all its outgoing routes
			BusStop currentStop = unevaluatedStops.poll();
			for (BusRoute currentRoute : currentStop.getAllRoutes()) {

				// The total cost of this path from origin and the stop at the end of route
				double pathCost = pathCosts.get(currentStop) + currentRoute.getCost();
				BusStop destinationStop = currentRoute.getDestinationStop();

				// If the bus stop hasn't been visited before, add to unevaluated stop queue
				if (pathCosts.get(destinationStop) == null) {
					pathCosts.put(destinationStop, pathCost);
					unevaluatedStops.add(destinationStop);
				}

				// If this path is shorter or equal, update paths and its cost
				if (pathCost <= pathCosts.get(destinationStop)) {
					pathCosts.put(destinationStop, pathCost);
					paths.put(destinationStop, currentStop);
				}
			}
		}

		// If the cost to the destination is null, then a path was not found
		if (pathCosts.get(destination) == null)
			throw new Exception("No path found!");

		// Retrace the path map from destination to origin to construct a list of stops representing the path.
		List<BusStop> path = new ArrayList<>();
		BusStop currentStop = destination;
		while (currentStop != origin) {
			path.add(0, currentStop);             // Add to front of list due to reverse-tracing path
			currentStop = paths.get(currentStop); // Trace the path back up one step towards origin
		}

		// Remember to add the first stop to the path as well!
		path.add(0, currentStop);

		return new ShortestPath(path, pathCosts.get(destination));
	}

	/* Previous implementation */
	/*
	static List<Nodes> graph = new ArrayList<>();
	static final int INFI = 1000000000;

	// Node Class
	static class Nodes {
		int vertexNumber;
		// List shows number of vertex and the weight of the connected edge
		List<Pair> child;

		Nodes(int vertexNumber) {
			this.vertexNumber = vertexNumber;
			child = new ArrayList<>();

		}

		// Add the child for the given node
		void add_child(int numberOfVetex, double length) {
			Pair pair = new Pair(numberOfVetex, length);
			child.add(pair);
		}
	}

	public static void main(String[] args) {
		//vertices
		int vertices = 99999;
		// source
		//int source = 2939 ;
		//destination
		//int destination = 1888;

		//User Input
		Scanner input = new Scanner(System.in);
		System.out.println("Search to find shortest path between 2 bus stops ");

		System.out.print("Enter start bus stop id:");
		int source = input.nextInt();
		System.out.print("Enter destination bus stop id:");
		int destination = input.nextInt();

		// Create the nodes
		for(int i = 0; i < vertices; i++) {
			Nodes nodes = new Nodes(i);
			graph.add(nodes);
		}

		String file  = "stop_times.txt";
		create_edge_stoptimes(file);

		file  = "transfers.txt";
		create_edge_transfers(file);

		int[] path = new int[graph.size()];
		double[] dist = dijkstraShortestPath(graph, source, path);

		if (dist[destination] == INFI) {
			System.out.printf("Source: %d and Destination: %d is not connected\n", source, destination);
			System.out.println("Bus stop doesn’t exist, no possible routes");

		}
		else {
			System.out.println("\nSource: From bus stop: "+ source + " to Destination:  "+destination+ " | Total Cost: "+dist[destination]);
			System.out.println("List of Stops en route:");

			printShortestPath(path, destination, source, dist);
			System.out.println("Stop id - "+destination+ " | Accumulated Cost: " + dist[destination]);

		}
	}
	*/
}
