
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


Sample output of printing out revaluated portfolio:

## 5 Market Data Updates
TSLA changes to 589.977634

Portfolio Update:
Symbol                   |Price     |Qty     |Value
-------------------------------------------------------------------------------------------------
AAPL                     |126.000   |200     |25200.000
AAPL-OCT-2021-120-C      |12.337    |100     |1233.717
AAPL-OCT-2021-120-P      |7.395     |200     |1479.073
TSLA                     |589.978   |100     |58997.763
TSLA-OCT-2021-550-C      |87.537    |20      |1750.749
TSLA-OCT-2021-600-P      |103.258   |-20     |-2065.151

-------------------------------------------------------------------------------------------------
Portfolio Value: 86596.15
**************************************************************************************************
**************************************************************************************************

## 6 Market Data Updates
AAPL changes to 125.982286

Portfolio Update:
Symbol                   |Price     |Qty     |Value
-------------------------------------------------------------------------------------------------
AAPL                     |125.982   |200     |25196.457
AAPL-OCT-2021-120-C      |12.326    |100     |1232.561
AAPL-OCT-2021-120-P      |7.402     |200     |1480.347
TSLA                     |589.978   |100     |58997.763
TSLA-OCT-2021-550-C      |87.537    |20      |1750.749
TSLA-OCT-2021-600-P      |103.258   |-20     |-2065.151

-------------------------------------------------------------------------------------------------
Portfolio Value: 86592.73
**************************************************************************************************


