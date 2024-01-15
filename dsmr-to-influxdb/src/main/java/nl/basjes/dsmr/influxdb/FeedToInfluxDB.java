/*
 * Dutch Smart Meter Requirements (DSMR) Toolkit
 * Copyright (C) 2019-2021 Niels Basjes
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

import static nl.basjes.dsmr.CheckCRC.crcIsValid;

public final class FeedToInfluxDB {

    private FeedToInfluxDB() {
    }

    private static final Logger LOG = LoggerFactory.getLogger(FeedToInfluxDB.class);

    private static volatile boolean running = true;

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

        InfluxDB influxDB = null;

        try(FileInputStream inputStream = new FileInputStream(commandlineOptions.tty)) {

            if (commandlineOptions.databaseUrl == null) {
                LOG.info("No database, outputting to console");
            } else {
                LOG.info("Connecting to database {}", commandlineOptions.databaseUrl);

                if (commandlineOptions.databaseUsername == null) {
                    influxDB = InfluxDBFactory.connect(commandlineOptions.databaseUrl);
                } else {
                    influxDB = InfluxDBFactory.connect(
                        commandlineOptions.databaseUrl,
                        commandlineOptions.databaseUsername,
                        commandlineOptions.databasePassword);
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
                    e.printStackTrace();
                    throw e;
                }

                if (dsmrTelegram != null && dsmrTelegram.isValid()) {
                    Point point = Point
                        .measurement("electricity")

                        // We are rounding the timestamp to seconds to make the graphs in influxdb work a bit better
//                        .time((System.currentTimeMillis()/1000)*1000, TimeUnit.MILLISECONDS)
                        .time(dsmrTelegram.getReceiveTimestamp().toEpochSecond(), TimeUnit.SECONDS)

                        .tag("equipmentId",                          dsmrTelegram.getEquipmentId())
                        .tag("p1Version",                            dsmrTelegram.getP1Version())

                        .addField("electricityReceivedLowTariff",    dsmrTelegram.getElectricityReceivedLowTariff())
                        .addField("electricityReceivedNormalTariff", dsmrTelegram.getElectricityReceivedNormalTariff())
                        .addField("electricityReturnedLowTariff",    dsmrTelegram.getElectricityReturnedLowTariff())
                        .addField("electricityReturnedNormalTariff", dsmrTelegram.getElectricityReturnedNormalTariff())
                        // DONOTCOMMIT: The cast to float is because  of my OWN influxDB ONLY !
                        .addField("electricityTariffIndicator",      (float)dsmrTelegram.getElectricityTariffIndicator())
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
                        .addField("powerReturnedL3",                 dsmrTelegram.getPowerReturnedL3())

                        .build();

                    if (influxDB == null) {
                        LOG.info("{}", point.lineProtocol());
                    } else {
                        influxDB.write(point);
                    }
                }
            }
        } finally {
            if (influxDB != null) {
                influxDB.close();
            }
        }
    }

    private static final class CommandOptions {
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
