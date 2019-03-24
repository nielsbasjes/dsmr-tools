package nl.basjes.dsmr;

public class TestDsmrParser {

    // This is the example testcase documented in the specification.
    // https://www.netbeheernederland.nl/_upload/Files/Slimme_meter_15_a727fce1f1.pdf
    //
    // The example telegram below is based on:
    // - DSMR version 5.0 (value 50 behind OBIS code 1-3:0.2.8.255)
    // - It is send at 2010, December 9th, 11h30m20s
    // - Gas value of 2010, December 9th, 11:25h is presented
    // - 4 power failures in any phase
    // - 2 long power failure in any phase
    // - Power Failure Event log:
    //   Failure at 2010, December 8th, 15h20m15s, duration 240 seconds
    //   Failure at 2010, December 8th, 15h05m03s, duration 301 seconds
    // - 2 voltage sags in phase L1
    // - 1 voltage sag in phase L2 (poly phase meters only)
    // - 0 voltage sags in phase L3 (poly phase meters only)
    // - 0 voltage swells in phase L1
    // - 3 voltage swells in phase L2 (poly phase meters only)
    // - 0 voltage swells in phase L3 (poly phase meters only)
    // - Only one M-Bus device is connected to the Electricity meter.
    //   The register value of the Gas meter is 12785,123 m3.
    //   This value is captured by the G meter at 2010, December 9th,at 11h25m00s Wintertime.
    // - Instantaneous voltage per phase
    // - Instantaneous current per phase
    // - Instantaneous active power (+P) per phase
    // - Instantaneous active power (-P) per phase

    String testcase = "\r\n"+
        "/ISk5\\2MT382-1000\r\n"+
        "\r\n"+
        "1-3:0.2.8(50)\r\n"+
        "0-0:1.0.0(101209113020W)\r\n"+
        "0-0:96.1.1(4B384547303034303436333935353037)\r\n"+
        "1-0:1.8.1(123456.789*kWh)\r\n"+
        "1-0:1.8.2(123456.789*kWh)\r\n"+
        "1-0:2.8.1(123456.789*kWh)\r\n"+
        "1-0:2.8.2(123456.789*kWh)\r\n"+
        "0-0:96.14.0(0002)\r\n"+
        "1-0:1.7.0(01.193*kW)\r\n"+
        "1-0:2.7.0(00.000*kW)\r\n"+
        "0-0:96.7.21(00004)\r\n"+
        "0-0:96.7.9(00002)\r\n"+
        "1-0:99.97.0(2)(0-0:96.7.19)(101208152415W)(0000000240*s)(101208151004W)(0000000301*s)\r\n"+
        "1-0:32.32.0(00002)\r\n"+
        "1-0:52.32.0(00001)\r\n"+
        "1-0:72.32.0(00000)\r\n"+
        "1-0:32.36.0(00000)\r\n"+
        "1-0:52.36.0(00003)\r\n"+
        "1-0:72.36.0(00000)\r\n"+
        "0-0:96.13.0(303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F303132333435363738393A3B3C3D3E3F)\r\n"+
        "1-0:32.7.0(220.1*V)\r\n"+
        "1-0:52.7.0(220.2*V)\r\n"+
        "1-0:72.7.0(220.3*V)\r\n"+
        "1-0:31.7.0(001*A)\r\n"+
        "1-0:51.7.0(002*A)\r\n"+
        "1-0:71.7.0(003*A)\r\n"+
        "1-0:21.7.0(01.111*kW)\r\n"+
        "1-0:41.7.0(02.222*kW)\r\n"+
        "1-0:61.7.0(03.333*kW)\r\n"+
        "1-0:22.7.0(04.444*kW)\r\n"+
        "1-0:42.7.0(05.555*kW)\r\n"+
        "1-0:62.7.0(06.666*kW)\r\n"+
        "0-1:24.1.0(003)\r\n"+
        "0-1:96.1.0(3232323241424344313233343536373839)\r\n"+
        "0-1:24.2.1(101209112500W)(12785.123*m3)\r\n"+
        "!EF2F\r\n"+
        "\r\n";





}
