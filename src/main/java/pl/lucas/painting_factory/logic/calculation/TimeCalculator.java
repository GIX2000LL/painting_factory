package pl.lucas.painting_factory.logic.calculation;

import pl.lucas.painting_factory.model.Vehicle;

import java.util.List;

public class TimeCalculator {

    private static final int SUV_PAINTING_TIME = 10; // minutes
    private static final int REGULAR_CAR_PAINTING_TIME = 5; // minutes
    private static final int TRUCK_PAINTING_TIME = 20; // minutes
    private static final int COLOR_CHANGE_TIME = 20; // minutes
    private static final int INITIAL_COLOR_LOAD_TIME = 10; // minutes
    public static final int WORKING_DAY_MINUTES = 8 * 60; // 8 hours in minutes

    public int calculateTotalPaintingTime(List<Vehicle> vehicles) {
        int totalTime = INITIAL_COLOR_LOAD_TIME;
        String currentColor = null;

        for (Vehicle vehicle : vehicles) {
            if (currentColor == null || !currentColor.equals(vehicle.getAssignedColor())) {
                totalTime += COLOR_CHANGE_TIME;
                currentColor = vehicle.getAssignedColor();
            }

            switch (vehicle.getType().toString()) {
                case "SUV":
                    totalTime += SUV_PAINTING_TIME;
                    break;
                case "CAR":
                    totalTime += REGULAR_CAR_PAINTING_TIME;
                    break;
                case "TRUCK":
                    totalTime += TRUCK_PAINTING_TIME;
                    break;
                default:
                    throw new IllegalArgumentException("Unknown vehicle type: " + vehicle.getType());
            }
        }

        return totalTime;
    }

    public boolean exceedsWorkingDay(int totalTime) {
        return totalTime > WORKING_DAY_MINUTES;
    }

    public int calculateNumberOfDays(int totalTime) {
        int fullDays = totalTime / WORKING_DAY_MINUTES;
        int remainingMinutes = totalTime % WORKING_DAY_MINUTES;
        return remainingMinutes > 0 ? fullDays + 1 : fullDays;
    }
}