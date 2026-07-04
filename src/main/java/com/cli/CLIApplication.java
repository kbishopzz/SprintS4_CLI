package com.cli;

import com.model.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.List;
import java.util.Scanner;

@SpringBootApplication(scanBasePackages = "com")
@EntityScan("com.model")
@EnableJpaRepositories("com.repo")
public class CLIApplication implements CommandLineRunner {

    // ANSI Colors
    private static final String RESET = "\u001B[0m";
    private static final String BLUE = "\u001B[34m";
    private static final String GREEN = "\u001B[32m";
    private static final String RED = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    private final ApiClient apiClient = new ApiClient();

    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        SpringApplication.run(CLIApplication.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println(CYAN + "========================================" + RESET);
        System.out.println(CYAN + "   Welcome to Airport System CLI App    " + RESET);
        System.out.println(CYAN + "========================================" + RESET);

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            try {
                switch (choice) {
                    case "1":
                        managePassengers();
                        break;
                    case "2":
                        manageAirports();
                        break;
                    case "3":
                        manageCities();
                        break;
                    case "4":
                        managePlanes();
                        break;
                    case "5":
                        runDomainQueries();
                        break;
                    case "6":
                        running = false;
                        System.out.println(GREEN + "Exiting... Goodbye!" + RESET);
                        break;
                    default:
                        System.out.println(RED + "Invalid choice. Please select 1-6." + RESET);
                }
            } catch (Exception e) {
                System.out.println(RED + "Error: " + e.getMessage() + RESET);
            }
        }
    }

    private static void printMainMenu() {
        System.out.println("\n" + BLUE + "--- Main Menu ---" + RESET);
        System.out.println("1. Manage Passengers");
        System.out.println("2. Manage Airports");
        System.out.println("3. Manage Cities");
        System.out.println("4. Manage Planes");
        System.out.println("5. Sprint Questions");
        System.out.println("6. Exit");
        System.out.print("Select an option: ");
    }

    // ==========================================
    // PASSENGER MANAGEMENT
    // ==========================================
    private void managePassengers() throws Exception {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + CYAN + "--- Passenger Management ---" + RESET);
            System.out.println("1. List Passengers (Paginated)");
            System.out.println("2. Find Passenger by ID");
            System.out.println("3. Create Passenger");
            System.out.println("4. Update Passenger");
            System.out.println("5. Delete Passenger");
            System.out.println("6. Back to Main Menu");
            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("Enter page number (default 0): ");
                    String pStr = scanner.nextLine().trim();
                    int page = pStr.isEmpty() ? 0 : Integer.parseInt(pStr);
                    System.out.print("Enter page size (default 20): ");
                    String sStr = scanner.nextLine().trim();
                    int size = sStr.isEmpty() ? 20 : Integer.parseInt(sStr);
                    System.out.print("Enter sorting column and order (e.g. 'lastName,asc' or 'firstName,desc', press enter to skip): ");
                    String sort = scanner.nextLine().trim();
                    if (sort.isEmpty()) {
                        sort = null;
                    }

                    List<Passenger> passengers = apiClient.getPassengers(page, size, sort);
                    System.out.println(YELLOW + "Page info: page " + page + " size " + size + " (returned "
                            + passengers.size() + " items)" + RESET);
                    printPassengerTable(passengers);
                    break;
                case "2":
                    System.out.print("Enter Passenger ID: ");
                    Long id = Long.parseLong(scanner.nextLine().trim());
                    try {
                        Passenger passenger = apiClient.getPassengerById(id);
                        printPassengerDetails(passenger);
                    } catch (Exception e) {
                        System.out.println(RED + "Passenger not found." + RESET);
                    }
                    break;
                case "3":
                    System.out.print("Enter First Name: ");
                    String firstName = scanner.nextLine().trim();
                    System.out.print("Enter Last Name: ");
                    String lastName = scanner.nextLine().trim();
                    System.out.print("Enter Phone Number: ");
                    String phoneNumber = scanner.nextLine().trim();

                    Passenger newPassenger = new Passenger(firstName, lastName, phoneNumber);
                    Passenger createdP = apiClient.createPassenger(newPassenger);
                    System.out.println(GREEN + "Passenger created successfully!" + RESET);
                    printPassengerDetails(createdP);
                    break;
                case "4":
                    System.out.print("Enter Passenger ID to update: ");
                    Long updateId = Long.parseLong(scanner.nextLine().trim());
                    System.out.print("Enter New First Name: ");
                    String uFirstName = scanner.nextLine().trim();
                    System.out.print("Enter New Last Name: ");
                    String uLastName = scanner.nextLine().trim();
                    System.out.print("Enter New Phone Number: ");
                    String uPhoneNumber = scanner.nextLine().trim();

                    Passenger updatePassenger = new Passenger(uFirstName, uLastName, uPhoneNumber);
                    Passenger updatedP = apiClient.updatePassenger(updateId, updatePassenger);
                    System.out.println(GREEN + "Passenger updated successfully!" + RESET);
                    printPassengerDetails(updatedP);
                    break;
                case "5":
                    System.out.print("Enter Passenger ID to delete: ");
                    Long deleteId = Long.parseLong(scanner.nextLine().trim());
                    apiClient.deletePassenger(deleteId);
                    System.out.println(GREEN + "Passenger deleted successfully." + RESET);
                    break;
                case "6":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "Invalid choice." + RESET);
            }
        }
    }

    private void printPassengerTable(List<Passenger> passengers) {
        if (passengers.isEmpty()) {
            System.out.println(YELLOW + "No records found." + RESET);
            return;
        }
        System.out.println(
                CYAN + "| ID                   | FIRSTNAME            | LASTNAME             | PHONENUMBER          |"
                        + RESET);
        System.out.println(
                CYAN + "----------------------------------------------------------------------------------------------"
                        + RESET);
        for (Passenger p : passengers) {
            System.out.printf("| %-20d | %-20s | %-20s | %-20s |\n", p.getId(), p.getFirstName(), p.getLastName(),
                    p.getPhoneNumber());
        }
    }

    private void printPassengerDetails(Passenger p) {
        System.out.println(YELLOW + "Passenger Details:" + RESET);
        System.out.println("  ID: " + p.getId());
        System.out.println("  First Name: " + p.getFirstName());
        System.out.println("  Last Name: " + p.getLastName());
        System.out.println("  Phone Number: " + p.getPhoneNumber());
    }

    // ==========================================
    // AIRPORT MANAGEMENT
    // ==========================================
    private void manageAirports() throws Exception {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + CYAN + "--- Airport Management ---" + RESET);
            System.out.println("1. List All Airports");
            System.out.println("2. Find Airport by ID");
            System.out.println("3. Create Airport");
            System.out.println("4. Update Airport");
            System.out.println("5. Delete Airport");
            System.out.println("6. Back to Main Menu");
            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    List<Airport> airports = apiClient.getAllAirports();
                    printAirportTable(airports);
                    break;
                case "2":
                    System.out.print("Enter Airport ID: ");
                    Long id = Long.parseLong(scanner.nextLine().trim());
                    try {
                        Airport airport = apiClient.getAirport(id);
                        printAirportDetails(airport);
                    } catch (Exception e) {
                        System.out.println(RED + "Airport not found." + RESET);
                    }
                    break;
                case "3":
                    System.out.print("Enter Airport Name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Enter Airport Code (e.g. JFK): ");
                    String code = scanner.nextLine().trim();
                    System.out.print("Enter City ID (Optional, press enter to skip): ");
                    String cityIdStr = scanner.nextLine().trim();

                    Airport newAirport = new Airport(name, code);
                    if (!cityIdStr.isEmpty()) {
                        City city = apiClient.getCity(Long.parseLong(cityIdStr));
                        newAirport.setCity(city);
                    }

                    Airport createdA = apiClient.createAirport(newAirport);
                    System.out.println(GREEN + "Airport created successfully!" + RESET);
                    printAirportDetails(createdA);
                    break;
                case "4":
                    System.out.print("Enter Airport ID to update: ");
                    Long updateId = Long.parseLong(scanner.nextLine().trim());
                    System.out.print("Enter New Airport Name: ");
                    String uName = scanner.nextLine().trim();
                    System.out.print("Enter New Airport Code: ");
                    String uCode = scanner.nextLine().trim();
                    System.out.print("Enter New City ID (Optional, press enter to skip): ");
                    String uCityIdStr = scanner.nextLine().trim();

                    Airport updateAirport = new Airport(uName, uCode);
                    if (!uCityIdStr.isEmpty()) {
                        City city = apiClient.getCity(Long.parseLong(uCityIdStr));
                        updateAirport.setCity(city);
                    }

                    Airport updatedA = apiClient.updateAirport(updateId, updateAirport);
                    System.out.println(GREEN + "Airport updated successfully!" + RESET);
                    printAirportDetails(updatedA);
                    break;
                case "5":
                    System.out.print("Enter Airport ID to delete: ");
                    Long deleteId = Long.parseLong(scanner.nextLine().trim());
                    apiClient.deleteAirport(deleteId);
                    System.out.println(GREEN + "Airport deleted successfully." + RESET);
                    break;
                case "6":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "Invalid choice." + RESET);
            }
        }
    }

    private void printAirportTable(List<Airport> airports) {
        if (airports.isEmpty()) {
            System.out.println(YELLOW + "No records found." + RESET);
            return;
        }
        System.out.println(CYAN + "| ID                   | NAME                 | AIRPORTCODE          |" + RESET);
        System.out.println(CYAN + "-----------------------------------------------------------------------" + RESET);
        for (Airport a : airports) {
            System.out.printf("| %-20d | %-20s | %-20s |\n", a.getId(), a.getName(), a.getAirportCode());
        }
    }

    private void printAirportDetails(Airport a) {
        System.out.println(YELLOW + "Airport Details:" + RESET);
        System.out.println("  ID: " + a.getId());
        System.out.println("  Name: " + a.getName());
        System.out.println("  Airport Code: " + a.getAirportCode());
    }

    // ==========================================
    // CITY MANAGEMENT
    // ==========================================
    private void manageCities() throws Exception {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + CYAN + "--- City Management ---" + RESET);
            System.out.println("1. List Cities (Paginated)");
            System.out.println("2. Find City by ID");
            System.out.println("3. View Airports in City");
            System.out.println("4. Create City");
            System.out.println("5. Update City");
            System.out.println("6. Delete City");
            System.out.println("7. Back to Main Menu");
            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("Enter page number (default 0): ");
                    String pStr = scanner.nextLine().trim();
                    int page = pStr.isEmpty() ? 0 : Integer.parseInt(pStr);
                    System.out.print("Enter page size (default 20): ");
                    String sStr = scanner.nextLine().trim();
                    int size = sStr.isEmpty() ? 20 : Integer.parseInt(sStr);
                    System.out.print("Enter sorting column and order (e.g. 'name,asc' or 'population,desc', press enter to skip): ");
                    String sort = scanner.nextLine().trim();
                    if (sort.isEmpty()) {
                        sort = null;
                    }

                    List<City> cities = apiClient.getCities(page, size, sort);
                    System.out.println(YELLOW + "Page info: page " + page + " size " + size + " (returned "
                            + cities.size() + " items)" + RESET);
                    printCityTable(cities);
                    break;
                case "2":
                    System.out.print("Enter City ID: ");
                    Long id = Long.parseLong(scanner.nextLine().trim());
                    try {
                        City city = apiClient.getCity(id);
                        printCityDetails(city);
                    } catch (Exception e) {
                        System.out.println(RED + "City not found." + RESET);
                    }
                    break;
                case "3":
                    System.out.print("Enter City ID: ");
                    Long cId = Long.parseLong(scanner.nextLine().trim());
                    List<Airport> cityAirports = apiClient.getAirportsInCity(cId);
                    printAirportTable(cityAirports);
                    break;
                case "4":
                    System.out.print("Enter City Name: ");
                    String name = scanner.nextLine().trim();
                    System.out.print("Enter Province: ");
                    String province = scanner.nextLine().trim();
                    System.out.print("Enter Population: ");
                    int population = Integer.parseInt(scanner.nextLine().trim());

                    City newCity = new City(name, province, population);
                    City createdC = apiClient.createCity(newCity);
                    System.out.println(GREEN + "City created successfully!" + RESET);
                    printCityDetails(createdC);
                    break;
                case "5":
                    System.out.print("Enter City ID to update: ");
                    Long updateId = Long.parseLong(scanner.nextLine().trim());
                    System.out.print("Enter New City Name: ");
                    String uName = scanner.nextLine().trim();
                    System.out.print("Enter New Province: ");
                    String uProvince = scanner.nextLine().trim();
                    System.out.print("Enter New Population: ");
                    int uPopulation = Integer.parseInt(scanner.nextLine().trim());

                    City updateCity = new City(uName, uProvince, uPopulation);
                    City updatedC = apiClient.updateCity(updateId, updateCity);
                    System.out.println(GREEN + "City updated successfully!" + RESET);
                    printCityDetails(updatedC);
                    break;
                case "6":
                    System.out.print("Enter City ID to delete: ");
                    Long deleteId = Long.parseLong(scanner.nextLine().trim());
                    apiClient.deleteCity(deleteId);
                    System.out.println(GREEN + "City deleted successfully." + RESET);
                    break;
                case "7":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "Invalid choice." + RESET);
            }
        }
    }

    private void printCityTable(List<City> cities) {
        if (cities.isEmpty()) {
            System.out.println(YELLOW + "No records found." + RESET);
            return;
        }
        System.out.println(
                CYAN + "| ID                   | NAME                 | PROVINCE             | POPULATION           |"
                        + RESET);
        System.out.println(
                CYAN + "----------------------------------------------------------------------------------------------"
                        + RESET);
        for (City c : cities) {
            System.out.printf("| %-20d | %-20s | %-20s | %-20d |\n", c.getId(), c.getName(), c.getProvince(),
                    c.getPopulation());
        }
    }

    private void printCityDetails(City c) {
        System.out.println(YELLOW + "City Details:" + RESET);
        System.out.println("  ID: " + c.getId());
        System.out.println("  Name: " + c.getName());
        System.out.println("  Province: " + c.getProvince());
        System.out.println("  Population: " + c.getPopulation());
    }

    // ==========================================
    // PLANE MANAGEMENT
    // ==========================================
    private void managePlanes() throws Exception {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + CYAN + "--- Plane Management ---" + RESET);
            System.out.println("1. List All Planes");
            System.out.println("2. Find Plane by ID");
            System.out.println("3. Create Plane");
            System.out.println("4. Update Plane");
            System.out.println("5. Delete Plane");
            System.out.println("6. Back to Main Menu");
            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    List<Plane> planes = apiClient.getAllPlanes();
                    printPlaneTable(planes);
                    break;
                case "2":
                    System.out.print("Enter Plane ID: ");
                    Long id = Long.parseLong(scanner.nextLine().trim());
                    try {
                        Plane plane = apiClient.getPlaneById(id);
                        printPlaneDetails(plane);
                    } catch (Exception e) {
                        System.out.println(RED + "Plane not found." + RESET);
                    }
                    break;
                case "3":
                    System.out.print("Enter Plane Type: ");
                    String type = scanner.nextLine().trim();
                    System.out.print("Enter Airline Name: ");
                    String airline = scanner.nextLine().trim();
                    System.out.print("Enter Number of Passengers: ");
                    int num = Integer.parseInt(scanner.nextLine().trim());

                    Plane newPlane = new Plane();
                    newPlane.setType(type);
                    newPlane.setAirlineName(airline);
                    newPlane.setNumOfPassengers(num);

                    Plane createdPl = apiClient.createPlane(newPlane);
                    System.out.println(GREEN + "Plane created successfully!" + RESET);
                    printPlaneDetails(createdPl);
                    break;
                case "4":
                    System.out.print("Enter Plane ID to update: ");
                    Long updateId = Long.parseLong(scanner.nextLine().trim());
                    System.out.print("Enter New Plane Type: ");
                    String uType = scanner.nextLine().trim();
                    System.out.print("Enter New Airline Name: ");
                    String uAirline = scanner.nextLine().trim();
                    System.out.print("Enter New Number of Passengers: ");
                    int uNum = Integer.parseInt(scanner.nextLine().trim());

                    Plane updatePlane = new Plane();
                    updatePlane.setType(uType);
                    updatePlane.setAirlineName(uAirline);
                    updatePlane.setNumOfPassengers(uNum);

                    Plane updatedPl = apiClient.updatePlane(updateId, updatePlane);
                    if (updatedPl != null) {
                        System.out.println(GREEN + "Plane updated successfully!" + RESET);
                        printPlaneDetails(updatedPl);
                    } else {
                        System.out.println(RED + "Plane not found." + RESET);
                    }
                    break;
                case "5":
                    System.out.print("Enter Plane ID to delete: ");
                    Long deleteId = Long.parseLong(scanner.nextLine().trim());
                    apiClient.deletePlane(deleteId);
                    System.out.println(GREEN + "Plane deleted successfully." + RESET);
                    break;
                case "6":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "Invalid choice." + RESET);
            }
        }
    }

    private void printPlaneTable(List<Plane> planes) {
        if (planes.isEmpty()) {
            System.out.println(YELLOW + "No records found." + RESET);
            return;
        }
        System.out.println(
                CYAN + "| ID                   | TYPE                 | AIRLINENAME          | NUMOFPASSENGERS      |"
                        + RESET);
        System.out.println(
                CYAN + "----------------------------------------------------------------------------------------------"
                        + RESET);
        for (Plane pl : planes) {
            System.out.printf("| %-20d | %-20s | %-20s | %-20d |\n", pl.getID(), pl.getType(), pl.getAirlineName(),
                    pl.getNumOfPassengers());
        }
    }

    private void printPlaneDetails(Plane pl) {
        System.out.println(YELLOW + "Plane Details:" + RESET);
        System.out.println("  ID: " + pl.getID());
        System.out.println("  Type: " + pl.getType());
        System.out.println("  Airline Name: " + pl.getAirlineName());
        System.out.println("  Number of Passengers: " + pl.getNumOfPassengers());
    }

    private void runDomainQueries() throws Exception {
        boolean back = false;
        while (!back) {
            System.out.println("\n" + CYAN + "--- Sprint Questions Menu ---" + RESET);
            System.out.println("1. List Airports in a City");
            System.out.println("2. List Planes for a Passenger");
            System.out.println("3. List Airports for a Plane");
            System.out.println("4. List Airports used by a Passenger");
            System.out.println("5. Back to Main Menu");
            System.out.print("Select an option: ");
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1":
                    System.out.print("Enter City ID: ");
                    Long cityId = Long.parseLong(scanner.nextLine().trim());
                    List<Airport> cityAirports = apiClient.getAirportsInCity(cityId);
                    printAirportTable(cityAirports);
                    break;
                case "2":
                    System.out.print("Enter Passenger ID: ");
                    Long passengerId = Long.parseLong(scanner.nextLine().trim());
                    List<Plane> passengerPlanes = apiClient.getPlanesForPassenger(passengerId);
                    printPlaneTable(passengerPlanes);
                    break;
                case "3":
                    System.out.print("Enter Plane ID: ");
                    Long planeId = Long.parseLong(scanner.nextLine().trim());
                    List<Airport> planeAirports = apiClient.getAirportsForPlane(planeId);
                    printAirportTable(planeAirports);
                    break;
                case "4":
                    System.out.print("Enter Passenger ID: ");
                    Long passengerAirportId = Long.parseLong(scanner.nextLine().trim());
                    List<Airport> passengerAirports = apiClient.getAirportsForPassenger(passengerAirportId);
                    printAirportTable(passengerAirports);
                    break;
                case "5":
                    back = true;
                    break;
                default:
                    System.out.println(RED + "Invalid choice. Please select 1-5." + RESET);
            }
        }
    }
}
