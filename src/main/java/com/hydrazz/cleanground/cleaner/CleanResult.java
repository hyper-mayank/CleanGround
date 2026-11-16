package com.hydrazz.cleanground.cleaner;

public class CleanResult {

    private int itemsRemoved;
    private int mobsCulled;
    private int vehiclesRemoved;
    private long durationMillis;

    public CleanResult() {
        this.itemsRemoved = 0;
        this.mobsCulled = 0;
        this.vehiclesRemoved = 0;
        this.durationMillis = 0;
    }

    public synchronized void addItems(int count) {
        this.itemsRemoved += count;
    }

    public synchronized void addMobs(int count) {
        this.mobsCulled += count;
    }

    public synchronized void addVehicles(int count) {
        this.vehiclesRemoved += count;
    }

    public void setDurationMillis(long durationMillis) {
        this.durationMillis = durationMillis;
    }

    public int getItemsRemoved() {
        return itemsRemoved;
    }

    public int getMobsCulled() {
        return mobsCulled;
    }

    public int getVehiclesRemoved() {
        return vehiclesRemoved;
    }

    public long getDurationMillis() {
        return durationMillis;
    }

    public int getTotalRemoved() {
        return itemsRemoved + mobsCulled + vehiclesRemoved;
    }
}
