package nl.basjes.dsmr.simulator;

import org.junit.Test;

import static java.lang.Math.PI;

public class Experiments {

    @Test
    public void tryStuff() {

        double sinPeriodScaler = 60.0/(PI*2.0);

        long epochMillis = System.currentTimeMillis();
        for (long i = 0; i < 120; i++) {
            epochMillis++;

            double sin = Math.sin(epochMillis / sinPeriodScaler);
            System.out.println(i + "     " + String.format("%09.3f", 900 + (100. * sin)));
        }
    }
}
