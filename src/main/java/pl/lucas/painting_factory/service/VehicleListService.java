package pl.lucas.painting_factory.service;

import org.springframework.stereotype.Service;
import pl.lucas.painting_factory.model.Vehicle;
import java.util.ArrayList;
import java.util.List;

@Service
public class VehicleListService {

    public List<List<Vehicle>> splitVehiclesByDays(List<Vehicle> vehicles, int workingDayMinutes) {
        List<List<Vehicle>> days = new ArrayList<>();
        List<Vehicle> currentDay = new ArrayList<>();
        int currentDayTime = 0;

        for (Vehicle vehicle : vehicles) {
            int paintingTime = vehicle.getType().getPaintingTime();
            if (currentDayTime + paintingTime > workingDayMinutes) {
                days.add(currentDay);
                currentDay = new ArrayList<>();
                currentDayTime = 0;
            }
            currentDay.add(vehicle);
            currentDayTime += paintingTime;
        }

        if (!currentDay.isEmpty()) {
            days.add(currentDay);
        }

        return days;
    }
}
