package pl.lucas.painting_factory.output;

import com.fasterxml.jackson.databind.ObjectMapper;
import pl.lucas.painting_factory.logic.strategy.StrategyDetails;
import pl.lucas.painting_factory.model.Vehicle;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GenerateOutput {

    public void generateOutputFile(List<Vehicle> originalVehicles, List<List<Vehicle>> sortedByDays, String chosenStrategy, StrategyDetails strategyDetails) throws IOException {

        ObjectMapper objectMapper = new ObjectMapper();
        OutputData outputData = new OutputData(originalVehicles, addDayInfoToSortedVehicles(sortedByDays), extractFirstStrategy(chosenStrategy), strategyDetails);

        String outputPath = "src/main/resources/output/output.json";
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(outputPath), outputData);

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


    public record OutputData(List<Vehicle> originalVehicles, List<DayVehicles> sortedByDays, String chosenStrategy,
                                 StrategyDetails strategyDetails) {

    }

    public record DayVehicles(String day, List<Vehicle> vehicles) {

    }
}