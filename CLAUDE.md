# Courier

A Java application that composes and sends a personalized weekly email digest. It runs as an AWS Lambda function and is configured via `courier.yaml`.

## What it does

Reads `courier.yaml`, builds an HTML email from a list of configured widgets, and sends it via AWS SES SMTP.

## Widgets

Each section in `courier.yaml` maps to a widget type:

- `interestRate` — current Fed interest rate and next FOMC meeting date
- `alpacaComparison` — weekly and YTD performance for a list of stock symbols (via Alpaca API)
- `topStories` — top 3 stories from a WSJ RSS feed
- `gregory` — AI-generated market commentary using Claude (Anthropic SDK), fed data from other widgets
- `notionGoals` — goal progress from a Notion database

## Stack

- Java 17, Gradle
- AWS Lambda (`LambdaHandler`) + AWS Secrets Manager for credentials
- Jakarta Mail + AWS SES SMTP for sending
- Freemarker for HTML email templates
- OkHttp for external API calls, Jsoup for HTML parsing

## Configuration

- `src/main/resources/courier.yaml` — controls recipient, subject, and which widgets appear
- `src/main/resources/application.properties` — SMTP settings (or set as env vars)
- Secrets (API keys) fetched at runtime from AWS Secrets Manager

## Build & run

```bash
./gradlew build        # builds fat jar + Lambda zip (courier-lambda.zip)
./gradlew run          # runs locally
```
