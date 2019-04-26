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

package nl.basjes.dsmr.influxdb;

import nl.basjes.dsmr.DSMRTelegram;
import nl.basjes.dsmr.ParseDsmrTelegram;
import nl.basjes.parse.ReadUTF8RecordStream;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import org.influxdb.dto.Pong;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class FeedToInfluxDB {

    private static final Logger LOG = LoggerFactory.getLogger(FeedToInfluxDB.class);

    public static volatile boolean running = true;

    public static void main(String...  args) throws IOException {
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

            InfluxDB influxDB = null;
            if (commandlineOptions.databaseUrl == null) {
                LOG.info("No database, outputting to console");
            } else {
                LOG.info("Connecting to database {}", commandlineOptions.databaseUrl);

                if (commandlineOptions.databaseUsername == null) {
                    influxDB = InfluxDBFactory.connect(commandlineOptions.databaseUrl);
                } else {
                    influxDB = InfluxDBFactory.connect(commandlineOptions.databaseUrl, commandlineOptions.databaseUsername, commandlineOptions.databasePassword);
                }
                influxDB.setDatabase(commandlineOptions.databaseName);
                influxDB.disableBatch();

                Pong response = influxDB.ping();
                if (response.getVersion().equalsIgnoreCase("unknown")) {
                    LOG.error("Error pinging server.");
                    return;
                }
            }

            ReadUTF8RecordStream reader = new ReadUTF8RecordStream(inputStream, "\r\n![0-9A-F]{4}\r\n");

            LOG.info("Starting read loop");

            while (running) {
                String       telegram = reader.read();
                if (telegram == null) {
                    running = false;
                    LOG.info ("End of stream detected");
                    break;
                }
                DSMRTelegram record   = ParseDsmrTelegram.parse(telegram);

                if (record != null && record.isValid()) {
                    Point point = Point
                        .measurement("electricity")

                        .time(record.getTimestamp().toInstant().toEpochMilli(), TimeUnit.MILLISECONDS)

                        .tag("equipmentId", record.getEquipmentId())
                        .tag("p1Version", record.getP1Version())

                        .addField("electricityReceivedLowTariff",    record.getElectricityReceivedLowTariff())
                        .addField("electricityReceivedNormalTariff", record.getElectricityReceivedNormalTariff())
                        .addField("electricityReturnedLowTariff",    record.getElectricityReturnedLowTariff())
                        .addField("electricityReturnedNormalTariff", record.getElectricityReturnedNormalTariff())
                        .addField("electricityTariffIndicator",      record.getElectricityTariffIndicator())
                        .addField("electricityPowerReceived",        record.getElectricityPowerReceived())
                        .addField("electricityPowerReturned",        record.getElectricityPowerReturned())
                        .addField("powerFailures",                   record.getPowerFailures())
                        .addField("longPowerFailures",               record.getLongPowerFailures())
                        .addField("voltageSagsPhaseL1",              record.getVoltageSagsPhaseL1())
                        .addField("voltageSagsPhaseL2",              record.getVoltageSagsPhaseL2())
                        .addField("voltageSagsPhaseL3",              record.getVoltageSagsPhaseL3())
                        .addField("voltageSwellsPhaseL1",            record.getVoltageSwellsPhaseL1())
                        .addField("voltageSwellsPhaseL2",            record.getVoltageSwellsPhaseL2())
                        .addField("voltageSwellsPhaseL3",            record.getVoltageSwellsPhaseL3())
                        .addField("voltageL1",                       record.getVoltageL1())
                        .addField("voltageL2",                       record.getVoltageL2())
                        .addField("voltageL3",                       record.getVoltageL3())
                        .addField("currentL1",                       record.getCurrentL1())
                        .addField("currentL2",                       record.getCurrentL2())
                        .addField("currentL3",                       record.getCurrentL3())
                        .addField("powerReceivedL1",                 record.getPowerReceivedL1())
                        .addField("powerReceivedL2",                 record.getPowerReceivedL2())
                        .addField("powerReceivedL3",                 record.getPowerReceivedL3())
                        .addField("powerReturnedL1",                 record.getPowerReturnedL1())
                        .addField("powerReturnedL2",                 record.getPowerReturnedL2())
                        .addField("powerReturnedL3",                 record.getPowerReturnedL3())

                        .build();

                    if (influxDB == null) {
                        LOG.info("{}", point.lineProtocol());
                    } else {
                        influxDB.write(point);
                    }
                }
            }
        }
    }

    private static class CommandOptions {
        @Option(name = "-tty", usage = "The tty device from which to read")
        private String tty = "/dev/ttyUSB0";

        @Option(name = "-databaseUrl", usage = "The URL of the InfluxDB database")
        private String databaseUrl = null;

        @Option(name = "-databaseName", usage = "The NAME of the InfluxDB database")
        private String databaseName = null;

        @Option(name = "-databaseUsername", usage = "The USERNAME of the InfluxDB database")
        private String databaseUsername = null;

        @Option(name = "-databasePassword", usage = "The PASSWORD of the InfluxDB database")
        private String databasePassword = null;
    }

}
