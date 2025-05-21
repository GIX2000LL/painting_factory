package pl.lucas.painting_factory.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import pl.lucas.painting_factory.input.InputGenerator;
import pl.lucas.painting_factory.logic.calculation.TimeCalculator;
import pl.lucas.painting_factory.logic.strategy.SortingStrategy;
import pl.lucas.painting_factory.logic.strategy.SortingStrategySelector;
import pl.lucas.painting_factory.logic.strategy.StrategyDetails;
import pl.lucas.painting_factory.model.Vehicle;
import pl.lucas.painting_factory.output.GenerateOutput;
import pl.lucas.painting_factory.service.FileService;
import pl.lucas.painting_factory.service.VehicleListService;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping ("/")
public class HomeController {

    private static final Logger logger = LoggerFactory.getLogger(HomeController.class);

    FileService fileService;
    VehicleListService vehicleListService;

    private final InputGenerator inputGenerator;
    private String lastUploadedFilePath = "src/main/resources/input/input.json";
    private List<Vehicle> originalVehicles; //
    private List<Vehicle> sortedVehicles; //
    private String sortedBy = "None"; //

    public HomeController(InputGenerator inputGenerator, FileService fileService, VehicleListService vehicleListService) {
        this.inputGenerator = inputGenerator;
        this.fileService = fileService;
        this.vehicleListService = vehicleListService;
    }

    @GetMapping
    public String home(Model model) {

        // cleaning up the original and sorted vehicles
        originalVehicles = null;
        sortedVehicles = null;

        // adding the default values to the model
        model.addAttribute("vehicles", null);
        model.addAttribute("sortedVehicles", sortedVehicles);
        resetModel(model);

        return "home";
    }

