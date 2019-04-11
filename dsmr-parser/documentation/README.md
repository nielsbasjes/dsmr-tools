Download via 
https://www.netbeheernederland.nl/dossiers/slimme-meter-15/documenten
the file
https://www.netbeheernederland.nl/_upload/Files/Slimme_meter_15_a727fce1f1.pdf


A big unknown was the Device Type field used when an MBus attached device is used.

Although I'm not 100% sure the only documentation that I have found (which also happens to match the only example telegrams I have) was found here: http://www.m-bus.com/mbusdoc/md8.php

The only match is the "gas" = "3" case.

8.4.1 Measured Medium Variable Structure
 
| Medium | Code bin. Bit 7 .. 0 | Code hex. |
| --- | --- | --- |
|Other | 0000 0000 |00
|Oil|0000 0001|01
|Electricity|0000 0010|02
|Gas|0000 0011|03
|Heat (Volume measured at return temperature: outlet)|0000 0100|04
|Steam|0000 0101|05
|Hot Water|0000 0110|06
|Water|0000 0111|07
|Heat Cost Allocator.|0000 1000|08
|Compressed Air|0000 1001|09
|Cooling load meter (Volume measured at return temperature: outlet) |0000 1010|0A
|Cooling load meter (Volume measured at flow temperature: inlet) |0000 1011|0B
|Heat (Volume measured at flow temperature: inlet)|0000 1100|0C
|Heat / Cooling load meter §|0000 1101|OD
|Bus / System|0000 1110|0E
|Unknown Medium|0000 1111|0F
|Reserved|..........|10 to 15
|Cold Water|0001 0110|16
|Dual Water|0001 0111|17
|Pressure|0001 1000|18
|A/D Converter|0001 1001|19
|Reserved|..........|20 to FF

