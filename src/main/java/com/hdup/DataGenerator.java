package com.hdup;

import java.util.Random;

/**
 * @author Vadim Bobrov
 */
public class DataGenerator {

    private static final String[] CUSTOMERS = new String[20];
    private static final String[] LOCATIONS = new String[] {"location0", "location1"};
    private static final String[] WIREIDS = new String[300];

    static {
        for (int i = 0; i < 20; i ++)
            CUSTOMERS[i] = "customer" + i;
        for (int i = 0; i < 300; i ++)
            WIREIDS[i] = "wire" + i;
    }

    private final Random random = new Random();

    public String getRandomCustomer() {
        return CUSTOMERS[random.nextInt(20)];
    }

    public String getCustomer(int i) {
        return CUSTOMERS[i];
    }

    public String getRandomLocation() {
        return LOCATIONS[random.nextInt(2)];
    }

    public String getLocation(int i) {
        return LOCATIONS[i];
    }

    public String getRandomWireId() {
        return WIREIDS[random.nextInt(300)];
    }

    public String getWireId(int i) {
        return WIREIDS[i];
    }

    public long getRandomMeasurement() {
        return random.nextLong();
    }


}
