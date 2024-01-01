# Real-time Stock Data Streaming and Analysis with Apache Flink

This project is an Apache Flink application that continuously consumes and analyzes real-time stock market data. It leverages the RapidAPI to fetch stock prices for companies such as Tesla (TSLA), Netflix (NFLX). The application computes the average stock prices over sliding time windows, providing an aggregate view of market trends over short intervals.

## Author
- Ibrahim Traoré

## Features

- Real-time data retrieval via RapidAPI.
- Calculation of average stock prices using sliding windows.
- Persistence of analytical results to the filesystem for further analysis.
- Uses Matplotlib's animation functionality to update stock price graphs in real-time.

## Prerequisites

Before running the application, make sure to set up the following :
- Java 11
- IntelliJ IDEA with Scala plugin
- Apache Flink dependencies added to your project

## Installation

1. Clone the repository to your local machine.
2. Configure `application.conf` with your RapidAPI key in file `src/main/resources/application.conf`
````
API_KEY = --RealStoncks API key--
````
3. Start flink server
````bash
sbt run
````
4. Run python script. Make sure to wait for flink to generate necessary files.
````bash
python plot.py
````
The output of visualisation should look similar to this,
<img src="output.png"/>

## Usage

Once the application is started, it will begin processing the data streams of specified stocks and write the results into files within the configured directory.

## Contributing

Contributions are what make the open-source community such an amazing place to learn, inspire, and create. Any contributions you make are **greatly appreciated**.

If you have a suggestion that would improve this, please fork the repo and create a pull request. You can also simply open an issue with the tag "enhancement".

Don't forget to give the project a star! Thanks again!

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## License

Distributed under the MIT License. See `LICENSE` for more information.
 