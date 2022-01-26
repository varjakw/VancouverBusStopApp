import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// This class handles everything to do with bus stops,
// including parsing bus stops from files, parsing transfers
// from files and sorting bus stops via stop IDs.
public class BusStops {

	public static class SearchNode {
		private char data;
		private SearchNode left, equal, right;
		private boolean termination;

		public SearchNode(char val) {
			data = val;
			left = null;
			equal = null;
			right = null;
			termination = false;
		}
	}

	// A map between bus stop IDs and a Bus Stop object
	private final Map<Integer, BusStop> stops;

	// A map between bus stop names and IDs
	private Map<String, Integer> stopNames;

	// Root node for TST
	private SearchNode tstRootNode;

	// Constuctor
	public BusStops() {
		stops = new HashMap<>();
		stopNames = new HashMap<>();
		tstRootNode = null;
	}

	// Add unsorted stops to the stop map, names map, and stop names to the TST
	public void addStops(Map<Integer, BusStop> newStops) {

		// Add bus stop objects to stops hash map
		stops.putAll(newStops);

		String[] keywords = { "FLAGSTOP", "WB", "NB", "SB", "EB" };
		for (Map.Entry stop : newStops.entrySet()) {
			int id = (int) stop.getKey();
			// Getting rid of all keywords
			String name = (String) ((BusStop) stop.getValue()).getName();
			String[] splitName = name.split("\\s+");

			while (Arrays.stream(keywords).anyMatch(splitName[0]::equals)) {
				name = String.join(" ", String.join(" ", Arrays.copyOfRange(splitName, 1, splitName.length)),
						splitName[0]);
				splitName = name.split("\\s+");
			}

			stopNames.put(name, id);

			char[] nameCharArr = name.toCharArray();
			if (tstRootNode == null) {
				tstRootNode = addNameTST(nameCharArr, tstRootNode);
			} else {
				addNameTST(nameCharArr, tstRootNode);
			}
		}

	}

	// Return stop by ID
	public BusStop get(int id) {
		return stops.get(id);
	}

	// Return all stop in a list
	public List<BusStop> getAll() {
		return new ArrayList<>(stops.values());
	}

	// Find all trips with a specific arrival time and sort by ID
	public List<BusTrip> getTripsWithArrivalTime(LocalTime time) {
		// For every stop
		return stops.values().stream()
				// For every trip in a stop
				.flatMap(stop -> stop.getTrips().stream())
				// Keep only trips with arrival time = time
				.filter(trip -> trip.getArrivalTime().equals(time))
				// Sort by ID
				.sorted((a, b) -> Integer.compare(a.getID(), b.getID()))
				// Return as list
				.collect(Collectors.toList());
	}

	public List<BusStop> getSearchResults(BusStops allStops, String searchQuery) {
		List<BusStop> foundStops = new ArrayList<>();

		char[] searchCharArr = searchQuery.toCharArray();
		SearchNode subStringFound = substringSearch(searchCharArr, tstRootNode);

		if (subStringFound != null) {
			List<String> foundPostfixes = findAllStops(subStringFound);
			String prefix = "";
			if (searchQuery.length() >= 2) {
				prefix = searchQuery.substring(0, searchQuery.length() - 1);
			}

			for (String postfix : foundPostfixes) {
				if (stopNames.containsKey(prefix + postfix)) {
					int id = (int) stopNames.get(prefix + postfix);
					foundStops.add(allStops.get(id));
				}
			}
		}

		return foundStops;
	}

	// Recursively adds nodes (if not already existing) for each char in a stop name
	// to the TST
	public SearchNode addNameTST(char[] stopName, SearchNode currNode) {

		if (currNode == null) {
			SearchNode newNode = new SearchNode(stopName[0]);
			currNode = newNode;

		}

		if (currNode.data < stopName[0]) {
			currNode.right = addNameTST(stopName, currNode.right);
		}

		else if (currNode.data > stopName[0]) {
			currNode.left = addNameTST(stopName, currNode.left);
		}

		else {
			if (stopName.length <= 1) {
				currNode.termination = true;
			} else {
				currNode.equal = addNameTST(Arrays.copyOfRange(stopName, 1, stopName.length), currNode.equal);
			}
		}
		return currNode;
	}

	// Recursively search for a search term by char in the TST
	public SearchNode substringSearch(char[] search, SearchNode currNode) {

		if (currNode == null) {
			return null;
		}

		if (currNode.data < search[0]) {
			return substringSearch(search, currNode.right);
		}

		else if (currNode.data > search[0]) {
			return substringSearch(search, currNode.left);
		}

		else {
			if (search.length <= 1) {
				return currNode;
			} else {
				return substringSearch(Arrays.copyOfRange(search, 1, search.length), currNode.equal);
			}
		}

	}

	// Recursively traverses the tree starting from a given node and returns all
	// found words
	public List<String> findAllStops(SearchNode currNode) {

		ArrayList<String> foundStrings = new ArrayList<String>();

		if (currNode != null) {
			String currData = Character.toString(currNode.data);

			if (currNode.termination) {
				foundStrings.add(currData);
				// System.out.println("terminating string");
			}
			for (String str : findAllStops(currNode.left)) {
				foundStrings.add(str);
			}
			for (String str : findAllStops(currNode.right)) {
				foundStrings.add(str);
			}
			for (String str : findAllStops(currNode.equal)) {
				foundStrings.add(currData + str);
			}
		}

		return foundStrings;
	}

}