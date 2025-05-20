package pl.lucas.painting_factory.logic.strategy;

public class StrategyDetails {
    private String strategyName;
    private int totalTime;
    private int numberOfDays;
    private int positionChangeDelay;
    private int suvTime;
    private int carTime;
    private int truckTime;
    private int colorChangeTime;
    private int initialColorLoadTime;

    public String getStrategyName() {
        return strategyName;
    }

    public void setStrategyName(String strategyName) {
        this.strategyName = strategyName;
    }

    public int getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(int totalTime) {
        this.totalTime = totalTime;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public void setNumberOfDays(int numberOfDays) {
        this.numberOfDays = numberOfDays;
    }

    public int getPositionChangeDelay() {
        return positionChangeDelay;
    }

    public void setPositionChangeDelay(int positionChangeDelay) {
        this.positionChangeDelay = positionChangeDelay;
    }

    public int getSuvTime() {
        return suvTime;
    }

    public void setSuvTime(int suvTime) {
        this.suvTime = suvTime;
    }

    public int getCarTime() {
        return carTime;
    }

    public void setCarTime(int carTime) {
        this.carTime = carTime;
    }

    public int getTruckTime() {
        return truckTime;
    }

    public void setTruckTime(int truckTime) {
        this.truckTime = truckTime;
    }

    public int getColorChangeTime() {
        return colorChangeTime;
    }

    public void setColorChangeTime(int colorChangeTime) {
        this.colorChangeTime = colorChangeTime;
    }

    public int getInitialColorLoadTime() {
        return initialColorLoadTime;
    }

    public void setInitialColorLoadTime(int initialColorLoadTime) {
        this.initialColorLoadTime = initialColorLoadTime;
    }
}