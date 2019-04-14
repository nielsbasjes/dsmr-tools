InfluxDB

1) Stream splitter
2) DSMR Parser
3) ReplaceText
Evaluation Mode: Entire text
Replacement strategy: Always replace
Replacement value: electricity electricityPowerReceived=${dsmr.electricityPowerReceived} ${dsmr.timestamp.epochSecond}000000000

4) PutInfluxDB
