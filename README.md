# rest-example

This project is a REST API example for managing music tracks. It is built with Java using Jersey for REST services, Grizzly as the HTTP server, and Swagger for API documentation.

## High-Level Server Structure

```mermaid
graph TD
    A[HTTP Client] --> B[Grizzly Server]
    B --> C[Static Content Handler<br/>/public]
    C --> D[Public Files<br/>index.html]
    C --> E[Swagger UI<br/>/public/swagger]
    B --> F[REST API Handler<br/>/dsaApp]
    F --> G[TracksService<br/>TextService]
```

## Main Architecture

```mermaid
graph TD
    A[HTTP Client] --> B[TracksService]
    A --> C[TextService]
    B --> D[TracksManager]
    D --> E[Track Model]
    F[Main] --> G[Grizzly Server]
    G --> B
    G --> C
    H[MyExceptionMapper] --> G
```

### Main Components:
- **TracksService**: REST service that exposes endpoints to manage tracks (GET, POST, etc.).
- **TextService**: Simple REST service for text responses.
- **TracksManager**: Interface and implementation for the business logic of track management.
- **Track**: Data model representing a music track.
- **MyExceptionMapper**: Exception mapper to handle API errors.
- **TrackNotFoundException**: Custom exception for tracks not found.
