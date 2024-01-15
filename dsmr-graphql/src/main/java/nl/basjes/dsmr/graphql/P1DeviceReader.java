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

package nl.basjes.dsmr.graphql;

import lombok.extern.log4j.Log4j2;
import nl.basjes.dsmr.DSMRTelegram;
import nl.basjes.dsmr.ParseDsmrTelegram;
import nl.basjes.parse.ReadUTF8RecordStream;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Component
@Log4j2
public class P1DeviceReader implements DisposableBean, Runnable {

    private volatile boolean running;

    private final Thread thread;

    private final ApplicationContext context;

    private final DSMRTelegramPublisher output;

    private final P1DeviceReaderConfig config;

    @SuppressWarnings("this-escape")
    public P1DeviceReader(ApplicationContext context,
                          DSMRTelegramPublisher output,
                          P1DeviceReaderConfig config) {
        this.context = context;
        this.output = output;
        this.config = config;
        this.thread = new Thread(this);
        this.thread.start();
    }

    @Override
    public void run(){
        running = true;
        log.info("Using tty: {}", config.getTty());
        try(FileInputStream inputStream = new FileInputStream(config.getTty())) {

            ReadUTF8RecordStream reader = new ReadUTF8RecordStream(inputStream, "\r\n![0-9A-F]{4}\r\n");

            log.info("Starting read loop");

            while (running) {
                String telegram = reader.read();
                if (telegram == null) {
                    running = false;
                    log.info("End of stream detected");
                    break;
                }

//                if (!crcIsValid(telegram)) {
//                    log.info("Bad DSMR Telegram (invalid CRC)");
//                    log.trace("DROPPING INVALID Telegram:\nvvvvvvvvvv\n{}\n^^^^^^^^^^\n", telegram);
//                    continue;
//                }

                DSMRTelegram dsmrTelegram;
                try {
                    dsmrTelegram = ParseDsmrTelegram.parse(telegram);
                } catch (Exception e) {
                    log.error("Exception: {}", e.getMessage());
                    throw e;
                }

                log.info("Got DSMR Telegram @ {}", dsmrTelegram.getReceiveTimestamp());

                output.publish(dsmrTelegram);
            }
        } catch (FileNotFoundException e) {
            log.error("Got FileNotFoundException: {}", e.getMessage());
        } catch (IOException e) {
            log.error("Got IOException: {}", e.getMessage());
        } catch (NullPointerException e) {
            log.error("Got NullPointerException: {}", e.getMessage());
        }

        // If this ends then the entire application must terminate.
        shutdownApplication();
    }

    private void shutdownApplication() {
        if (context != null) {
            ApplicationContext applicationContext = context;
            ApplicationContext parent = applicationContext.getParent();
            while (parent != null) {
                applicationContext = parent;
                parent = applicationContext.getParent();
            }
            log.error("Terminating application.");
            ((ConfigurableApplicationContext) context).close();
        }
    }

    @Override
    public void destroy(){
        running = false;
    }

}
