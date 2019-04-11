Configuration settings
====

The USB serial MUST be set to 'raw' or the crc checksum will NEVER match.

    stty -F /dev/ttyUSB0 115200 raw

NOTES
====
http://www.lipin.nl/DSMRBinding.html

- Gas meter values
- Thermal meter (heating) values
- Thermal meter (cooling) values
- Water meter values
- Generic meter values
- Slave electricity meter values

So heating and cooling are separate. Also there is a "Generic".
