package com.hdup;

import java.io.IOException;

/**
 * @author Vadim Bobrov
 */
public class DatabaseFiller {

    private DataGenerator dataGenerator;
    private Writer writer;

    public DatabaseFiller() throws IOException {
        dataGenerator = new DataGenerator();
        writer = new Writer();
    }

    public void fillRandom(int records) throws IOException {
        for(int i = 0; i < records; i++) {
            writer.write(dataGenerator.getRandomCustomer(), dataGenerator.getRandomLocation(), dataGenerator.getRandomWireId(), (long)i, dataGenerator.getRandomMeasurement());
            if( i % 1000 == 0)
                System.out.println(i);
        }

        writer.close();
    }

    public void fill(String customer, String location, String wireid, int records) throws IOException {
        for(int i = 0; i < records; i++) {
            writer.write(customer, location, wireid, (long)i, 888L);
            if( i % 1000 == 0)
                System.out.println(i);
        }

        writer.close();
    }

}
