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

                // Odczytanie pojazdów z pliku
                originalVehicles = readVehiclesFromFile(lastUploadedFilePath);

                // Resetowanie listy posortowanych pojazdów
                sortedVehicles = null;

                // Aktualizacja modelu
                model.addAttribute("vehicles", originalVehicles);
                model.addAttribute("sortedVehicles", sortedVehicles);
                model.addAttribute("totalTime", 0);
                model.addAttribute("numberOfDays", 0);
                model.addAttribute("sortedBy", "None");
                model.addAttribute("strategyTime", 0);
                model.addAttribute("strategyDays", 0);

            } catch (IOException e) {
                e.printStackTrace();
                model.addAttribute("error", "Błąd podczas odczytu pliku.");
            }
        } else {
            model.addAttribute("error", "Nie wybrano pliku lub plik jest pusty.");
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

            TimeCalculator timeCalculator = new TimeCalculator();

            // Obsługa przypadku, gdy czas jest taki sam dla wielu strategii
            if (bestStrategyName.startsWith("Time is the same")) {
                String firstStrategyName = bestStrategyName.split(": ")[1].split(", ")[0];
                SortingStrategy bestStrategy = selector.getStrategies().stream()
                        .filter(strategy -> strategy.getClass().getSimpleName().equals(firstStrategyName))
                        .findFirst()
                        .orElseThrow(() -> new IllegalStateException("Strategy not found: " + firstStrategyName));

                sortedVehicles = bestStrategy.sort(new ArrayList<>(originalVehicles)); // Sortowanie na kopii

                int totalTimeForStrategy = timeCalculator.calculateTotalPaintingTime(sortedVehicles);
                int numberOfDaysForStrategy = timeCalculator.calculateNumberOfDays(totalTimeForStrategy);

                // Dodanie atrybutów do modelu
                model.addAttribute("sortedBy", bestStrategyName);
                model.addAttribute("vehicles", originalVehicles); // Oryginalna lista
                model.addAttribute("sortedVehicles", sortedVehicles); // Posortowana lista
                model.addAttribute("strategyTime", totalTimeForStrategy);
                model.addAttribute("strategyDays", numberOfDaysForStrategy);

                // Obliczanie całkowitego czasu i dni dla oryginalnej listy
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

            sortedVehicles = bestStrategy.sort(new ArrayList<>(originalVehicles)); // Sortowanie na kopii

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
