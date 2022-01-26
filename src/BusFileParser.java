import java.awt.FileDialog;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class BusFileParser {
	private static final int BUS_STOP_EXPECTED_CSV_FIELD_COUNT = 10;
	private static final int TRANSFER_EXPECTED_CSV_FIELD_COUNT = 4;
	private static final int STOP_TIME_EXPECTED_CSV_FIELD_COUNT = 9;
	private static final DateTimeFormatter STOP_TIME_EXPECTED_TIME_FORMAT = DateTimeFormatter.ofPattern("H:m:s");

	// Let the user select a file before attempting to parse the file for bus stops
	public static int ImportStopsFile(BusStops stops, boolean printErrors) {
		return parseFile(stops, file -> {
			Map<Integer, BusStop> loadedStops = LoadStops(file, printErrors);
			stops.addStops(loadedStops);
			return loadedStops.size();
		});
	}

	// Let the user select a file before attempting to parse the file for bus stop
	// times
	public static int ImportStopTimesFile(BusStops stops, boolean printErrors) {
		return parseFile(stops, file -> LoadStopTimes(stops, file, printErrors));
	}

	// Let the user select a file before attempting to parse the file for bus stop
	// transfers
	public static int ImportStopTransfersFile(BusStops stops, boolean printErrors) {
		return parseFile(stops, file -> LoadStopTransfers(stops, file, printErrors));
	}

	// Load all bus stop data from a file and add to stops sorted by stop ID
	public static Map<Integer, BusStop> LoadStops(File file, boolean printErrors) throws IOException {

		// Read in every line from file
		String currentLine;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		Map<Integer, BusStop> stops = new HashMap<>();
		while ((currentLine = reader.readLine()) != null) {
			try {

				// Split the CSV line, remove whitespace, keep empty strings
				String[] data = currentLine.split("\\s*,\\s*", -1);
				if (data.length != BUS_STOP_EXPECTED_CSV_FIELD_COUNT)
					throw new IOException("Incorrect number of bus stop CSV fields!");

				// Parse and add data to stops
				int stopID = Integer.parseInt(data[0]);
				stops.put(stopID,
						new BusStop(stopID, data[1].isEmpty() ? 0 : Integer.parseInt(data[1]), data[2], data[3],
								Double.parseDouble(data[4]), Double.parseDouble(data[5]), data[6], data[7],
								Integer.parseInt(data[8]), data[9]));

			} catch (Exception e) {
				if (printErrors)
					System.err.println(e);
			}
		}
		reader.close();

		return stops;
	}

	// Load all bus time data from a file and add to trips sorted by trip ID
	public static int LoadStopTimes(BusStops stops, File file, boolean printErrors) throws IOException {
		int stopTripsLoadedCount = 0;

		// Remember previous trip and stop parsed every iteration
		int previousTripID = -1;
		BusStop previousStop = null;

		// Read in every line from file
		String currentLine;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while ((currentLine = reader.readLine()) != null) {
			try {

				// Split the CSV line, remove whitespace, keep empty strings
				String[] data = currentLine.split("\\s*,\\s*", -1);
				if (data.length != STOP_TIME_EXPECTED_CSV_FIELD_COUNT)
					throw new IOException("Invalid number of stop time CSV fields!");

				// Parse the trip id and stop from this line
				int tripID = Integer.parseInt(data[0]);
				BusStop stop = stops.get(Integer.parseInt(data[3]));
				if (stop == null)
					throw new IOException("Stop #" + tripID + " not found!");

				// Only add a trip to stop from the previous stop if this is the same trip as
				// the last one
				if (tripID == previousTripID) {
					previousStop.addTrip(new BusTrip(tripID, LocalTime.parse(data[1], STOP_TIME_EXPECTED_TIME_FORMAT),
							LocalTime.parse(data[2], STOP_TIME_EXPECTED_TIME_FORMAT), previousStop, stop,
							data[4].isEmpty() ? 0 : Integer.parseInt(data[4]), data[5],
							data[6].isEmpty() ? 0 : Integer.parseInt(data[6]),
							data[7].isEmpty() ? 0 : Integer.parseInt(data[7]),
							data[8].isEmpty() ? 0 : Double.parseDouble(data[8])));
					stopTripsLoadedCount++;
				}

				// Remember previous trip and stop parsed
				previousTripID = tripID;
				previousStop = stop;

			} catch (Exception e) {
				if (printErrors)
					System.err.println(e);
			}
		}
		reader.close();

		return stopTripsLoadedCount;
	}

	// Load all bus stop transfers data from a file and add to stops
	public static int LoadStopTransfers(BusStops stops, File file, boolean printErrors) throws IOException {
		int stopTransfersLoadedCount = 0;

		// Read in every line from file
		String currentLine;
		BufferedReader reader = new BufferedReader(new FileReader(file));
		while ((currentLine = reader.readLine()) != null) {
			try {

				// Split the CSV line, remove whitespace, keep empty strings
				String[] data = currentLine.split("\\s*,\\s*", -1);
				if (data.length != TRANSFER_EXPECTED_CSV_FIELD_COUNT)
					throw new IOException("Incorrect number of transfer CSV fields!");

				// Parse stop IDs
				int originStopID = Integer.parseInt(data[0]);
				BusStop originStop = stops.get(originStopID);
				if (originStop == null)
					throw new IOException("Transfer origin stop #" + originStopID + " not found!");
				int destinationStopID = Integer.parseInt(data[1]);
				BusStop destinationStop = stops.get(Integer.parseInt(data[1]));
				if (destinationStop == null)
					throw new IOException("Transfer destination stop #" + destinationStopID + " not found!");

				// Parse and add transfer data to origin stop
				BusTransfer transfer = new BusTransfer(destinationStop, Integer.parseInt(data[2]),
						data[3].isEmpty() ? 0 : Double.parseDouble(data[3]));
				originStop.addTransfer(transfer);
				stopTransfersLoadedCount++;

			} catch (Exception e) {
				if (printErrors)
					System.err.println(e);
			}
		}
		reader.close();

		return stopTransfersLoadedCount;
	}

	// Handles common logic used by all file parsing methods
	private interface FileParser {
		public int parse(File file) throws IOException;
	}

	private static int parseFile(BusStops stops, FileParser parser) {

		// Keep attempting to import a file
		int entriesImportedCount = 0;
		while (entriesImportedCount == 0) {
			try {

				// Let the user choose a file
				File file = ChooseFile();
				if (file == null) {
					break; // Give up if none was selected
				}

				// Attempt to parse the file
				entriesImportedCount = parser.parse(file);
				if (entriesImportedCount == 0)
					throw new IOException("No valid entires could be parsed from " + file.getName() + "!");

				// Notify user of completion
				JOptionPane.showMessageDialog(null,
						"Successfully imported " + entriesImportedCount + " entries from " + file.getName() + "!",
						"Import complete", JOptionPane.INFORMATION_MESSAGE, null);

			} catch (IOException e) {
				JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE, null);
			}
		}

		return entriesImportedCount;
	}

	// A helper method to let the user pick a file using Java Swing
	private static File ChooseFile() {

		// Setup a new frame with a file picker in it
		JFrame frame = new JFrame();
		FileDialog fileDialog = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
		fileDialog.setDirectory(System.getProperty("user.dir"));
		fileDialog.setMultipleMode(false);

		// Wait for the user to pick a file
		fileDialog.setVisible(true);
		frame.dispose();

		// Return the file if valid, else return null
		String filename = fileDialog.getFile();
		return (filename == null) ? null : new File(fileDialog.getFile());
	}
}