package pl.lucas.painting_factory.model;

import lombok.Getter;

@Getter
public enum VehicleType {
    SUV(10), CAR(5), TRUCK(20);

    private final int paintingTime;

    VehicleType(int paintingTime) {
        this.paintingTime = paintingTime;
    }

}

