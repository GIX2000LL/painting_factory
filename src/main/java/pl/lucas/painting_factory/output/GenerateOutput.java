package pl.lucas.painting_factory.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.lucas.painting_factory.model.Vehicle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenerateOutput {

    public void generateOutputFile(List<Vehicle> originalVehicles, List<List<Vehicle>> sortedByDays, String chosenStrategy) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Tworzenie struktury danych do zapisania
        OutputData outputData = new OutputData(originalVehicles, addDayInfoToSortedVehicles(sortedByDays), extractFirstStrategy(chosenStrategy));

        // Ścieżka do folderu output w resources
        String outputPath = "src/main/resources/output/output.json";

        // Tworzenie folderu, jeśli nie istnieje
        File outputDir = new File("src/main/resources/output");
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }

        // Zapis do pliku JSON
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), outputData);

        // Informacja o utworzeniu pliku
        System.out.println("Output file created at: " + outputPath);
    }

    private String extractFirstStrategy(String chosenStrategy) {
        if (chosenStrategy.startsWith("Time is the same")) {
            return chosenStrategy.split(": ")[1].split(", ")[0];
        }
        return chosenStrategy;
    }

    private List<DayVehicles> addDayInfoToSortedVehicles(List<List<Vehicle>> sortedByDays) {
        List<DayVehicles> dayVehiclesList = new ArrayList<>();
        for (int i = 0; i < sortedByDays.size(); i++) {
            dayVehiclesList.add(new DayVehicles("Day " + (i + 1), sortedByDays.get(i)));
        }
        return dayVehiclesList;
    }

    // Klasa pomocnicza do przechowywania danych
    public static class OutputData {
        private List<Vehicle> originalVehicles;
        private List<DayVehicles> sortedByDays;
        private String chosenStrategy;

        public OutputData(List<Vehicle> originalVehicles, List<DayVehicles> sortedByDays, String chosenStrategy) {
            this.originalVehicles = originalVehicles;
            this.sortedByDays = sortedByDays;
            this.chosenStrategy = chosenStrategy;
        }

        public List<Vehicle> getOriginalVehicles() {
            return originalVehicles;
        }

        public List<DayVehicles> getSortedByDays() {
            return sortedByDays;
        }

        public String getChosenStrategy() {
            return chosenStrategy;
        }
    }

    // Klasa pomocnicza do przechowywania pojazdów z informacją o dniu
    public static class DayVehicles {
        private String day;
        private List<Vehicle> vehicles;

        public DayVehicles(String day, List<Vehicle> vehicles) {
            this.day = day;
            this.vehicles = vehicles;
        }

        public String getDay() {
            return day;
        }

        public List<Vehicle> getVehicles() {
            return vehicles;
        }
    }
}