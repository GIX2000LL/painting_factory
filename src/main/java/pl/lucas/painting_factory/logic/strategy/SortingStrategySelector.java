package pl.lucas.painting_factory.logic.strategy;

import lombok.Getter;
import pl.lucas.painting_factory.logic.calculation.TimeCalculator;
import pl.lucas.painting_factory.model.Vehicle;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SortingStrategySelector {

    private static final int POSITION_CHANGE_DELAY = 5;
    private final List<SortingStrategy> strategies = new ArrayList<>();

    public SortingStrategySelector() {
        strategies.add(new PaintingTimeAscendingStrategy());
        strategies.add(new ColorNameAscendingStrategy());
        strategies.add(new ColorNameDescendingStrategy());
        strategies.add(new VehicleTypeAscendingStrategy());
        strategies.add(new VehicleTypeDescendingStrategy());
        strategies.add(new ColorGroupingStrategy());
        strategies.add(new PaintingTimeDescendingStrategy());
        strategies.add(new HybridStrategy());
    }

    public String selectBestStrategy(List<Vehicle> vehicles) {
        if (strategies.isEmpty()) {
            throw new IllegalStateException("No sorting strategies available.");
        }

        TimeCalculator timeCalculator = new TimeCalculator();
        List<String> bestStrategies = new ArrayList<>();
        int minDays = Integer.MAX_VALUE;
        int minTotalTime = Integer.MAX_VALUE;

        for (SortingStrategy strategy : strategies) {
            List<Vehicle> sortedVehicles = strategy.sort(new ArrayList<>(vehicles));
            int totalTime = timeCalculator.calculateTotalPaintingTime(sortedVehicles);
            int positionChangeDelay = calculatePositionChangeDelay(vehicles, sortedVehicles);
            totalTime += positionChangeDelay;

            int numberOfDays = timeCalculator.calculateNumberOfDays(totalTime);

            if (numberOfDays < minDays || (numberOfDays == minDays && totalTime < minTotalTime)) {
                minDays = numberOfDays;
                minTotalTime = totalTime;
                bestStrategies.clear();
                bestStrategies.add(strategy.getClass().getSimpleName());
            } else if (numberOfDays == minDays && totalTime == minTotalTime) {
                bestStrategies.add(strategy.getClass().getSimpleName());
            }
        }

        if (bestStrategies.isEmpty()) {
            throw new IllegalStateException("No suitable sorting strategy found.");
        }

        if (bestStrategies.size() > 1) {
            System.out.println("Multiple optimal strategies found. Picking the first one: " + bestStrategies.get(0));
            return "Time is the same for all of these strategies: " + String.join(", ", bestStrategies);
        } else {
            return bestStrategies.get(0);
        }
    }

    private int calculatePositionChangeDelay(List<Vehicle> originalVehicles, List<Vehicle> sortedVehicles) {
        int delay = 0;
        for (int i = 0; i < originalVehicles.size(); i++) {
            if (!originalVehicles.get(i).equals(sortedVehicles.get(i))) {
                delay += POSITION_CHANGE_DELAY;
            }
        }
        return delay;
    }

    public List<StrategyDetails> getStrategyDetails(List<Vehicle> vehicles) {
        List<StrategyDetails> strategyDetailsList = new ArrayList<>();
        TimeCalculator timeCalculator = new TimeCalculator();

        for (SortingStrategy strategy : strategies) {
            List<Vehicle> sortedVehicles = strategy.sort(new ArrayList<>(vehicles));
            int totalTime = timeCalculator.calculateTotalPaintingTime(sortedVehicles);
            int positionChangeDelay = calculatePositionChangeDelay(vehicles, sortedVehicles);
            int numberOfDays = timeCalculator.calculateNumberOfDays(totalTime);

            StrategyDetails details = new StrategyDetails();
            details.setStrategyName(strategy.getClass().getSimpleName());
            details.setTotalTime(totalTime);
            details.setNumberOfDays(numberOfDays);
            details.setPositionChangeDelay(positionChangeDelay);
            details.setSuvTime(timeCalculator.calculatePaintingTimeForType(sortedVehicles, "SUV"));
            details.setCarTime(timeCalculator.calculatePaintingTimeForType(sortedVehicles, "CAR"));
            details.setTruckTime(timeCalculator.calculatePaintingTimeForType(sortedVehicles, "TRUCK"));
            details.setColorChangeTime(timeCalculator.calculateTotalColorChangeTime(sortedVehicles));
            details.setInitialColorLoadTime(TimeCalculator.INITIAL_COLOR_LOAD_TIME);

            strategyDetailsList.add(details);
        }

        return strategyDetailsList;
    }
}