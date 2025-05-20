package pl.lucas.painting_factory.logic.strategy;

import pl.lucas.painting_factory.model.Vehicle;

import java.util.List;

public interface SortingStrategy {
    List<Vehicle> sort(List<Vehicle> vehicles);
}