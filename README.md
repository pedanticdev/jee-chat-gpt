# BudgetJourney - Discover Your Dream City Within Your Budget

Imagine you want to visit a city and have a specific budget in mind. BudgetJourney is an app designed to suggest multiple points of interest within the city, tailored to fit your budget constraints.

This Java-based microservice application leverages the OpenAI GPT API to generate recommendations for points of interest. To optimize costs and reduce the volume of requests to the GPT API, all previous suggestions are cached using the Payara Data Grid through JCache.

## Prerequisite

* Java 1/17 (Java EE 8, Jakarta EE 10): https://sdkman.io


## Configuration

Open the `microprofile-config.properties` file and provide the following configuration settings:

1. OpenAI API key:
    ```shell
    openai.key={YOUR_API_KEY}
    ```

    ```

## Usage

Start the app from a terminal:
```shell
docker compose up -d
mvn mvn clean package -DskipTests  -Pproduction && /usr/bin/cp -f target/*.war deployments 
```

The application should automatically open a browser window on the following address: http://localhost:8080/jee-chatgpt

<img width="1505" alt="tokyo" src="https://user-images.githubusercontent.com/1537233/228314699-dfd48764-3565-4dca-8875-caf9ae3f3e8b.png">

To experience what BudgetJourney has to offer, simply provide a city name and budget limit. Please note that it may take 30 seconds or more for the OpenAI GPT to generate suggestions. However, to enhance efficiency, all previous suggestions are cached using JCache and will be served from there whenever you inquire about the same city and budget again.
