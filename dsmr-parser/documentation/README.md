# Specification

## Main document
I downloaded via
https://www.netbeheernederland.nl/dossiers/slimme-meter-15/documenten
these files of for "P1 Companion Standard"
- Version 4.2.2 : https://www.netbeheernederland.nl/_upload/Files/Slimme_meter_15_32ffe3cc38.pdf
- Version 5.0.2 : https://www.netbeheernederland.nl/_upload/Files/Slimme_meter_15_a727fce1f1.pdf

Older versions (with some now deprecated fields):
- Version 3.0 was sent to me by email by netbeheernederland.nl (Big thanks!) and is said to be the same as DSMR 2.2 with some bugfixes.
- Version 4.0 was downloaded from http://files.domoticaforum.eu/uploads/Smartmetering/DSMR%20v4.0%20final%20P1.pdf

## MBus Device type

A big unknown in this specification was the Device Type field of an attached MBus device.

Based mainly on the answers from Netbeheer Nederland only these values actually occur:

    Meter type description                          Device type (hex)
    Electricity Meter                               02
    Gas meter                                       03

### Answer from netbeheernederland.nl in december 2019 (in Dutch)

In December 2019 I emailed Netbeheer Nederland, and they kindly clarified this issue (in Dutch)

    De mogelijke waarden voor device types staan de in achterliggende standaard EN-13757-3 tabel 6 (M-Bus).
    In gebruik in de Nederlandse slimme meter zijn:

    Meter type description                          Device type (hex)
    Gas meter                                       03
    Water meter                                     07
    Electricity Meter                               02
    Heat                                            04
    Warm water (30 – 90 degrees C)                  06
    Hot water (>=90 degrees C)                      15
    Dual register meter (Hot/Cold water meter)      17

    Wat betreft de voorbeelden voor water en warmte/koude meters; die zijn er niet.
    Alhoewel de DSMR het wel mogelijk maakt om ook deze meters via de E-meter uit te lezen wordt er in de
    praktijk geen gebruik van gemaakt omdat water en warmte/koude geen onderdeel zijn van de E/G netbeheerders.

My simplified English translation:

    The possible values are defined in EN-13757-3 tabel 6 (M-Bus) yet in the Dutch smart meters only these values
    are actually used:

    Meter type description                          Device type (hex)
    Gas meter                                       03
    Water meter                                     07
    Electricity Meter                               02
    Heat                                            04
    Warm water (30 – 90 degrees C)                  06
    Hot water (>=90 degrees C)                      15
    Dual register meter (Hot/Cold water meter)      17

    There are no examples available for the water and hot/cold meters because that is simply never used
    because these are supplied by other companies that do not use these measuring systems.

### Answer from m-bus.com

I found additional information on http://www.m-bus.com/

The only match is the "gas" = "3" case.

| Medium | Code bin. Bit 7 .. 0 | Code hex. |
| --- | --- | --- |
|Other | 0000 0000 |00
|Oil|0000 0001|01
|Electricity|0000 0010|02
|Gas|0000 0011|03
|Heat (Volume measured at return temperature: outlet)|0000 0100|04
|Steam|0000 0101|05
|Warm Water (30-90 Celcius)|0000 0110|06
|Water|0000 0111|07
|Heat Cost Allocator|0000 1000|08
|Compressed Air|0000 1001|09
|Cooling load meter (Volume measured at return temperature: outlet)|0000 1010|0A
|Cooling load meter (Volume measured at flow temperature: inlet)|0000 1011|0B
|Heat (Volume measured at flow temperature: inlet)|0000 1100|0C
|Heat / Cooling load meter|0000 1101|OD
|Bus / System|0000 1110|0E
|Unknown Medium|0000 1111|0F
|Reserved|..........|10 to 14
|Hot Water (>90 Celsius)|0001 0101|15
|Cold Water|0001 0110|16
|Dual register (hot/cold) Water|0001 0111|17
|Pressure|0001 1000|18
|A/D Converter|0001 1001|19
|Reserved|..........|20 to FF

<!--
    Copyright (C) 2019-2024 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

        https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
-->
