package pl.lucas.painting_factory.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import pl.lucas.painting_factory.model.Vehicle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping ("/")
public class HomeController {

    private final InputGenerator inputGenerator;
    private String lastUploadedFilePath = "src/main/resources/input/input.json"; // domyślna ścieżka
    private List<Vehicle> originalVehicles; // Lista oryginalna
    private List<Vehicle> sortedVehicles; // Lista posortowana

    public HomeController(InputGenerator inputGenerator) {
        this.inputGenerator = inputGenerator;
    }

    @GetMapping
    public String home(Model model) {
        // Czyszczenie list
        originalVehicles = null;
        sortedVehicles = null;

        // Dodanie pustych wartości do modelu
        model.addAttribute("vehicles", originalVehicles);
        model.addAttribute("sortedVehicles", sortedVehicles);
        model.addAttribute("totalTime", 0);
        model.addAttribute("numberOfDays", 0);
        model.addAttribute("sortedBy", "None");
        model.addAttribute("strategyTime", 0);
        model.addAttribute("strategyDays", 0);

        return "home";
//        model.addAttribute("vehicles", originalVehicles); // Wyświetl oryginalną listę
//        model.addAttribute("sortedVehicles", sortedVehicles); // Wyświetl posortowaną listę
//        return "home";
    }

    @PostMapping("/generate")
    public String generateInput(Model model) {
        try {
            inputGenerator.generateInput();
            originalVehicles = readVehiclesFromFile("src/main/resources/input/input.json");
            model.addAttribute("vehicles", originalVehicles);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "home";
    }

    @PostMapping("/upload")
    public String uploadFile(@RequestParam("file") MultipartFile file, Model model) {
        if (!file.isEmpty()) {
            try {
                byte[] bytes = file.getBytes();
                Path path = Paths.get("src/main/resources/input/input" + file.getOriginalFilename());
                Files.write(path, bytes);
                lastUploadedFilePath = path.toString();

                originalVehicles = readVehiclesFromFile(lastUploadedFilePath);


                model.addAttribute("vehicles", originalVehicles);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "home";
    }

    @PostMapping("/calculate")
    public String calculatePaintingTime(Model model) {
        try {
            if (originalVehicles == null) {
                originalVehicles = readVehiclesFromFile(lastUploadedFilePath); // Jeśli brak danych, odczytaj z pliku
            }
            TimeCalculator timeCalculator = new TimeCalculator();
            int totalTime = timeCalculator.calculateTotalPaintingTime(originalVehicles);
            int numberOfDays = timeCalculator.calculateNumberOfDays(totalTime);

            model.addAttribute("vehicles", originalVehicles); // Wyświetl oryginalną listę
            model.addAttribute("totalTime", totalTime);
            model.addAttribute("numberOfDays", numberOfDays);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "home";
    }

    @PostMapping("/sort")
    public String sortVehicles(Model model) {
        try {
            if (originalVehicles == null) {
                originalVehicles = readVehiclesFromFile(lastUploadedFilePath);
            }

            // Tworzenie kopii oryginalnej listy
            List<Vehicle> vehiclesCopy = new ArrayList<>(originalVehicles);

            SortingStrategySelector selector = new SortingStrategySelector();
            String bestStrategyName = selector.selectBestStrategy(vehiclesCopy);

            // Obsługa przypadku, gdy czas jest taki sam dla wielu strategii
            if (bestStrategyName.startsWith("Time is the same")) {
                model.addAttribute("sortedBy", bestStrategyName);
                model.addAttribute("vehicles", originalVehicles); // Oryginalna lista
                model.addAttribute("sortedVehicles", new ArrayList<>()); // Pusta lista dla posortowanych pojazdów
                model.addAttribute("totalTime", 0);
                model.addAttribute("numberOfDays", 0);
                model.addAttribute("strategyTime", 0);
                model.addAttribute("strategyDays", 0);
                return "home";
            }

            // Wyszukiwanie strategii na podstawie nazwy
            SortingStrategy bestStrategy = selector.getStrategies().stream()
                    .filter(strategy -> strategy.getClass().getSimpleName().equals(bestStrategyName))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("Strategy not found: " + bestStrategyName));

            // Sortowanie pojazdów
            sortedVehicles = bestStrategy.sort(vehiclesCopy);

            // Obliczanie czasu dla strategii
            TimeCalculator timeCalculator = new TimeCalculator();
            int totalTimeForStrategy = timeCalculator.calculateTotalPaintingTime(sortedVehicles);
            int numberOfDaysForStrategy = timeCalculator.calculateNumberOfDays(totalTimeForStrategy);

            // Obliczanie całkowitego czasu i dni dla oryginalnej listy
            int totalTime = timeCalculator.calculateTotalPaintingTime(originalVehicles);
            int numberOfDays = timeCalculator.calculateNumberOfDays(totalTime);

            // Dodanie atrybutów do modelu
            model.addAttribute("vehicles", originalVehicles); // Oryginalna lista
            model.addAttribute("sortedVehicles", sortedVehicles); // Posortowana lista
            model.addAttribute("sortedBy", bestStrategyName);
            model.addAttribute("totalTime", totalTime);
            model.addAttribute("numberOfDays", numberOfDays);
            model.addAttribute("strategyTime", totalTimeForStrategy);
            model.addAttribute("strategyDays", numberOfDaysForStrategy);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "home";
    }

    private List<Vehicle> readVehiclesFromFile(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<List<Vehicle>>() {});
    }
}
