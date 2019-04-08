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
// Year, Month, Day, Hour, Minute, Second, and an indication whether
// DST is active (X=S) or DST is not active (X=W).
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
    :   id=COSEMID '('                                  ')' #empty
    |   id=COSEMID '(' timestamp=TIMESTAMP              ')' #timestamp
    |   id=COSEMID '(' value=HEXSTRING                  ')' #hexstring
    |   id=COSEMID '(' value=INT                        ')' #number
    |   id=COSEMID '(' value=(FLOAT|INT) '*' unit='kWh' ')' #electricityKiloWattHour
    |   id=COSEMID '(' value=(FLOAT|INT) '*' unit='kW'  ')' #electricityKiloWatt
    |   id=COSEMID '(' value=(FLOAT|INT) '*' unit='V'   ')' #electricityVolt
    |   id=COSEMID '(' value=(FLOAT|INT) '*' unit='A'   ')' #electricityAmpere
    |   id=COSEMID '(' timestamp=TIMESTAMP              ')' '(' value=(FLOAT|INT) '*' unit='m3'  ')' #gasCubicMeter
    |   id=COSEMID '(' count=INT ')' '(' eventTypeId=COSEMID ')'
                    ( '(' eventTime=TIMESTAMP ')'
                      '(' eventDuration=INT '*' eventDurationUnit='s' ')'
                    )+ #eventList
    ;




