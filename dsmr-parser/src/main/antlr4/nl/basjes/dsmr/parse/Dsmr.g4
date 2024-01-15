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
grammar Dsmr;

// Based on the specs from netbeheernederland.nl and observations from my own Dutch power meter
// https://www.netbeheernederland.nl/dossiers/slimme-meter-15/documenten
// https://www.netbeheernederland.nl/_upload/Files/Slimme_meter_15_a727fce1f1.pdf

SPACES: [ \t\r\n]+ -> skip;

//    /ISK5\2M550T-1012
//    /Ene5\SAGEMCOM CX2000-           <-- Note the space in the name !
IDENT       : '/' [a-zA-Z0-9][a-zA-Z0-9][a-zA-Z0-9] '5' [ \\a-zA-Z0-9_.-]+ ;

CRC         : '!' [0-9A-F][0-9A-F][0-9A-F][0-9A-F] ;

COSEMID     : [01] '-' [0-9] ':' [0-9][0-9]? '.' [0-9][0-9]? '.' [0-9][0-9]? ;

// YYMMDDhhmmssX ASCII presentation of Time stamp
// Year, Month, Day, Hour, Minute, Second,
// and an indication whether DST is active (X=S) or DST is not active (X=W).
// S=Summertime W=Wintertime
// Because this is the DSMR (Dutch ...) we assume this means Europe/Amsterdam
// Format       Y    Y     M   M     D    D     h    h     m    m     s    s      S or W
TIMESTAMP   : [0-9][0-9] [01][0-9] [0-3][0-9] [0-2][0-9] [0-5][0-9] [0-5][0-9]  ('S'|'W')?;

fragment HEXDIGIT: [0-9A-Fa-f][0-9A-Fa-f];

HEXSTRING : HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT+;

fragment DIGIT    : [0-9];
fragment DIGIT1_2 : DIGIT    DIGIT?;
fragment DIGIT1_3 : DIGIT1_2 DIGIT?;
fragment DIGIT1_4 : DIGIT1_3 DIGIT?;
fragment DIGIT1_5 : DIGIT1_4 DIGIT?;
fragment DIGIT1_6 : DIGIT1_5 DIGIT?;
fragment DIGIT1_7 : DIGIT1_6 DIGIT?;
fragment DIGIT1_8 : DIGIT1_7 DIGIT?;
fragment DIGIT1_9 : DIGIT1_8 DIGIT?;
fragment DIGIT1_10: DIGIT1_9 DIGIT?;

FLOAT       : DIGIT1_6 '.' DIGIT1_3 ;

INT         : DIGIT1_10;

telegram
    : ident=IDENT field+ ( crc=CRC | '!\r\n' )
    ;

powerFailureEvent: '(' eventTime=TIMESTAMP ')'
                   '(' eventDuration=INT '*' eventDurationUnit='s' ')'
                   ;

