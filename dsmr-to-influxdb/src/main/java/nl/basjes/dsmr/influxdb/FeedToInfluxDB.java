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

package nl.basjes.dsmr.influxdb;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import nl.basjes.dsmr.DSMRTelegram;
import nl.basjes.dsmr.ParseDsmrTelegram;
import nl.basjes.parse.ReadUTF8RecordStream;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.time.Instant;

import static nl.basjes.dsmr.CheckCRC.crcIsValid;

public final class FeedToInfluxDB {

    private FeedToInfluxDB() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(FeedToInfluxDB.class);

    private static volatile boolean running = true;

    public static void main(String... args) throws IOException {
        final CommandOptions commandlineOptions = new CommandOptions();
        final CmdLineParser  parser             = new CmdLineParser(commandlineOptions);
        try {
            parser.parseArgument(args);

        } catch (CmdLineException cle) {
            LOG.error("Errors: {}", cle.getMessage());
            LOG.error("");
            System.err.println("Usage: java jar <jar containing this class> <options>");
            parser.printUsage(System.err);
            return;
        }

        LOG.info("Opening stream {}", commandlineOptions.tty);
        try(FileInputStream inputStream = new FileInputStream(commandlineOptions.tty)) {
            ReadUTF8RecordStream reader = new ReadUTF8RecordStream(inputStream, "\r\n![0-9A-F]{4}\r\n");

            if (commandlineOptions.databaseUrl == null) {
                LOG.info("No database, outputting to console");
                readLoop(reader, null);
                return;
            }

            LOG.info("Connecting to database {} at {}", commandlineOptions.databaseName, commandlineOptions.databaseUrl);
            try(InfluxDBClient influxDBClient = InfluxDBClientFactory
                .create(commandlineOptions.databaseUrl,
                    commandlineOptions.databaseToken.toCharArray(),
                    "basjes",
                    "dsmr")) {

                if (!influxDBClient.ping()) {
                    LOG.error("Error pinging server.");
                    return;
                }
                readLoop(reader, influxDBClient.getWriteApiBlocking());
            }
        }
    }