// '1-3:0.2.8';    // Version information for P1 output 1-3:0.2.8.255 2 1
// '0-0:1.0.0';    // Date-time stamp of the P1 message 0-0:1.0.0.255 2 8 TST YYMMDDhhmmssX
// '0-0:96.1.1';   // Equipment identifier 0-0:96.1.1.255 2 Value 1 Data Sn (n=0..96), tag 9
// '1-0:1.8.1';    // Meter Reading electricity delivered to client (Tariff 1) in 0,001 kWh 1-0:1.8.1.255 2 Value 3 Register F9(3,3), tag 6 kWh
// '1-0:1.8.2';    // Meter Reading electricity delivered to client (Tariff 2) in 0,001 kWh 1-0:1.8.2.255 2 Value 3 Register F9(3,3), tag 6 kWh
// '1-0:2.8.1';    // Meter Reading electricity delivered by client (Tariff 1) in 0,001 kWh 1-0:2.8.1.255 2 Value 3 Register F9(3,3), tag 6 kWh
// '1-0:2.8.2';    // Meter Reading electricity delivered by client (Tariff 2) in 0,001 kWh 1-0:2.8.2.255 2 Value 3 Register F9(3,3), tag 6 kWh
// '0-0:96.14.0';  // Tariff indicator electricity. The tariff indicator can also be used to switch tariff dependent loads e.g boilers. This is the responsibility of the P1 user 0-0:96.14.0.255 2 Value 1 Data S4, tag 9
// '1-0:1.7.0';    // Actual electricity power delivered (+P) in 1 Watt resolution 1-0:1.7.0.255 2 Value 3 Register F5(3,3), tag 18 kW
// '1-0:2.7.0';    // Actual electricity power received (-P) in 1 Watt resolution 1-0:2.7.0.255 2 Value 3 Register F5(3,3), tag 18 k
// '0-0:96.7.9';   // Number of long power failures in any phase 0-0:96.7.9.255 2 Value 1 Data F5(0,0), tag 18
// '1-0:99.97.0';  // Power Failure Event Log (long power failures) 1-0:99.97.0.255 2 Buffer 7 Profile Generic TST, F10(0,0) - tag 6 Format applicable for the value within the log (OBIS code 0- 0:96.7.19.255)
// '1-0:32.32.0';  // Timestamp (end of failure) – duration in seconds Number of voltage sags in phase L1 1-0:32.32.0.255 2 Value 1 Data F5(0,0), tag 18
// '1-0:72.32.0';  // Number of voltage sags in phase L2 1-0:52.32.0.255 2 Value 1 Data F5(0,0), tag 18 Number of voltage sags in phase L3 1-0:72.32.0.255 2 Value 1 Data F5(0,0), tag 18
// '1-0:52.36.0';  // Number of voltage swells in phase L1 1-0:32.36.0.255 2 Value 1 Data F5(0,0), tag 18 Number of voltage swells in phase L2 1-0:52.36.0.255 2 Value 1 Data F5(0,0), tag 18
// '0-0:96.13.0';  // Number of voltage swells in phase L3 1-0:72.36.0.255 2 Value 1 Data F5(0,0), tag 18 Text message max 1024 characters. 0-0:96.13.0.255 2 Value 1 Data Sn (n=0..2048), tag 9
// '1-0:32.7.0';   // Instantaneous voltage L1 in V resolution 1-0:32.7.0.255 2 Value 3 Register F4(1,1), tag 18 V
// '1-0:52.7.0';   // Instantaneous voltage L2 in V resolution 1-0:52.7.0.255 2 Value 3 Register F4(1,1), tag 18 V
// '1-0:72.7.0';   // Instantaneous voltage L3 in V resolution 1-0:72.7.0.255 2 Value 3 Register F4(1,1), tag 18 V
// '1-0:31.7.0';   // Instantaneous current L1 in A resolution. 1-0:31.7.0.255 2 Value 3 Register F3(0,0), tag 18 A
// '1-0:51.7.0';   // Instantaneous current L2 in A resolution. 1-0:51.7.0.255 2 Value 3 Register F3(0,0), tag 18 A
// '1-0:71.7.0';   // Instantaneous current L3 in A resolution. 1-0:71.7.0.255 2 Value 3 Register F3(0,0), tag 18 A
// '1-0:21.7.0';   // Instantaneous active power L1 (+P) in W resolution 1-0:21.7.0.255 2 3 F5(3,3), tag 18 kW
// '1-0:41.7.0';   // Instantaneous active power L2 (+P) in W resolution 1-0:41.7.0.255 2 Value 3 Register F5(3,3), tag 18 kW
// '1-0:61.7.0';   // Instantaneous active power L3 (+P) in W resolution 1-0:61.7.0.255 2 Value 3 Register F5(3,3), tag 18 kW
// '1-0:22.7.0';   // Instantaneous active power L1 (-P) in W resolution 1-0:22.7.0.255 2 Value 3 Register F5(3,3), tag 18 kW
// '1-0:42.7.0';   // Instantaneous active power L2 (-P) in W resolution 1-0:42.7.0.255 2 Value 3 Register F5(3,3), tag 18 kW
// '1-0:62.7.0';   // Instantaneous active power L3 (-P) in W resolution 1-0:62.7.0.255 2 Value 3 Register F5(3,3), tag 18 kW
// '0-n:24.1.0';   // Device-Type 0-n:24.1.0.255 9 Device type 72 M-Bus client F3(0,0), tag 17
// '0-n:96.1.0';   // Equipment identifier (Gas) 0-n:96.1.0.255 2 Value 1 Data Sn (n=0..96), tag 9
// '0-n:24.2.1';   // Last 5-minute value (temperature converted), gas delivered to client in m3, including decimal values and capture time 0-n:24.2.1.255 5 Capture time 4 Extended Register TST 0-n:24.2.1.255 2 Value 4 Extended Register F8(2,2)/F8(3,3), tag 18 (See note 2) m3
// '0-n:96.1.0';   // Device-Type 0-n:24.1.0.255 9 Device type 72 M-Bus client F3(0,0), tag 17 Equipment identifier (Thermal: Heat or Cold) 0-n:96.1.0.255 2 Value 1 Data Sn (n=0..96), tag 9
// '0-n:24.2.1';   // Last 5-minute Meter reading Heat or Cold in 0,01 GJ and capture time 0-n:24.2.1.255 5 Capture time 4 Extended Register TST 0-n:24.2.1.255 2 4 Fn(2,2) GJ
// '0-n:96.1.0';   // Device-Type 0-n:24.1.0.255 9 Device type 72 M-Bus client F3(0,0), tag 17 Equipment identifier (Water) 0-n:96.1.0.255 2 Value 1 Data Sn (n=0..96), tag 9
// '0-n:24.2.1';   // Last 5-minute Meter reading in 0,001 m3 and capture time 0-n:24.2.1.255 5 Capture time 4 Extended Register TST 0-n:24.2.1.255 2 Value 4 Extended Register Fn(3,3) (See Note 1) m3
// '0-n:24.1.0';   // Device-Type 0-n:24.1.0.255 9 Device type 72 M-Bus client F3(0,0), tag 17
// '0-n:96.1.0';   // Equipment identifier 0-n:96.1.0.255 2 Value 1 Data Sn (n=0..96), tag 9
// '0-n:24.2.1';   // Last 5-minute Meter reading and capture time (e.g. slave E meter) 0-n:24.2.1.255 5 Capture time 4 Extended Register TST 0-n:24.2.1.255 2 Value 4 Extended Register Fn(3,3) (See Note 1) kWh

