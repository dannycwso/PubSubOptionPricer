
Project: Implement a publisher to simulate stock price random-walking in GBM. The stock ticks are consummed by a subscriber which re-price options using BS-model if exist and then whole portfolio value.

Implementation Highlights:
- The project provides 2 implementations. 1st approach is to use Aeron IPC as communication channel between publisher and subscriber which is the recommended approach of this assignment
- The 2nd approach is to use Discruptor ring buffer to wire the publisher and subscriber. Per requirement, the publisher and subscriber can be running in the same process. This approach can be simpler and faster in term of performance.
- Read the AeronAgentRunnerMain.java and DiscruptorMain.java as the starting point to read these 2 approaches respectively
- To read the GBM and Option pricer codes, can read the StockPriceGBMGeneratorImpl.java and BSOptionPricerImpl.java
- Per requirement, the publisher produces a stock tick randomly in a time range (0.5 to 2 sec). The stock picked for price movement is also randomly selected from the list of stocks pre-loaded in CSV or DB file
- Per requirement, it is recommended to read the security definition and position data from SQLite DB and CSV respectively. There is a sample security_def DB file and position.csv file in the source code.
- There is a sql.txt in the project which contains the SQL statements to create the table and insert some test data to the table.
- During the initial development, I developed codes to read security definition from CSV as well (sample security_def.csv is in project). I still keep the codes in the PositionManager.java. 
At present, by default, the program is to read security definition from DB file in order to comply with the requirement. But to enable the implementation to read all test data from CSV files, need to change a boolean flag in the Main classes and recompile to make it effective.
- Enjoy the codes and feel free to provide feedback.

Build:
- Java 8 and Maven (a pom file is in the project)

3rd party libs:
- aeron, agrona, disruptor, apache common csv and maths, junit, Logback, sqlite-jdbc. They all can be found in pom.xml

TODO:
- Enjoy the codes and feel free to provide feedback. Let's see what can be further improved per discussion. 


