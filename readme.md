# Courier

👋 Hi! Welcome to the codebase for Courier - a configurable Java application that sends a personalized newsletter with
data relevant to my goals.

(Insert logo banner here)

## Background

The idea for Courier began with how I set my goals for 2026. My system has many layers where I set large yearly goals
that boil down to monthly goals. My monthly goals are made up of weekly goals and my weekly goals are made up of daily
goals. These are all written as lists and when I reach a goal of any type, I cross it off the list. This is a system
that I have used and built on for years. Recently, the system has grown to a point where I often try to answer a single
question: how are things going? Courier aims to answer that question.

## How it works

Courier emails me every Sunday morning with any data that I consider relevant to my goals. This is typically financial
metrics indicating the health of the tech sector, relevant news stories about the broader markets, and progress reports
on my yearly goals. The Sunday morning schedule allows me to wake up on a morning when I am well rested, see how things
are going, and decide what my goals for the new week will be.

(Insert courier email images here)

## Features:

- Customizable layout of widgets
- Data from a variety of APIs
- Insights from Gregory the virtual analyst :)

## Tech stack

![Java](https://img.shields.io/badge/-Java-000?&logo=openjdk&logoColor=007396)
![Gradle](https://img.shields.io/badge/-Gradle-000?&logo=Gradle)
![HTML](https://img.shields.io/badge/-HTML-000?&logo=HTML5)
![AWS](https://custom-icon-badges.demolab.com/badge/-AWS-000?style=flat&logo=aws&logoColor=FF9900)
![GitHub Actions](https://img.shields.io/badge/-GitHub%20Actions-000?&logo=GitHub)

## Application design

The Courier application follows a builder design pattern where each widget in courier.yaml has a corresponding builder
class in Java. The builder classes generate the corresponding widgets with Apache Freemarker templates and the widgets
are added to the final email.

### courier.yaml example

```
recipient: "john.doe@gmail.com"
subject: "Your weekly report"

sections:
  - type: "alpacaComparison"
    symbols: [ "QQQ", "SPY", "NVDA" ]
  - type: "interestRate"
  - type: "topStories"
  - type: "gregory"
  - type: "notionGoals"
  ```

<img width="1440" height="1024" alt="application-diagram" src="https://github.com/user-attachments/assets/aac2877a-64c4-4057-8b75-af526fee40f7" />

## Hosting

Hosting is built on AWS with Lambda doing most of the heavy lifting. Supporting services like EventBridge, Secrets
Manager, and Simple Email Service(SES) are also used.

<img width="1440" height="1024" alt="aws-diagram" src="https://github.com/user-attachments/assets/e5d119a9-7712-4d5c-afc0-10495fb68dc1" />

