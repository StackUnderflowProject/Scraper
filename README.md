# Scraper

This project is a web scraper for football ⚽, handball 🤾‍♂️ and basketball 🏀 data.

It fetches and processes data from the Slovenian Football
League ([Prva liga Telemach](https://www.prvaliga.si/tekmovanja/?id_menu=101)),
Handball
League ([1. A DRŽAVNA MOŠKA ROKOMETNA LIGA](https://livestat.rokometna-zveza.si/#/liga/1155/sezona/70/lestvica))
and Basketball League ([Liga Nova KBM](https://www.eurobasket.com/Slovenia/basketball-Liga-Nova-KBM.aspx)) websites.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing
purposes.

### Prerequisites

- Kotlin 🇰
- Java ♨️
- Gradle 🐘

### Installing

1. Clone the repository
2. Navigate to the project directory
3. Run `gradle build` to build the project

## Usage

The main entry point of the application is the `main` function in `Main.kt`.

The project includes several scrapers:

- `KZSScraper`: Scrapes data from the KZS website
- `PLTScraper`: Scrapes data from the PLT website
- `RZSScraper`: Scrapes data from the RZS website

Each scraper has methods to fetch different types of data:

- `getTeams()`: Fetches team data
- `getStadiums()`: Fetches arena data
- `getStandings()`: Fetches standings data
- `getMatches()`: Fetches match data

There is also a `saveAllData()` method that fetches all data and saves it in a specified file format (CSV , XML, or
JSON).

## Acknowledgments

- Thanks to the Slovenian Football, Handball and Basketball League for providing the data 
- Thanks to all contributors to this project
- Special thanks to your mother for anal support
- 
