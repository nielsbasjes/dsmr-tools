Status: Unstable, work in progress, DO NOT USE ... (yet)
===

DSMR Toolkit
====
DSMR are the Dutch Smart Meter Requirements; part of these requirements are the messages sent out via the P1 port that can be captured and processed on a computer.

Many tools already exist to do this but so far I have not found one that allows me to easily select the system the messages are to be sent to.

This is going to be a set of Java based libraries and UDFs that should allow processing DSMR messages in a way that is as flexible and reuable as possible.
As part of this there will be a few Nifi processors in order to support this flexibility.
  
...

LICENSE
===

   Copyright 2019 Niels Basjes

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       https://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
