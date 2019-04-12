/*
 * Dutch Smart Meter Requirements (DSMR) Toolkit
 * Copyright (C) 2019-2019 Niels Basjes
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

import nl.basjes.dsmr.parse.CheckCRC;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.Math.PI;
import static java.nio.charset.StandardCharsets.UTF_8;

public class P1Simulator {

    public static volatile boolean running = true;

    public static void main(String...  args) throws InterruptedException {

        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("YYMMddHHmmss");
        final ZoneId            zone      = ZoneId.of("Europe/Amsterdam");
        double sinPeriodScaler = 60000.0/(PI*2.0);

        long previousNow = 0;

        String electricEquipmentId = stringToHex("E1234567890");
        String gasEquipmentId = stringToHex("G1234567890");
        System.err.println("WARNING: This is FAKE data that should only be used for testing purposes.");

        while (running) {

            // Now wait for the next second to occur.
            long epochMillis = ( System.currentTimeMillis() / 1000 ) * 1000;
            while (epochMillis <= previousNow) {
                Thread.sleep(10);
                epochMillis = ( System.currentTimeMillis() / 1000 ) * 1000;
            }
            previousNow = epochMillis;

            final Instant       nowInstant  = Instant.ofEpochMilli(epochMillis);
            final ZonedDateTime now         = ZonedDateTime.ofInstant(nowInstant, zone);
            final boolean       isDST       = zone.getRules().isDaylightSavings(now.toInstant());
            final String        nowString   = formatter.format(now) +  (isDST ? "S" : "W");

            double sin = Math.sin(epochMillis / sinPeriodScaler);

            String record =
                "/ISk5\\2MT382-1000\r\n" +
                "\r\n" +
                "1-3:0.2.8(50)\r\n" +                 // DSMR Version
                "0-0:1.0.0("+nowString+")\r\n" +      // Timestamp
                "0-0:96.1.1("+electricEquipmentId+")\r\n" + // Equipment Id
                "1-0:1.8.1("+ String.format("%09.3f",  800 + (100. * sin))+"*kWh)\r\n" +
                "1-0:1.8.2("+ String.format("%09.3f",  800 - (100. * sin))+"*kWh)\r\n" +
                "1-0:2.8.1("+ String.format("%09.3f", 1000 + (100. * sin))+"*kWh)\r\n" +
                "1-0:2.8.2("+ String.format("%09.3f", 1000 - (100. * sin))+"*kWh)\r\n" +
                "0-0:96.14.0(0002)\r\n" +
                "1-0:1.7.0("+String.format("%05.3f", 1.0 + (0.400 * sin))+"*kW)\r\n" +
                "1-0:2.7.0("+String.format("%05.3f", 1.0 - (0.400 * sin))+"*kW)\r\n" +
                "0-0:96.7.21(00004)\r\n" +
                "0-0:96.7.9(00002)\r\n" +
                "1-0:99.97.0(2)(0-0:96.7.19)(101208152415W)(0000000240*s)(101208151004W)(0000000301*s)\r\n" +
                "1-0:32.32.0(00002)\r\n" +
                "1-0:52.32.0(00001)\r\n" +
                "1-0:72.32.0(00000)\r\n" +
                "1-0:32.36.0(00000)\r\n" +
                "1-0:52.36.0(00003)\r\n" +
                "1-0:72.36.0(00000)\r\n" +
                "0-0:96.13.0(44534D522073696D756C61746F722063726561746564206279204E69656C73204261736A65732E205365652068747470733A2F2F64736D722E6261736A65732E6E6C20666F72206D6F726520696E666F726D6174696F6E2E)\r\n" +
                "1-0:32.7.0("+String.format("%04.1f", 221. + (3 * sin))+"*V)\r\n" +
                "1-0:52.7.0("+String.format("%04.1f", 222. - (3 * sin))+"*V)\r\n" +
                "1-0:72.7.0("+String.format("%04.1f", 223. + (6 * sin))+"*V)\r\n" +
                "1-0:31.7.0("+String.format("%03.0f",   5. + (2 * sin))+"*A)\r\n" +
                "1-0:51.7.0("+String.format("%03.0f",   6. - (2 * sin))+"*A)\r\n" +
                "1-0:71.7.0("+String.format("%03.0f",   7. + (3 * sin))+"*A)\r\n" +
                "1-0:21.7.0("+String.format("%05.3f", 1.0 + (0.400 * sin))+"*kW)\r\n" +
                "1-0:41.7.0("+String.format("%05.3f", 2.0 + (0.400 * sin))+"*kW)\r\n" +
                "1-0:61.7.0("+String.format("%05.3f", 3.0 + (0.400 * sin))+"*kW)\r\n" +
                "1-0:22.7.0("+String.format("%05.3f", 4.0 + (0.400 * sin))+"*kW)\r\n" +
                "1-0:42.7.0("+String.format("%05.3f", 5.0 + (0.400 * sin))+"*kW)\r\n" +
                "1-0:62.7.0("+String.format("%05.3f", 6.0 + (0.400 * sin))+"*kW)\r\n" +
                "0-1:24.1.0(003)\r\n" +
                "0-1:96.1.0("+gasEquipmentId+")\r\n" +
                "0-1:24.2.1(101209112500W)(12785.123*m3)\r\n" +
                "!FFFF\r\n";

            record =  CheckCRC.fixCrc(record);
            System.out.print(record);
            System.err.println("Wrote record for timestamp: " + nowString);
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
