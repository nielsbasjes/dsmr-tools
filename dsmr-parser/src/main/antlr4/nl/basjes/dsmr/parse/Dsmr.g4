grammar Dsmr;

// Based on the specs from netbeheernederland.nl and observations from my own Dutch power meter
// https://www.netbeheernederland.nl/dossiers/slimme-meter-15/documenten
// https://www.netbeheernederland.nl/_upload/Files/Slimme_meter_15_a727fce1f1.pdf

SPACES: [ \t\r\n]+ -> skip;

//    /ISK5\2M550T-1012
IDENT       : '/' [a-zA-Z0-9][a-zA-Z0-9][a-zA-Z0-9] [0-9] [\\a-zA-Z0-9_-]+ ;

CRC         : '!' [0-9A-F][0-9A-F][0-9A-F][0-9A-F] ;

COSEMID     : [01] '-' [0-9] ':' [0-9][0-9]? '.' [0-9][0-9]? '.' [0-9][0-9]? ;

// YYMMDDhhmmssX ASCII presentation of Time stamp
// Year, Month, Day, Hour, Minute, Second,
// and an indication whether DST is active (X=S) or DST is not active (X=W).
// S=Summertime W=Wintertime
// Format       Y    Y     M   M     D    D     h    h     m    m     s    s      S or W
TIMESTAMP   : [0-9][0-9] [01][0-9] [0-3][0-9] [0-2][0-9] [0-5][0-9] [0-5][0-9]  ('S'|'W');

fragment HEXDIGIT: [0-9A-Fa-f][0-9A-Fa-f];

HEXSTRING : HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT HEXDIGIT+;

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

telegram  : ident=IDENT field+ crc=CRC ;

