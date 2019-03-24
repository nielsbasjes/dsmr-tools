Configuration settings
====

The USB serial MUST be set to 'raw' or the crc checksum will NEVER match.

    stty -F /dev/ttyUSB0 115200 raw
