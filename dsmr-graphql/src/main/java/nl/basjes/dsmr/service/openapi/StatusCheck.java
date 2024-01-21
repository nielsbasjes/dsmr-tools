/*
 * Yet Another UserAgent Analyzer
 * Copyright (C) 2013-2024 Niels Basjes
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nl.basjes.dsmr.service.openapi;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import nl.basjes.dsmr.service.device.P1DeviceReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "System status", description = "Checking if the servlet is running")
@RestController
public class StatusCheck {
    private final P1DeviceReader p1DeviceReader;

    @Autowired
    public StatusCheck(P1DeviceReader p1DeviceReader) {
        this.p1DeviceReader = p1DeviceReader;
    }

    // ------------------------------------------

    @SuppressWarnings("SameReturnValue")
    @Operation(
        summary = "Is the servlet running?",
        description = "This endpoint is intended for checking if the service has been started up."
    )
    @ApiResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The P1 device reader is running",
        content = @Content(examples = @ExampleObject("YES"))
    )
    @GetMapping(
        path = "/liveness"
    )
    public String isLive() {
        return "YES";
    }

    // ------------------------------------------

    @SuppressWarnings("SameReturnValue")
    @Operation(
        summary = "Is the P1 device reader running?",
        description = "This endpoint is intended for checking if the underlying P1 reader has been started up."
    )
    @ApiResponse(
        responseCode = "200", // HttpStatus.OK
        description = "The P1 device reader is running",
        content = @Content(examples = @ExampleObject("YES"))
    )
    @ApiResponse(
        responseCode = "500", // HttpStatus.INTERNAL_SERVER_ERROR,
        description = "The P1 device reader is still starting up or has failed to startup",
        content = @Content(examples = @ExampleObject())
    )
    @GetMapping(
        path = "/readiness"
    )
    public String isReady() {
        if (p1DeviceReader.isRunning()) {
            return "YES";
        }
        return "NO";
    }

}