    @PostMapping("/generate")
    public String generateInput(Model model) {

        try {
            inputGenerator.generateInput();
            originalVehicles = fileService.readVehiclesFromFile("src/main/resources/input/input.json");
            model.addAttribute("vehicles", originalVehicles);

        } catch (IOException e) {
            logger.error("Error during generating input file: {}", e.getMessage(), e);
        }
        return "home";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {

        if (!file.isEmpty()) {
            try {
                Path path = Paths.get("src/main/resources/input/input" + file.getOriginalFilename());
                fileService.saveFile(file.getBytes(), path);
                lastUploadedFilePath = path.toString();

                System.out.println("Input file uploaded to: " + lastUploadedFilePath);

                originalVehicles = fileService.readVehiclesFromFile(lastUploadedFilePath);
                sortedVehicles = null;

                //model actualization
                model.addAttribute("vehicles", originalVehicles);
                model.addAttribute("sortedVehicles", sortedVehicles);
                resetModel(model);

            } catch (IOException e) {
                logger.error("Error during upload input file: {}", e.getMessage(), e);
                model.addAttribute("error", "Error during reading file.");
            }
        } else {
            model.addAttribute("error", "You don't chose a file or uploaded file is empty.");
        }
        return "home";
    }

    @PostMapping("/calculate")
    public String calculatePaintingTime(Model model) {
        try {
            if (originalVehicles == null) {
                originalVehicles = fileService.readVehiclesFromFile(lastUploadedFilePath);
            }
            TimeCalculator timeCalculator = new TimeCalculator();
            int totalTime = timeCalculator.calculateTotalPaintingTime(originalVehicles);
            int numberOfDays = timeCalculator.calculateNumberOfDays(totalTime);

            model.addAttribute("vehicles", originalVehicles);
            model.addAttribute("totalTime", totalTime);
            model.addAttribute("numberOfDays", numberOfDays);
        } catch (IOException e) {
            logger.error("Error during calculation of times from input file: {}", e.getMessage(), e);
        }
        return "home";
    }

    @PostMapping("/sort")
    public String sortVehicles(Model model) {
        try {
            if (originalVehicles == null) {
                originalVehicles = fileService.readVehiclesFromFile(lastUploadedFilePath);
            }

            // copy of original list
            List<Vehicle> vehiclesCopy = new ArrayList<>(originalVehicles);

            SortingStrategySelector selector = new SortingStrategySelector();
            String bestStrategyName = selector.selectBestStrategy(vehiclesCopy);

            this.sortedBy = bestStrategyName;
            model.addAttribute("sortedBy", bestStrategyName);

            List<StrategyDetails> strategyDetails = selector.getStrategyDetails(originalVehicles);
            model.addAttribute("strategyDetails", strategyDetails);

            TimeCalculator timeCalculator = new TimeCalculator();
            int workingDayMinutes = 8 * 60;

            // case when we have a lot of strategies with the same time
            if (bestStrategyName.startsWith("Time is the same")) {
                String firstStrategyName = bestStrategyName.split(": ")[1].split(", ")[0];
                SortingStrategy bestStrategy = selector.getStrategies().stream()
                        .filter(strategy -> strategy.getClass().getSimpleName().equals(firstStrategyName))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Strategy not found: " + firstStrategyName));

                sortedVehicles = bestStrategy.sort(new ArrayList<>(originalVehicles));

                int totalTimeForStrategy = timeCalculator.calculateTotalPaintingTime(sortedVehicles);
                int numberOfDaysForStrategy = timeCalculator.calculateNumberOfDays(totalTimeForStrategy);

                List<List<Vehicle>> sortedByDays = vehicleListService.splitVehiclesByDays(sortedVehicles, workingDayMinutes);
                List<List<Vehicle>> originalByDays = vehicleListService.splitVehiclesByDays(originalVehicles, workingDayMinutes);

                model.addAttribute("sortedBy", bestStrategyName);
                model.addAttribute("vehicles", originalVehicles);
                model.addAttribute("sortedVehicles", sortedVehicles);
                model.addAttribute("strategyTime", totalTimeForStrategy);
                model.addAttribute("strategyDays", numberOfDaysForStrategy);
                model.addAttribute("sortedByDays", sortedByDays);
                model.addAttribute("originalByDays", originalByDays);

                int totalTime = timeCalculator.calculateTotalPaintingTime(originalVehicles);
                int numberOfDays = timeCalculator.calculateNumberOfDays(totalTime);
                model.addAttribute("totalTime", totalTime);
                model.addAttribute("numberOfDays", numberOfDays);

                return "home";
            }

            SortingStrategy bestStrategy = selector.getStrategies().stream()
                    .filter(strategy -> strategy.getClass().getSimpleName().equals(bestStrategyName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Strategy not found: " + bestStrategyName));

            sortedVehicles = bestStrategy.sort(new ArrayList<>(originalVehicles));

            int totalTimeForStrategy = timeCalculator.calculateTotalPaintingTime(sortedVehicles);
            int numberOfDaysForStrategy = timeCalculator.calculateNumberOfDays(totalTimeForStrategy);

            List<List<Vehicle>> sortedByDays = vehicleListService.splitVehiclesByDays(sortedVehicles, workingDayMinutes);
            List<List<Vehicle>> originalByDays = vehicleListService.splitVehiclesByDays(originalVehicles, workingDayMinutes);

            model.addAttribute("vehicles", originalVehicles);
            model.addAttribute("sortedVehicles", sortedVehicles);
            model.addAttribute("sortedBy", bestStrategyName);
            model.addAttribute("totalTime", timeCalculator.calculateTotalPaintingTime(originalVehicles));
            model.addAttribute("numberOfDays", timeCalculator.calculateNumberOfDays(timeCalculator.calculateTotalPaintingTime(originalVehicles)));
            model.addAttribute("strategyTime", totalTimeForStrategy);
            model.addAttribute("strategyDays", numberOfDaysForStrategy);
            model.addAttribute("sortedByDays", sortedByDays);
            model.addAttribute("originalByDays", originalByDays);
        } catch (IOException e) {
            logger.error("Error during choose of optimal strategy {}", e.getMessage(), e);
        }
        return "home";
    }

    @PostMapping("/generate-output")
    public String generateOutput(Model model) {
        try {
            if (originalVehicles != null && sortedVehicles != null) {
                GenerateOutput generateOutput = new GenerateOutput();
                List<List<Vehicle>> sortedByDays = vehicleListService.splitVehiclesByDays(sortedVehicles, TimeCalculator.WORKING_DAY_MINUTES);

                SortingStrategySelector selector = new SortingStrategySelector();

                // case for multiply strategies with the same time
                String strategyToUse;
                if (sortedBy.startsWith("Time is the same")) {
                    String[] strategies = sortedBy.split(": ")[1].split(", ");
                    strategyToUse = strategies[0];
                } else {
                    strategyToUse = sortedBy;
                }

                List<StrategyDetails> allStrategyDetails = selector.getStrategyDetails(originalVehicles);
                StrategyDetails chosenStrategyDetails = allStrategyDetails.stream()
                        .filter(details -> details.getStrategyName().equals(strategyToUse))
                        .findFirst()
                        .orElse(null);

                if (chosenStrategyDetails == null) {
                    System.out.println("Chosen strategy details are null. Check if 'sortedBy' matches any strategy name.");
                }

                generateOutput.generateOutputFile(originalVehicles, sortedByDays, strategyToUse, chosenStrategyDetails);
                model.addAttribute("message", "Output file generated successfully!");
            } else {
                model.addAttribute("error", "Cannot generate output. Ensure vehicles are sorted first.");
            }
        } catch (IOException e) {
            logger.error("Error during output generation: {}", e.getMessage(), e);
            model.addAttribute("error", "Error generating output file.");
        }
        return "home";
    }

    private void resetModel(Model model) {
        model.addAttribute("totalTime", 0);
        model.addAttribute("numberOfDays", 0);
        model.addAttribute("sortedBy", "None");
        model.addAttribute("strategyTime", 0);
        model.addAttribute("strategyDays", 0);
    }
}
