This is intended as an overview of the major changes

v0.7-SNAPSHOT
===
- Several fields are cleaned
  - `p1Version`: "42" --> "4.2"
  - `ident` renamed to `rawIdent`, parsed into `equimpentBrandTag` and `ident`.

v0.6
===
- Drop the water/cold/heat fields as they are not used.
- Partial support for DMSR 2.2 style records (no P1 version, no timestamp, no CRC, different way of including gas measurements).
- Update to (Mi)NiFi 1.15.0

v0.5
===
- MBus handle absent unit
- Support DSMR 4.2 message codes i.e. 0-0:96.13.1..

v0.4
===
- Fix parsing a device name with a space ' ' or dot '.' in it.
- Parse the list of power failures (including Nifi support)

v0.3
===
- Changed the MBus channel Device type from a String to an Integer.
- Added basic support to also accept kWh for MBus devices.

v0.2
===
- Changed ElectricityTariffIndicator from Double to Long

v0.1
===
- Initial release as I use it at home.

License
=======
    Dutch Smart Meter Requirements (DSMR) Toolkit
    Copyright (C) 2019-2021 Niels Basjes

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

    https://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
