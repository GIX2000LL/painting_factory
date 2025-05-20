package pl.lucas.painting_factory.logic.strategy;

import pl.lucas.painting_factory.model.Vehicle;

import java.util.Comparator;
import java.util.List;

public class ColorNameAscendingStrategy implements SortingStrategy {
    @Override
    public List<Vehicle> sort(List<Vehicle> vehicles) {
        vehicles.sort(Comparator.comparing(Vehicle::getAssignedColor));
        return vehicles;
    }
}