package pl.lucas.painting_factory.service;

import org.springframework.stereotype.Service;
import pl.lucas.painting_factory.model.Vehicle;
import pl.lucas.painting_factory.repository.VehicleRepository;

import java.util.List;

@Service
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
}