    public static void readLoop(ReadUTF8RecordStream reader, WriteApiBlocking writeApi) throws IOException {
        LOG.info("Starting read loop");

        while (running) {
            String telegram = reader.read();
            if (telegram == null) {
                running = false;
                LOG.info("End of stream detected");
                break;
            }

            if (!crcIsValid(telegram)) {
                LOG.error("DROPPING INVALID Telegram:\nvvvvvvvvvv\n{}\n^^^^^^^^^^\n", telegram);
                continue;
            }

            DSMRTelegram dsmrTelegram = null;
            try {
                dsmrTelegram = ParseDsmrTelegram.parse(telegram);
            } catch (Exception e) {
                System.err.println("Exception: " + e);
                throw e;
            }

            if (dsmrTelegram != null && dsmrTelegram.isValid()) {
                Point point = Point
                    .measurement("electricity")

                    // We are rounding the timestamp to seconds to make the graphs in influxdb work a bit better
                    .time(Instant.ofEpochSecond(dsmrTelegram.getReceiveTimestamp().toEpochSecond()), WritePrecision.S)

                    .addTag("equipmentId",                            dsmrTelegram.getEquipmentId())
                    .addTag("p1Version",                              dsmrTelegram.getP1Version())

                    .addField("electricityReceivedLowTariff",    dsmrTelegram.getElectricityReceivedLowTariff())
                    .addField("electricityReceivedNormalTariff", dsmrTelegram.getElectricityReceivedNormalTariff())
                    .addField("electricityReturnedLowTariff",    dsmrTelegram.getElectricityReturnedLowTariff())
                    .addField("electricityReturnedNormalTariff", dsmrTelegram.getElectricityReturnedNormalTariff())
                    .addField("electricityTariffIndicator",      dsmrTelegram.getElectricityTariffIndicator())
                    .addField("electricityPowerReceived",        dsmrTelegram.getElectricityPowerReceived())
                    .addField("electricityPowerReturned",        dsmrTelegram.getElectricityPowerReturned())
                    .addField("powerFailures",                   dsmrTelegram.getPowerFailures())
                    .addField("longPowerFailures",               dsmrTelegram.getLongPowerFailures())
                    .addField("voltageSagsPhaseL1",              dsmrTelegram.getVoltageSagsPhaseL1())
                    .addField("voltageSagsPhaseL2",              dsmrTelegram.getVoltageSagsPhaseL2())
                    .addField("voltageSagsPhaseL3",              dsmrTelegram.getVoltageSagsPhaseL3())
                    .addField("voltageSwellsPhaseL1",            dsmrTelegram.getVoltageSwellsPhaseL1())
                    .addField("voltageSwellsPhaseL2",            dsmrTelegram.getVoltageSwellsPhaseL2())
                    .addField("voltageSwellsPhaseL3",            dsmrTelegram.getVoltageSwellsPhaseL3())
                    .addField("voltageL1",                       dsmrTelegram.getVoltageL1())
                    .addField("voltageL2",                       dsmrTelegram.getVoltageL2())
                    .addField("voltageL3",                       dsmrTelegram.getVoltageL3())
                    .addField("currentL1",                       dsmrTelegram.getCurrentL1())
                    .addField("currentL2",                       dsmrTelegram.getCurrentL2())
                    .addField("currentL3",                       dsmrTelegram.getCurrentL3())
                    .addField("powerReceivedL1",                 dsmrTelegram.getPowerReceivedL1())
                    .addField("powerReceivedL2",                 dsmrTelegram.getPowerReceivedL2())
                    .addField("powerReceivedL3",                 dsmrTelegram.getPowerReceivedL3())
                    .addField("powerReturnedL1",                 dsmrTelegram.getPowerReturnedL1())
                    .addField("powerReturnedL2",                 dsmrTelegram.getPowerReturnedL2())
                    .addField("powerReturnedL3",                 dsmrTelegram.getPowerReturnedL3());

                if (writeApi == null) {
                    LOG.info("{}", point.toLineProtocol());
                } else {
                    LOG.info("Writing to influxDB");
                    writeApi.writePoint(point);
                }
            }
        }
    }

    @SuppressWarnings("CanBeFinal")
    private static final class CommandOptions {
        @Option(name = "-tty", usage = "The tty device from which to read")
        private String tty = "/dev/ttyUSB0";

        @Option(
            name = "-databaseUrl",
            usage = "The URL of the InfluxDB database",
            depends = { "-databaseUrl", "-databaseName", "-databaseToken", "-databaseOrg", "-databaseBucket" }
            )
        private String databaseUrl = null;

        @Option(
            name = "-databaseName",
            usage = "The NAME of the InfluxDB database",
            depends = { "-databaseUrl", "-databaseName", "-databaseToken", "-databaseOrg", "-databaseBucket" }
            )
        private String databaseName = null;

        @Option(
            name = "-databaseToken",
            usage = "The API token of the InfluxDB database",
            depends = { "-databaseUrl", "-databaseName", "-databaseToken", "-databaseOrg", "-databaseBucket" }
            )
        private String databaseToken = null;

        @Option(
            name = "-databaseOrg",
            usage = "The Organization of the InfluxDB database",
            depends = { "-databaseUrl", "-databaseName", "-databaseToken", "-databaseOrg", "-databaseBucket" }
            )
        private String databaseOrg = null;

        @Option(
            name = "-databaseBucket",
            usage = "The Bucket of the InfluxDB database",
            depends = { "-databaseUrl", "-databaseName", "-databaseToken", "-databaseOrg", "-databaseBucket" }
            )
        private String databaseBucket = null;

        @Override
        public String toString() {
            return
                "TTY             = " + tty + "\n" +
                "Database Url    = " + databaseUrl + "\n" +
                "Database Name   = " + databaseName + "\n" +
                "Database Token  = " + databaseToken + "\n" +
                "Database Org    = " + databaseOrg + "\n" +
                "Database Bucket = " + databaseBucket + "\n";
        }
    }
}
