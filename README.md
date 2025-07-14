## Introduction

This repository contains an experimental Temporal + [Spring AI](https://spring.io/projects/spring-ai) integration. 

Current Features are:
* Easily call AI models from withing a Temporal Workflow.
* First class integration with the Chat client model and conversation history.
* Automatically converting activities, local activities, nexus operations and child workflows (TBD) into tools.
* Support vector stores and vector search for RAG and context fetching.

## Note

This is an experimental project I mostly worked on during evening and weekends, it contains lots of TODOs and is not production ready. It is meant to be a proof of concept and a starting point for a conversation starter.

This project contains both the Temporal + Spring AI integration and a sample application that uses it to build a simple chat bot that can answer questions with a very small set of tools.

## Getting Started

### Prerequisites

* [Gradle](https://gradle.org/) 8.0+
* Java 21+
* Temporal cloud API key
* Open AI API key

### Running the Example

To see the Workflow code go [here](src/main/java/io/temporal/ai/workflows/ChatWorkflowImpl.java)


Set the following environment variables:
* `TEMPORAL_API_KEY` - Your Temporal cloud API key
* `OPENAI_API_KEY` - Your Open AI API key
* `TEMPORAL_NAMESPACE` - The Temporal namespace to use (default: `default`)
* `TEMPORAL_ADDRESS` - The Temporal host to connect to (default: `localhost`)


```bash
./gradlew bootRun
```

In another terminal, you can run the Temporal CLI to start the workflow:

```bash
temporal workflow start --type ChatWorkflow --task-queue temporal-spring-ai-chat-taskqueue --input '"You are a friendly chat bot that answers question in the voice of a Pirate"' --workflow-id PirateChat --tls
```
You can then interact with the chat bot by sending update requests to it:

```bash
 temporal workflow update execute --workflow-id PirateChat --name ask --input '"Can you set me an alarm to fire in two hours and tell me when"' --tls
```

Note: The Temporal CLI will need the appropriate connection settings to connect to your Temporal server.

## To Do

* Add documentation
* Add tests
* Figure out the error conversion story
* Figure out activity options story
* Allow passing specific models options from the workflow to activity
* Test with different AI models
* Add support for tool context
* Add support for observation
* Add support for child workflows
* Improve support for MCP
* Clean up the serialization code and logic
* Add support for automatic summary population
* Clean up conditional registration
* Support versioned removal of tools
* Improve support for media
* Add specific Temporal chat model options
* Separate out into library, auto-starter, and sample application