# Cloud-GPT: Enterprise Java Meets Generative AI

A demo application showcasing the integration of Jakarta EE 10, MicroProfile, Payara Server/Cloud, and OpenAI's GPT API to build an intelligent travel recommendation system.

## Technologies
- Jakarta EE 10
- MicroProfile Config & Cache
- Payara Server 6
- Payara Cloud deployment support
- OpenAI GPT API integration

## Prerequisites
- Java 21+
- Payara Server 6.2024.10 or later
- Docker

## Configuration
1. Create `microprofile-config.properties`:
```properties
openai.key=${OPENAI_API_KEY}
```

## Deployment Options

### Local Development (Payara Server)
```bash
mvn clean package
docker compose up -d
```

### Payara Cloud
1. Push to your GitHub repository
2. Connect repository to Payara Cloud
3. Deploy with a single click

## Features
- GPT-powered travel recommendations
- Distributed caching via Payara Data Grid
- MicroProfile Config for external configuration
- Cloud-native deployment ready

## Architecture
- Jakarta EE REST endpoints
- GenAI integration via OpenAI client
- JCache for response caching
- Containerized deployment

Access the application at: http://localhost:8080/jee-chatgpt
You can also access the app deployed to Payara Cloud at [Jakarta-101](https://jakarta101.com/jakarta-gpt)

Note: Initial GPT responses may take 30+ seconds. Subsequent identical queries are served from cache.
