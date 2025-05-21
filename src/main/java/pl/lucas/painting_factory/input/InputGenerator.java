package pl.lucas.painting_factory.input;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import pl.lucas.painting_factory.model.Vehicle;
import pl.lucas.painting_factory.repository.VehicleRepository;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
public class InputGenerator {

    private final VehicleRepository vehicleRepository;

    public InputGenerator(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public void generateInput() throws IOException {

        List<Vehicle> allVehicles = vehicleRepository.findAll();

        Random random = new Random();
        int numberOfVehicles = random.nextInt(allVehicles.size()) + 1; // At least 1 vehicle
        Collections.shuffle(allVehicles);
        List<Vehicle> selectedVehicles = allVehicles.subList(0, numberOfVehicles);

        ObjectMapper objectMapper = new ObjectMapper();
        File outputFile = new File("src/main/resources/input/input.json");
        objectMapper.writeValue(outputFile, selectedVehicles);

        System.out.println("Input data saved to: " + outputFile.getAbsolutePath() + " with " + numberOfVehicles + " vehicles.");
    }
}
