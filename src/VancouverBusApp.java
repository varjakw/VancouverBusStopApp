import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class VancouverBusApp {
	private static final boolean PRINT_ERRORS = false;
	private static final DateTimeFormatter STOP_TIME_EXPECTED_TIME_FORMAT = DateTimeFormatter.ofPattern("H:m:s");
	private static final String WINDOW_TITLE = "Vancouver Bus Stops";
	private static final String DEFAULT_STOPS_FILENAME = "stops.txt";
	private static final String DEFAULT_STOP_TIMES_FILENAME = "stop_times.txt";
	private static final String DEFAULT_STOP_TRANSFERS_FILENAME = "transfers.txt";
	private static final int OUTPUT_AREA_WIDTH = 640;
	private static final int OUTPUT_AREA_HEIGHT = 480;

	// Loaded structured data
	private BusStops stops;

	// GUI widgets
	private JFrame mainFrame;
	private JPanel inputPanel;
	private JPanel outputPanel;
	private JLabel titleLabel;
	private JLabel searchNameLabel;
	private JLabel searchTimeLabel;
	private JLabel searchTimeHourLabel;
	private JLabel searchTimeMinuteLabel;
	private JLabel searchTimeSecondLabel;
	private JLabel shortestPathLabel;
	private JLabel shortestPathToLabel;
	private JLabel importedDataLabel;
	private JTextField searchNameTextField;
	private JFormattedTextField searchTimeHourTextField;
	private JFormattedTextField searchTimeMinuteTextField;
	private JFormattedTextField searchTimeSecondTextField;
	private JFormattedTextField shortestPathStartTextField;
	private JFormattedTextField shortestPathEndTextField;
	private JButton searchNameButton;
	private JButton searchTimeButton;
	private JButton shortestPathButton;
	private JButton importDataButton;
	private JButton clearOutputButton;
	private JTextArea outputTextArea;
	private JScrollPane outputTextScrollPane;

	// Constructor
	public VancouverBusApp() {
		stops = new BusStops();
	}

	// Start the application
	public void start() {

		// javax.swing.UIManager.put("OptionPane.messageFont", new Font("Segoe UI",
		// Font.PLAIN, 20));

		// Setup the window
		createWidgets();
		configureWidgets();
		setupLayout();
		registerButtonCallbacks();

		// Prompt user if they want to attempt automatic file loading
		int choice = JOptionPane.showConfirmDialog(null,
				"Attempt to import files using the default file names?\n" + DEFAULT_STOPS_FILENAME + ", "
						+ DEFAULT_STOP_TIMES_FILENAME + " and " + DEFAULT_STOP_TRANSFERS_FILENAME
						+ " would be expected to be in your current working directory.\n"
						+ "Otherwise, you can manually choose which files to import later.",
				"Automatic data import?", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);
		switch (choice) {
		case 0:
			loadDefaultFiles();
			break;
		case 1:
			break;
		default:
			mainFrame.dispose();
			return;
		}

		// Force the user to import bus stops if none have already been imported
		try {
			if (stops.getAll().size() == 0)
				forceUserToImportBusStops();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e, "No bus stops imported!", JOptionPane.ERROR_MESSAGE, null);
			mainFrame.dispose();
			return;
		}

		// Show main window
		mainFrame.setVisible(true);
	}

	// Attempt to load data from the default file names
	private void loadDefaultFiles() {

		// Attempt to load stops - This must be successful, otherwise error out
		try {
			stops.addStops(BusFileParser.LoadStops(new File(DEFAULT_STOPS_FILENAME), PRINT_ERRORS));
			if (stops.getAll().size() == 0)
				throw new IOException("No bus stops could be imported from " + DEFAULT_STOPS_FILENAME + "!");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e, "No bus stops imported!", JOptionPane.ERROR_MESSAGE, null);
			return;
		}

		// Attempt to load times - Display warning if unsuccessful
		try {
			int timesLoaded = BusFileParser.LoadStopTimes(stops, new File(DEFAULT_STOP_TIMES_FILENAME), PRINT_ERRORS);
			if (timesLoaded == 0)
				throw new IOException("No bus stop times could be imported from " + DEFAULT_STOP_TIMES_FILENAME + "!");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e, "No bus stop times data imported!", JOptionPane.WARNING_MESSAGE,
					null);
		}

		// Attempt to load transfers - Display warning if unsuccessful
		try {
			int transfersLoaded = BusFileParser.LoadStopTransfers(stops, new File(DEFAULT_STOP_TRANSFERS_FILENAME),
					PRINT_ERRORS);
			if (transfersLoaded == 0)
				throw new IOException(
						"No bus stop transfers could be imported from " + DEFAULT_STOP_TRANSFERS_FILENAME + "!");
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, e, "No bus stop transfers data imported!", JOptionPane.WARNING_MESSAGE,
					null);
		}

		// New data may have been loaded - Refresh counters and such
		updateImportedData();
	}

	// Prompt user for required stops input data
	private void forceUserToImportBusStops() throws IOException {
		JOptionPane.showMessageDialog(null,
				"Before you can continue, you must at least import the bus stops you will be querying.\nClick OK to select a file to import.",
				"Please import bus stop data", JOptionPane.INFORMATION_MESSAGE, null);
		BusFileParser.ImportStopsFile(stops, PRINT_ERRORS);
		if (stops.getAll().size() == 0)
			throw new IOException(
					"No bus stops were successfully imported! Bus stop data must be imported before this application can be used.");
	}

	// Initialize window widgets
	private void createWidgets() {

		// Main window
		mainFrame = new JFrame(WINDOW_TITLE);
		mainFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		mainFrame.setResizable(false);

		// Panels
		inputPanel = new JPanel();
		outputPanel = new JPanel();
		mainFrame.add(inputPanel);
		mainFrame.add(outputPanel);

		// Input formatting
		NumberFormat integerFieldFormatter = NumberFormat.getIntegerInstance();
		integerFieldFormatter.setGroupingUsed(false);

		// Create window widgets
		titleLabel = new JLabel(WINDOW_TITLE);
		searchNameLabel = new JLabel("Search Stops for Name");
		searchTimeLabel = new JLabel("Search Trips for Arrival Time");
		searchTimeHourLabel = new JLabel("h");
		searchTimeMinuteLabel = new JLabel("m");
		searchTimeSecondLabel = new JLabel("s");
		shortestPathLabel = new JLabel("Find Shortest Path Between Stops");
		shortestPathToLabel = new JLabel("To");
		importedDataLabel = new JLabel("No bus times or transfers imported yet!");
		searchNameTextField = new JTextField();
		searchTimeHourTextField = new JFormattedTextField(integerFieldFormatter); // TODO: 0-23?
		searchTimeMinuteTextField = new JFormattedTextField(integerFieldFormatter); // TODO: 0-59?
		searchTimeSecondTextField = new JFormattedTextField(integerFieldFormatter); // TODO: 0-59?
		shortestPathStartTextField = new JFormattedTextField(integerFieldFormatter);
		shortestPathEndTextField = new JFormattedTextField(integerFieldFormatter);
		searchNameButton = new JButton("Go");
		searchTimeButton = new JButton("Go");
		shortestPathButton = new JButton("Go");
		importDataButton = new JButton("Import Data");
		clearOutputButton = new JButton("Clear Output");
		outputTextArea = new JTextArea("Welcome to " + WINDOW_TITLE + "!\n");
	}

	private void configureWidgets() {

		// Set the starting values for integer text fields
		searchTimeHourTextField.setValue(12);
		searchTimeMinuteTextField.setValue(30);
		searchTimeSecondTextField.setValue(0);
		shortestPathStartTextField.setValue(0);
		shortestPathEndTextField.setValue(0);

		// Place the output text area in a scroll pane
		outputTextArea.setEditable(false);
		outputTextScrollPane = new JScrollPane(outputTextArea);
		outputTextScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		outputTextScrollPane.setPreferredSize(new Dimension(OUTPUT_AREA_WIDTH, OUTPUT_AREA_HEIGHT));

		// All inputs start disabled except the import button
		searchNameTextField.setEnabled(false);
		searchTimeHourTextField.setEnabled(false);
		searchTimeMinuteTextField.setEnabled(false);
		searchTimeSecondTextField.setEnabled(false);
		shortestPathStartTextField.setEnabled(false);
		shortestPathEndTextField.setEnabled(false);
		searchTimeButton.setEnabled(false);
		shortestPathButton.setEnabled(false);
		searchNameButton.setEnabled(false);
		searchTimeButton.setEnabled(false);
		shortestPathButton.setEnabled(false);
	}

	private void setupLayout() {

		// Main frame layout
		GroupLayout mainFrameLayout = new GroupLayout(mainFrame.getContentPane());
		mainFrameLayout.setAutoCreateGaps(true);
		mainFrameLayout.setAutoCreateContainerGaps(true);
		mainFrame.setLayout(mainFrameLayout);
		// Horizontal order of widgets
		mainFrameLayout.setHorizontalGroup(
				mainFrameLayout.createSequentialGroup().addComponent(inputPanel).addComponent(outputPanel));
		// Vertical order of widgets
		mainFrameLayout.setVerticalGroup(
				mainFrameLayout.createParallelGroup().addComponent(inputPanel).addComponent(outputPanel));

		// Input panel layout
		GroupLayout inputPanelLayout = new GroupLayout(inputPanel);
		inputPanelLayout.setAutoCreateGaps(true);
		inputPanelLayout.setAutoCreateContainerGaps(true);
		inputPanel.setLayout(inputPanelLayout);
		// Horizontal order of widgets
		inputPanelLayout.setHorizontalGroup(inputPanelLayout.createSequentialGroup().addGroup(inputPanelLayout
				.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(titleLabel)
				.addGroup(inputPanelLayout
						.createSequentialGroup().addComponent(importDataButton).addComponent(importedDataLabel))
				.addComponent(searchNameLabel)
				.addGroup(inputPanelLayout
						.createSequentialGroup().addComponent(searchNameTextField).addComponent(searchNameButton))
				.addComponent(searchTimeLabel)
				.addGroup(inputPanelLayout.createSequentialGroup().addComponent(searchTimeHourTextField)
						.addComponent(searchTimeHourLabel).addComponent(searchTimeMinuteTextField)
						.addComponent(searchTimeMinuteLabel).addComponent(searchTimeSecondTextField)
						.addComponent(searchTimeSecondLabel).addComponent(searchTimeButton))
				.addComponent(shortestPathLabel)
				.addGroup(inputPanelLayout.createSequentialGroup().addComponent(shortestPathStartTextField)
						.addComponent(shortestPathToLabel).addComponent(shortestPathEndTextField)
						.addComponent(shortestPathButton))
				.addComponent(clearOutputButton)));
		// Vertical order of widgets
		inputPanelLayout
				.setVerticalGroup(inputPanelLayout.createSequentialGroup().addComponent(titleLabel)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(importDataButton).addComponent(importedDataLabel))
						.addComponent(searchNameLabel)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(searchNameTextField).addComponent(searchNameButton))
						.addComponent(searchTimeLabel)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(searchTimeHourTextField).addComponent(searchTimeHourLabel)
								.addComponent(searchTimeMinuteTextField).addComponent(searchTimeMinuteLabel)
								.addComponent(searchTimeSecondTextField).addComponent(searchTimeSecondLabel)
								.addComponent(searchTimeButton))
						.addComponent(shortestPathLabel)
						.addGroup(inputPanelLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(shortestPathStartTextField).addComponent(shortestPathToLabel)
								.addComponent(shortestPathEndTextField).addComponent(shortestPathButton))
						.addComponent(clearOutputButton));

		// Output panel layout
		GroupLayout outputPanelLayout = new GroupLayout(outputPanel);
		outputPanelLayout.setAutoCreateGaps(true);
		outputPanelLayout.setAutoCreateContainerGaps(true);
		outputPanel.setLayout(outputPanelLayout);
		// Horizontal order of widgets
		outputPanelLayout
				.setHorizontalGroup(outputPanelLayout.createSequentialGroup().addComponent(outputTextScrollPane));
		// Vertical order of widgets
		outputPanelLayout
				.setVerticalGroup(outputPanelLayout.createSequentialGroup().addComponent(outputTextScrollPane));

		// Smush the window widgets together nicely
		mainFrame.pack();
	}

	// Add an anonymous action listener to each button
	private void registerButtonCallbacks() {
		searchNameButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				onSearchNameButtonPress();
			}
		});
		searchTimeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				onSearchTimeButtonPress();
			}
		});
		shortestPathButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				onFindShortestPathButtonPress();
			}
		});
		importDataButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				onImportDataButtonPress();
			}
		});
		clearOutputButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				outputTextArea.setText("");
			}
		});
	}

	private void updateImportedData() {

		// Update label with new counts of imported data
		List<BusStop> stopsList = stops.getAll();
		int stopsCount = stops.getAll().size();
		int tripsCount = 0;
		int transfersCount = 0;
		for (BusStop stop : stopsList) {
			tripsCount += stop.getTrips().size();
			transfersCount += stop.getTransfers().size();
		}
		importedDataLabel.setText(stopsCount + " stops, " + tripsCount + " trips, " + transfersCount + " transfers");

		// Enable inputs if there is now imported data available
		if (stopsCount != 0 && (tripsCount != 0 || transfersCount != 0)) {
			searchNameTextField.setEnabled(true);
			searchTimeHourTextField.setEnabled(true);
			searchTimeMinuteTextField.setEnabled(true);
			searchTimeSecondTextField.setEnabled(true);
			shortestPathStartTextField.setEnabled(true);
			shortestPathEndTextField.setEnabled(true);
			searchTimeButton.setEnabled(true);
			shortestPathButton.setEnabled(true);
			searchNameButton.setEnabled(true);
			searchTimeButton.setEnabled(true);
			shortestPathButton.setEnabled(true);
		}

		println("File import and parsing complete.");
	}

	// Add text to output and scroll to the bottom
	private void println(String text) {
		outputTextArea.append(text + "\n");
		outputTextArea.setCaretPosition(outputTextArea.getDocument().getLength());
	}

	// Search by name button handler
	private void onSearchNameButtonPress() {

		// handles case senitivity of search
		String search = (searchNameTextField.getText().toUpperCase());
		List<BusStop> foundStops = stops.getSearchResults(stops, search);

		// needing to get the internal map representation from BusStops, see FIXME

		String stopName = null;
		String stopDescription = null;
		int stopID = 0;
		int stopCode = 0;
		double stopLat = 0;
		double stopLong = 0;
		int stopLocation = 0;
		String stopZone = null;

		if (foundStops.isEmpty()) {
			println("No Search Results Found.");
		}

		for (BusStop stopIn : foundStops) {
			stopID = stopIn.getID();
			stopCode = stopIn.getCode();
			stopName = stopIn.getName();
			stopDescription = stopIn.getDescription();
			stopLat = stopIn.getLatitude();
			stopLong = stopIn.getLongitude();
			stopZone = stopIn.getZone();
			stopLocation = stopIn.getLocation();
			println(stopID + stopCode + stopName + stopDescription + stopLat + stopLong + stopZone + stopLocation);

		}

	}

	// Search by arrival time button handler
	private void onSearchTimeButtonPress() {

		// Parse the requested arrival time from GUI
		LocalTime requestedArrivalTime;
		try {
			String requestedArrivalTimeString = searchTimeHourTextField.getText() + ":"
					+ searchTimeMinuteTextField.getText() + ":" + searchTimeSecondTextField.getText();
			requestedArrivalTime = LocalTime.parse(requestedArrivalTimeString, STOP_TIME_EXPECTED_TIME_FORMAT);
		} catch (Exception e) {
			println("Invalid arrival time format! Please ensure the format is h::m:s.\n");
			return;
		}

		// Perform search
		println("Searching for trips with a stop which has an arrival time of " + requestedArrivalTime + "...\n");
		List<BusTrip> tripsFound = stops.getTripsWithArrivalTime(requestedArrivalTime);

		// Output results
		if (tripsFound.size() == 0)
			println("No trips found!");
		for (BusTrip trip : tripsFound)
			println("Trip #" + trip.getID() + ": Arrives into " + trip.getDestinationStop().getName());

	}

	// Find shortest path between stops button handler
	private void onFindShortestPathButtonPress() {

		// Attempt to find the stops with IDs equal to the users input
		String originStopIDInput, destinationStopIDInput;
		BusStop originStop, destinationStop;
		try {
			originStopIDInput = shortestPathStartTextField.getText();
			originStop = stops.get(Integer.parseInt(originStopIDInput));
			destinationStopIDInput = shortestPathEndTextField.getText();
			destinationStop = stops.get(Integer.parseInt(destinationStopIDInput));
		} catch (Exception e) {
			println("Invalid stop IDs!");
			return;
		}

		// Check if the stops exist
		if (originStop == null) {
			println("Stop #" + originStopIDInput + " not found!");
			return;
		} else if (destinationStop == null) {
			println("Stop #" + destinationStopIDInput + " not found!");
			return;
		}

		// Perform Dijkstra algorithm to find shortest path
		ShortestPath shortestPath;
		println("Searching for shortest path between " + originStop.getName() + " and " + destinationStop.getName()
				+ "...");
		try {
			shortestPath = BusShortestPathFinder.Dijkstra(originStop, destinationStop);
		} catch (Exception e) {
			println("No route found with between " + originStop.getName() + " and " + destinationStop.getName() + "!");
			return;
		}

		// Output results
		println("The following route was found with a total cost of " + shortestPath.getCost() + ":");
		List<BusStop> stops = shortestPath.getPath();
		for (int i = 0; i < stops.size(); i++)
			println(i + ". Stop #" + stops.get(i).getID() + ": " + stops.get(i).getName());
	}

	// Import more data button handler
	private void onImportDataButtonPress() {

		// Ask user which type to import
		String[] buttonTitles = new String[] { "Bus Stops", "Bus Times", "Bus Transfers" };
		int buttonChoice = JOptionPane.showOptionDialog(mainFrame, "What type of data would you like to import?",
				"Choose input type", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, buttonTitles,
				buttonTitles[0]);
		if (buttonChoice == JOptionPane.CLOSED_OPTION)
			return;

		// Execute relevant method
		switch (buttonChoice) {
		case 0: // Import stops
			println("Importing stops...");
			if (BusFileParser.ImportStopsFile(stops, PRINT_ERRORS) != 0)
				break;
			println("Import cancelled.");
			return;
		case 1: // Import stop times
			println("Importing stop times...");
			if (BusFileParser.ImportStopTimesFile(stops, PRINT_ERRORS) != 0)
				break;
			println("Import cancelled.");
			return;
		case 2: // Import stop transfers
			println("Importing stop transfers...");
			if (BusFileParser.ImportStopTransfersFile(stops, PRINT_ERRORS) != 0)
				break;
			println("Import cancelled.");
			return;
		default:
			return;
		}

		// New data has been loaded - Refresh counters and such
		updateImportedData();
	}
}