package com.outsmart;

import org.joda.time.DateTime;

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

    /**
     * fills the database with random crap
     * @param records
     * @throws IOException
     */
    public void fillRandom(int records) throws IOException {
        for(int i = 0; i < records; i++) {
            writer.write(dataGenerator.getRandomCustomer(), dataGenerator.getRandomLocation(), dataGenerator.getRandomWireId(), (long)i, dataGenerator.getRandomMeasurement());
            if( i % 1000 == 0)
                System.out.println(i);
        }

        writer.close();
    }

    /**
     * fill the database for specific customer, location and wire
     * @param customer
     * @param location
     * @param wireid
     * @param records
     * @throws IOException
     */
    public void fill(String customer, String location, String wireid, int records) throws IOException {
        for(int i = 0; i < records; i++) {
            writer.write(customer, location, wireid, (long)i, 888L);
            if( i % 1000 == 0)
                System.out.println(i);
        }

        writer.close();
    }

    /**
     * fill the database with data spread evenly among all customers, locations and wires
     * from start time to end time every 5 minutes
     * @param start start time
     * @param end end time
     */
    public void fillEven(DateTime start, DateTime end, long value) throws IOException {
        for(long l = start.getMillis(), count = 0; l < end.getMillis(); l += 300000, count++) {

            if( count % 100 == 0)
                System.out.println(count);

            for(int i = 0; i < 20; i++)
                for(int j = 0; j < 2; j++)
                    for(int k = 0; k < 300; k++)
                        writer.write(dataGenerator.getCustomer(i), dataGenerator.getLocation(j), dataGenerator.getWireId(k), l, value);
        }

        writer.close();
    }

}