field
    : '1-3:0.2.8'    '(' version=INT                             ')' #p1Version                        // P1 Version information
    | '0-0:1.0.0'    '(' timestamp=TIMESTAMP                     ')' #timestamp                        // Timestamp
    | '0-0:96.1.1'   '(' id=HEXSTRING                            ')' #equipmentId                      // Equipment identifier
    | '0-0:96.14.0'  '(' value=INT                               ')' #electricityTariffIndicator       // Tariff indicator electricity
    | '1-0:1.8.1'    '(' value=(FLOAT|INT) '*' unit='kWh'        ')' #electricityReceivedLowTariff     // Meter Reading electricity delivered to client (low tariff) in 0,001 kWh
    | '1-0:1.8.2'    '(' value=(FLOAT|INT) '*' unit='kWh'        ')' #electricityReceivedNormalTariff  // Meter Reading electricity delivered to client (normal tariff) in 0,001 kWh
    | '1-0:2.8.1'    '(' value=(FLOAT|INT) '*' unit='kWh'        ')' #electricityReturnedLowTariff     // Meter Reading electricity delivered by client (low tariff) in 0,001 kWh
    | '1-0:2.8.2'    '(' value=(FLOAT|INT) '*' unit='kWh'        ')' #electricityReturnedNormalTariff  // Meter Reading electricity delivered by client (normal tariff) in 0,001 kWh
    | '1-0:1.7.0'    '(' value=(FLOAT|INT) '*' unit='kW'         ')' #electricityPowerReceived         // Actual electricity power delivered (+P) in 1 Watt resolution
    | '1-0:2.7.0'    '(' value=(FLOAT|INT) '*' unit='kW'         ')' #electricityPowerReturned         // Actual electricity power received (-P) in 1 Watt resolution
    | '0-0:96.7.21'  '(' count=INT                               ')' #powerFailures                    // Number of power failures in any phases
    | '0-0:96.7.9'   '(' count=INT                               ')' #longPowerFailures                // Number of long power failures in any phases
    | '1-0:99.97.0'  '(' count=INT ')'                               // The number of events in the log.
                             '(' eventTypeId='0-0:96.7.19' ')'               // According to the specs the format can only be this value
                             (   powerFailureEvent   )*                      #powerFailureEventLog             // Power failure event log

    | '1-0:32.32.0'  '(' count=INT  ')'                              #voltageSagsPhaseL1               // Number of voltage sags in phase L1
    | '1-0:52.32.0'  '(' count=INT  ')'                              #voltageSagsPhaseL2               // Number of voltage sags in phase L2
    | '1-0:72.32.0'  '(' count=INT  ')'                              #voltageSagsPhaseL3               // Number of voltage sags in phase L3
    | '1-0:32.36.0'  '(' count=INT  ')'                              #voltageSwellsPhaseL1             // Number of voltage swells in phase L1
    | '1-0:52.36.0'  '(' count=INT  ')'                              #voltageSwellsPhaseL2             // Number of voltage swells in phase L2
    | '1-0:72.36.0'  '(' count=INT  ')'                              #voltageSwellsPhaseL3             // Number of voltage swells in phase L3
    | '1-0:32.7.0'   '(' value=(FLOAT|INT) '*' unit='V'  ')'         #voltageL1                        // Instantaneous voltage L1
    | '1-0:52.7.0'   '(' value=(FLOAT|INT) '*' unit='V'  ')'         #voltageL2                        // Instantaneous voltage L2
    | '1-0:72.7.0'   '(' value=(FLOAT|INT) '*' unit='V'  ')'         #voltageL3                        // Instantaneous voltage L3
    | '1-0:31.7.0'   '(' value=(FLOAT|INT) '*' unit='A'  ')'         #currentL1                        // Instantaneous current L1
    | '1-0:51.7.0'   '(' value=(FLOAT|INT) '*' unit='A'  ')'         #currentL2                        // Instantaneous current L2
    | '1-0:71.7.0'   '(' value=(FLOAT|INT) '*' unit='A'  ')'         #currentL3                        // Instantaneous current L3
    | '1-0:21.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReceivedL1                  // Instantaneous active power L1 (+P)
    | '1-0:41.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReceivedL2                  // Instantaneous active power L2 (+P)
    | '1-0:61.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReceivedL3                  // Instantaneous active power L3 (+P)
    | '1-0:22.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReturnedL1                  // Instantaneous active power L1 (-P)
    | '1-0:42.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReturnedL2                  // Instantaneous active power L2 (-P)
    | '1-0:62.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReturnedL3                  // Instantaneous active power L3 (-P)

    | '0-0:96.3.10'   '(' type=INT ')'                               #switchSetting                    // Actual switch position Electricity (in/out/enabled).

    | '0-0:96.13.1'  '(' (text=HEXSTRING)? ')'                       #messageCodes                     // Text message codes: numeric 8 digits.
    | '0-0:96.13.0'  '(' (text=HEXSTRING)? ')'                       #message                          // Text message max 1024 characters.

    // Removed from DSMR since version 4.0.7 but does occur in production systems that use DSMR 2.2 and 3.0.
    | '0-0:17.0.0'   '(' value=(FLOAT|INT) '*' unit=('kW'|'A') ')'   #electricityThreshold             // The actual threshold Electricity in kW or Ampere

    // ========================= MBUS CHANNEL 1 =========================
    | '0-1:24.1.0'  '(' type=INT ')'                #mBus1Type             // Device type.
    | '0-1:96.1.0'  '(' id=HEXSTRING ')'            #mBus1EquipmentId      // Equipment Identifier.
    | '0-1:24.2.1'  '(' timestamp=TIMESTAMP ')'
                    '(' value=(FLOAT|INT) ('*' unit=('m3'|'GJ'|'kWh'))? ')'
                                                    #mBus1Usage            // Last 5 minute reading.

    | '0-1:24.3.0'  '(' timestamp=TIMESTAMP  ')'
                    '(' profileStatus=INT ')'
                    '(' recordingPeriod=INT ')' // Recording Period in Minutes.(capture period attribute).
                    // TODO: The standard indicates there can be multiple of these.
                    //  That is not yet supported as I do not have a real life example.
                    '(' numberMeasurementValues=INT ')' //  : Only 1 is supported at this time.
                    ( '(0-1:24.2.' INT ')' '(' unit=('m3'|'GJ'|'kWh') ')' ) // no '+': only exactly one here.
                    ( '(' value=(FLOAT|INT) ')' )+  #mBus1ProfileGeneric

    | '0-1:24.4.0'  '(' type=INT ')'                #mBus1GasValvePosition // Valve position gas (on/off/released).

    // ========================= MBUS CHANNEL 2 =========================
    | '0-2:24.1.0'  '(' type=INT ')'                #mBus2Type             // Device type.
    | '0-2:96.1.0'  '(' id=HEXSTRING ')'            #mBus2EquipmentId      // Equipment Identifier.
    | '0-2:24.2.1'  '(' timestamp=TIMESTAMP ')'
                    '(' value=(FLOAT|INT) ('*' unit=('m3'|'GJ'|'kWh'))? ')'
                                                    #mBus2Usage            // Last 5 minute reading.

    | '0-2:24.3.0'  '(' timestamp=TIMESTAMP  ')'
                    '(' profileStatus=INT ')'
                    '(' recordingPeriod=INT ')' // Recording Period in Minutes.(capture period attribute).
                    // TODO: The standard indicates there can be multiple of these.
                    //  That is not yet supported as I do not have a real life example.
                    '(' numberMeasurementValues=INT ')' //  : Only 1 is supported at this time.
                    ( '(0-2:24.2.' INT ')' '(' unit=('m3'|'GJ'|'kWh') ')' ) // no '+': only exactly one here.
                    ( '(' value=(FLOAT|INT) ')' )+  #mBus2ProfileGeneric

    | '0-2:24.4.0'  '(' type=INT ')'                #mBus2GasValvePosition // Valve position gas (on/off/released).


    // ========================= MBUS CHANNEL 3 =========================
    | '0-3:24.1.0'  '(' type=INT ')'                #mBus3Type             // Device type.
    | '0-3:96.1.0'  '(' id=HEXSTRING ')'            #mBus3EquipmentId      // Equipment Identifier.
    | '0-3:24.2.1'  '(' timestamp=TIMESTAMP ')'
                    '(' value=(FLOAT|INT) ('*' unit=('m3'|'GJ'|'kWh'))? ')'
                                                    #mBus3Usage            // Last 5 minute reading.

    | '0-3:24.3.0'  '(' timestamp=TIMESTAMP  ')'
                    '(' profileStatus=INT ')'
                    '(' recordingPeriod=INT ')' // Recording Period in Minutes.(capture period attribute).
                    // TODO: The standard indicates there can be multiple of these.
                    //  That is not yet supported as I do not have a real life example.
                    '(' numberMeasurementValues=INT ')' //  : Only 1 is supported at this time.
                    ( '(0-3:24.2.' INT ')' '(' unit=('m3'|'GJ'|'kWh') ')' ) // no '+': only exactly one here.
                    ( '(' value=(FLOAT|INT) ')' )+  #mBus3ProfileGeneric

    | '0-3:24.4.0'  '(' type=INT ')'                #mBus3GasValvePosition // Valve position gas (on/off/released).
    // ========================= MBUS CHANNEL 4 =========================
    | '0-4:24.1.0'  '(' type=INT ')'                #mBus4Type             // Device type.
    | '0-4:96.1.0'  '(' id=HEXSTRING ')'            #mBus4EquipmentId      // Equipment Identifier.
    | '0-4:24.2.1'  '(' timestamp=TIMESTAMP ')'
                    '(' value=(FLOAT|INT) ('*' unit=('m3'|'GJ'|'kWh'))? ')'
                                                    #mBus4Usage            // Last 5 minute reading.

    | '0-4:24.3.0'  '(' timestamp=TIMESTAMP  ')'
                    '(' profileStatus=INT ')'
                    '(' recordingPeriod=INT ')' // Recording Period in Minutes.(capture period attribute).
                    // TODO: The standard indicates there can be multiple of these.
                    //  That is not yet supported as I do not have a real life example.
                    '(' numberMeasurementValues=INT ')' //  : Only 1 is supported at this time.
                    ( '(0-4:24.2.' INT ')' '(' unit=('m3'|'GJ'|'kWh') ')' ) // no '+': only exactly one here.
                    ( '(' value=(FLOAT|INT) ')' )+  #mBus4ProfileGeneric

    | '0-4:24.4.0'  '(' type=INT ')'                #mBus4GasValvePosition // Valve position gas (on/off/released).

    // =========================

    | COSEMID        ('(' (FLOAT|INT|TIMESTAMP|HEXSTRING) ')')*        #unknownCosemId

    ;