field
//    :   id=COSEMID '('                                  ')' #empty
//    |   id=COSEMID '(' timestamp=TIMESTAMP              ')' #timestamp
//    |   id=COSEMID '(' value=HEXSTRING                  ')' #hexstring
//    |   id=COSEMID '(' value=INT                        ')' #number
//    |   id=COSEMID '(' value=(FLOAT|INT) '*' unit='kWh' ')' #electricityKiloWattHour
//    |   id=COSEMID '(' value=(FLOAT|INT) '*' unit='kW'  ')' #electricityKiloWatt
//    |   id=COSEMID '(' value=(FLOAT|INT) '*' unit='V'   ')' #electricityVolt
//    |   id=COSEMID '(' value=(FLOAT|INT) '*' unit='A'   ')' #electricityAmpere
//    |   id=COSEMID '(' timestamp=TIMESTAMP              ')' '(' value=(FLOAT|INT) '*' unit='m3'  ')' #cubicMeter
//    |   id=COSEMID '(' count=INT ')' '(' eventTypeId=COSEMID ')'
//                    ( '(' eventTime=TIMESTAMP ')'
//                      '(' eventDuration=INT '*' eventDurationUnit='s' ')'
//                    )+ #eventList

    : id='1-3:0.2.8'    '(' version=INT                             ')' #p1Version                        // P1 Version information
    | id='0-0:1.0.0'    '(' timestamp=TIMESTAMP                     ')' #timestamp                        // Timestamp
    | id='0-0:96.1.1'   '(' id=HEXSTRING                            ')' #equipmentId                      // Equipment identifier
    | id='1-0:1.8.1'    '(' value=(FLOAT|INT) '*' unit='kWh'        ')' #electricityReceivedLowTariff     // Meter Reading electricity delivered to client (low tariff) in 0,001 kWh
    | id='1-0:1.8.2'    '(' value=(FLOAT|INT) '*' unit='kWh'        ')' #electricityReceivedNormalTariff  // Meter Reading electricity delivered to client (normal tariff) in 0,001 kWh
    | id='1-0:2.8.1'    '(' value=(FLOAT|INT) '*' unit='kWh'        ')' #electricityReturnedLowTariff     // Meter Reading electricity delivered by client (low tariff) in 0,001 kWh
    | id='1-0:2.8.2'    '(' value=(FLOAT|INT) '*' unit='kWh'        ')' #electricityReturnedNormalTariff  // Meter Reading electricity delivered by client (normal tariff) in 0,001 kWh
    | id='0-0:96.14.0'  '(' value=INT                               ')' #electricityTariffIndicator       // Tariff indicator electricity
    | id='1-0:1.7.0'    '(' value=(FLOAT|INT) '*' unit='kW'         ')' #electricityPowerReceived         // Actual electricity power delivered (+P) in 1 Watt resolution
    | id='1-0:2.7.0'    '(' value=(FLOAT|INT) '*' unit='kW'         ')' #electricityPowerReturned         // Actual electricity power received (-P) in 1 Watt resolution
    | id='0-0:96.7.21'  '(' count=INT                               ')' #powerFailures                    // Number of power failures in any phases
    | id='0-0:96.7.9'   '(' count=INT                               ')' #longPowerFailures                // Number of long power failures in any phases
    | id='1-0:99.97.0'  '(' count=INT ')' '(' eventTypeId=COSEMID ')'
                                ( '(' eventTime=TIMESTAMP ')'
                                  '(' eventDuration=INT '*' eventDurationUnit='s' ')'
                                )+                                      #powerFailureEventLog             // Power failure event log

    | id='1-0:32.32.0'  '(' count=INT  ')'                              #voltageSagsPhaseL1               // Number of voltage sags in phase L1
    | id='1-0:52.32.0'  '(' count=INT  ')'                              #voltageSagsPhaseL2               // Number of voltage sags in phase L2
    | id='1-0:72.32.0'  '(' count=INT  ')'                              #voltageSagsPhaseL3               // Number of voltage sags in phase L3
    | id='1-0:32.36.0'  '(' count=INT  ')'                              #voltageSwellsPhaseL1             // Number of voltage swells in phase L1
    | id='1-0:52.36.0'  '(' count=INT  ')'                              #voltageSwellsPhaseL2             // Number of voltage swells in phase L2
    | id='1-0:72.36.0'  '(' count=INT  ')'                              #voltageSwellsPhaseL3             // Number of voltage swells in phase L3
    | id='1-0:32.7.0'   '(' value=(FLOAT|INT) '*' unit='V'  ')'         #voltageL1                        // Instantaneous voltage L1
    | id='1-0:52.7.0'   '(' value=(FLOAT|INT) '*' unit='V'  ')'         #voltageL2                        // Instantaneous voltage L2
    | id='1-0:72.7.0'   '(' value=(FLOAT|INT) '*' unit='V'  ')'         #voltageL3                        // Instantaneous voltage L3
    | id='1-0:31.7.0'   '(' value=(FLOAT|INT) '*' unit='A'  ')'         #currentL1                        // Instantaneous current L1
    | id='1-0:51.7.0'   '(' value=(FLOAT|INT) '*' unit='A'  ')'         #currentL2                        // Instantaneous current L2
    | id='1-0:71.7.0'   '(' value=(FLOAT|INT) '*' unit='A'  ')'         #currentL3                        // Instantaneous current L3
    | id='1-0:21.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReceivedL1                  // Instantaneous active power L1 (+P)
    | id='1-0:41.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReceivedL2                  // Instantaneous active power L2 (+P)
    | id='1-0:61.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReceivedL3                  // Instantaneous active power L3 (+P)
    | id='1-0:22.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReturnedL1                  // Instantaneous active power L1 (-P)
    | id='1-0:42.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReturnedL2                  // Instantaneous active power L2 (-P)
    | id='1-0:62.7.0'   '(' value=(FLOAT|INT) '*' unit='kW' ')'         #powerReturnedL3                  // Instantaneous active power L3 (-P)
    | id='0-0:96.13.0'  '(' (text=HEXSTRING)? ')'                       #message                          // Text message max 1024 characters.

    | id='0-1:24.1.0'   '(' type=INT ')'                                #mBus1Type                        // MBus channel 1: Device type.
    | id='0-1:96.1.0'   '(' id=HEXSTRING ')'                            #mBus1EquipmentId                 // MBus channel 1: Equipment Identifier.
    | id='0-1:24.2.1'   '(' timestamp=TIMESTAMP ')'
                        '(' value=(FLOAT|INT) '*' unit=('m3'|'GJ') ')'  #mBus1Usage                       // MBus channel 1: Last 5 minute reading.

    | id='0-2:24.1.0'   '(' type=INT ')'                                #mBus2Type                        // MBus channel 2: Device type.
    | id='0-2:96.1.0'   '(' id=HEXSTRING ')'                            #mBus2EquipmentId                 // MBus channel 2: Equipment Identifier.
    | id='0-2:24.2.1'   '(' timestamp=TIMESTAMP ')'
                        '(' value=(FLOAT|INT) '*' unit=('m3'|'GJ') ')'  #mBus2Usage                       // MBus channel 2: Last 5 minute reading.

    | id='0-3:24.1.0'   '(' type=INT ')'                                #mBus3Type                        // MBus channel 3: Device type.
    | id='0-3:96.1.0'   '(' id=HEXSTRING ')'                            #mBus3EquipmentId                 // MBus channel 3: Equipment Identifier.
    | id='0-3:24.2.1'   '(' timestamp=TIMESTAMP ')'
                        '(' value=(FLOAT|INT) '*' unit=('m3'|'GJ') ')'  #mBus3Usage                       // MBus channel 3: Last 5 minute reading.

    | id='0-4:24.1.0'   '(' type=INT ')'                                #mBus4Type                        // MBus channel 4: Device type.
    | id='0-4:96.1.0'   '(' id=HEXSTRING ')'                            #mBus4EquipmentId                 // MBus channel 4: Equipment Identifier.
    | id='0-4:24.2.1'   '(' timestamp=TIMESTAMP ')'
                        '(' value=(FLOAT|INT) '*' unit=('m3'|'GJ') ')'  #mBus4Usage                       // MBus channel 4: Last 5 minute reading.
    ;
