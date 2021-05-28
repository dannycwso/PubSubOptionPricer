
Project:
The assignment is to design a market data publisher sending stock tick randomly between configured time range. The stock prices have to "random-walk" in Geometric Brownian Motion. Also, design a receiver to pick up the stock ticks and re-price the European option in the portfolio as well as entire portfolio value using Black-Scholes Model. More importantly, it is recommended to use non-blocking (low-latency) data structure to wire the publisher and receiver.

Implementation Highlights:
- The project provides 2 implementations. 1st approach is to use Aeron IPC as communication channel between publisher and subscriber which is the recommended approach of this assignment
- The 2nd approach is to use Discruptor ring buffer to wire the publisher and subscriber. Per requirement, the publisher and subscriber can be running in the same process. This approach can be simpler and faster in term of performance.
- Read the AeronAgentRunnerMain.java and DiscruptorMain.java as the starting point to read these 2 approaches respectively
- To read the GBM and Option pricer codes, can read the StockPriceGBMGeneratorImpl.java and BSOptionPricerImpl.java
- Per requirement, the publisher produces a stock tick randomly in a pre-defined time range. The stock picked for price movement is also randomly selected from the list of stocks pre-loaded in CSV or DB file
- During the initial development, I had developed codes to read security definition from CSV. (sample security_def.csv is in project). I still keep the codes in the PositionManager.java. 
At present, by default, the program is to read security definition from DB file in order to comply with the requirement. But to enable the implementation to read all test data from CSV files, need to change a boolean flag in the Main classes and recompile to make it effective.

Build:
- Java 8 and Maven (a pom file is in the project)

3rd party libs:
- aeron, agrona, disruptor, apache common csv and maths, junit, Logback, sqlite-jdbc. They all can be found in pom.xml

TODO:
- Enjoy the codes and feel free to provide feedback. Let's see what can be further improved


