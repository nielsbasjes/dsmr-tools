/*
 * Dutch Smart Meter Requirements (DSMR) Toolkit
 * Copyright (C) 2019-2024 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package nl.basjes.dsmr.simulator;

import nl.basjes.dsmr.CheckCRC;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.stream.Collectors;

import static java.lang.Math.PI;
import static java.nio.charset.StandardCharsets.UTF_8;

public final class P1Simulator {

    private P1Simulator(){}

    private static volatile boolean running = true;

    private static final String MESSAGE_HEX =
        "This is fake data generated using the DSMR simulator created by Niels Basjes. See https://dsmr.basjes.nl for more information."
        .chars().mapToObj(Integer::toHexString).collect(Collectors.joining()).toUpperCase(Locale.ROOT);

    public static void main(String...  args) throws InterruptedException {

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMddHHmmss");
        final ZoneId            zone      = ZoneId.of("Europe/Amsterdam");
        double sinPeriodScaler = 60000.0/(PI*2.0);

        long previousNow = 0;

        String electricEquipmentId = stringToHex("E1234567890");
        String gasEquipmentId = stringToHex("G1234567890");
        System.err.println("WARNING: This is FAKE data that should only be used for testing purposes.");

        long breakCounter = 0;

        while (running) {

            // Now wait for the next second to occur.
            long epochMillis = (System.currentTimeMillis() / 1000) * 1000;
            while (epochMillis <= previousNow) {
                Thread.sleep(10);
                epochMillis = (System.currentTimeMillis() / 1000) * 1000;
            }
            previousNow = epochMillis;

            // +10000 because the clocks of the smarts meters is usually OFF by several seconds.
            final Instant       nowInstant  = Instant.ofEpochMilli(epochMillis+10000);
            final ZonedDateTime now         = ZonedDateTime.ofInstant(nowInstant, zone);
            final boolean       isDST       = zone.getRules().isDaylightSavings(now.toInstant());
            final String        nowString   = formatter.format(now) +  (isDST ? "S" : "W");

            double sin1 = Math.sin(epochMillis / sinPeriodScaler);
            double sin2 = Math.sin(epochMillis / sinPeriodScaler * 1.1);
            double sin3 = Math.sin(epochMillis / sinPeriodScaler * 1.2);
            double sin4 = Math.sin(epochMillis / sinPeriodScaler * 1.3);

            String dsmrTelegram =
                "/ISk5\\2MT382-1000 FAKE\r\n" +
                "\r\n" +
                "1-3:0.2.8(50)\r\n" +                 // DSMR Version
                "0-0:1.0.0("+nowString+")\r\n" +      // Timestamp
                "0-0:96.1.1("+electricEquipmentId+")\r\n" + // Equipment Id
                "1-0:1.8.1("+ String.format("%09.3f",  800 + (100. * sin1))+"*kWh)\r\n" +
                "1-0:1.8.2("+ String.format("%09.3f",  800 - (100. * sin2))+"*kWh)\r\n" +
                "1-0:2.8.1("+ String.format("%09.3f", 1000 + (100. * sin3))+"*kWh)\r\n" +
                "1-0:2.8.2("+ String.format("%09.3f", 1000 - (100. * sin4))+"*kWh)\r\n" +
                "0-0:96.14.0(0002)\r\n" +
                "1-0:1.7.0("+String.format("%05.3f", 1.0 + (0.400 * sin1))+"*kW)\r\n" +
                "1-0:2.7.0("+String.format("%05.3f", 1.0 - (0.400 * sin2))+"*kW)\r\n" +
                "0-0:96.7.21(00004)\r\n" +
                "0-0:96.7.9(00002)\r\n" +
                "1-0:99.97.0(2)(0-0:96.7.19)(101208152415W)(0000000240*s)(101208151004W)(0000000301*s)\r\n" +
                "1-0:32.32.0(00002)\r\n" +
                "1-0:52.32.0(00001)\r\n" +
                "1-0:72.32.0(00000)\r\n" +
                "1-0:32.36.0(00000)\r\n" +
                "1-0:52.36.0(00003)\r\n" +
                "1-0:72.36.0(00000)\r\n" +
                "0-0:96.13.0("+MESSAGE_HEX+")\r\n" +
                "1-0:32.7.0("+String.format("%04.1f", 221. + (3 * sin1))+"*V)\r\n" +
                "1-0:52.7.0("+String.format("%04.1f", 222. - (3 * sin2))+"*V)\r\n" +
                "1-0:72.7.0("+String.format("%04.1f", 223. + (6 * sin3))+"*V)\r\n" +
                "1-0:31.7.0("+String.format("%03.0f",   5. + (2 * sin1))+"*A)\r\n" +
                "1-0:51.7.0("+String.format("%03.0f",   6. - (2 * sin2))+"*A)\r\n" +
                "1-0:71.7.0("+String.format("%03.0f",   7. + (3 * sin3))+"*A)\r\n" +
                "1-0:21.7.0("+String.format("%05.3f", 1.0 + (0.400 * sin1))+"*kW)\r\n" +
                "1-0:41.7.0("+String.format("%05.3f", 2.0 + (0.400 * sin2))+"*kW)\r\n" +
                "1-0:61.7.0("+String.format("%05.3f", 3.0 + (0.400 * sin3))+"*kW)\r\n" +
                "1-0:22.7.0("+String.format("%05.3f", 4.0 + (0.400 * sin1))+"*kW)\r\n" +
                "1-0:42.7.0("+String.format("%05.3f", 5.0 + (0.400 * sin2))+"*kW)\r\n" +
                "1-0:62.7.0("+String.format("%05.3f", 6.0 + (0.400 * sin3))+"*kW)\r\n" +
//                "0-1:24.1.0(003)\r\n" +
//                "0-1:96.1.0("+gasEquipmentId+")\r\n" +
//                "0-1:24.2.1(101209112500W)(12785.123*m3)\r\n" +

                // NOTE: These values are created from what I understand of the specs.
                // I have put them 'out-of-order' deliberately to test the code better
                // 4
                "0-4:24.1.0(021)\r\n" +
                "0-4:96.1.0(5f5f5f5f464f55525f5f5f)\r\n" +
                "0-4:24.2.1(101209112400W)(12785.444*GJ)\r\n" +
                // 1
                "0-1:24.1.0(002)\r\n" +
                "0-1:96.1.0(5f5f5f5f4f4e455f5f5f5f)\r\n" +
                "0-1:24.2.1(101209112100W)(12785.111*kWh)\r\n" +
                // 3
                "0-3:24.1.0(004)\r\n" +
                "0-3:96.1.0(5f5f5f5f54485245455f5f)\r\n" +
                "0-3:24.2.1(101209112300W)(12785.333*GJ)\r\n" +
                // 2
                "0-2:24.1.0(003)\r\n" +
                "0-2:96.1.0(5f5f5f5f54574f5f5f5f5f)\r\n" +
                "0-2:24.2.1(101209112200W)(12785.222*m3)\r\n" +

                "!FFFF\r\n";

            dsmrTelegram =  CheckCRC.fixCrc(dsmrTelegram);

            // Now we periodically break records by cutting off the head or the tail
            String broken = "";
            breakCounter++;
            if (breakCounter % 5 == 0) {
                int cutAtChar = dsmrTelegram.length()/2;
                if (breakCounter % 10 == 0) {
                    // Cut off the head
                    dsmrTelegram = dsmrTelegram.substring(cutAtChar);
                    broken = "--> Broken: Missing HEAD";
                } else {
                    // Cut off the tail
                    dsmrTelegram = dsmrTelegram.substring(0, cutAtChar);
                    broken = "--> Broken: Missing TAIL";
                }
            }

            System.out.print(dsmrTelegram);
            System.err.println("Wrote record for timestamp: " + nowString + broken);
        }
    }


    private static String stringToHex(String input) {
        return bytesToHex(input.getBytes(UTF_8));
    }

    private static String bytesToHex(byte[] bytes) {
        final StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();

    }

}
