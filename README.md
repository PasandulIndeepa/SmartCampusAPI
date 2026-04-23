# SmartCampusAPI

A RESTful API for managing Rooms and Sensors across a Smart Campus, built using **JAX-RS (Jersey 2.32)** on **Apache Tomcat 9**.

---

## Table of Contents

- [API Overview](#api-overview)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [How to Build and Run](#how-to-build-and-run)
- [API Endpoints](#api-endpoints)
- [Sample curl Commands](#sample-curl-commands)
- [Error Handling](#error-handling)

---

## API Overview

The Smart Campus API provides a backend service for campus facilities managers to monitor and manage:

- **Rooms** - Physical spaces on campus (labs, libraries, halls)
- **Sensors** - Hardware devices deployed inside rooms (temperature, CO2, occupancy)
- **Sensor Readings** - Historical measurement logs recorded by each sensor

The API follows REST principles with a versioned base path at `/api/v1`. All responses are in JSON format. The data is stored entirely in-memory using `HashMap` and `ArrayList` — no database is required.

### Key Design Decisions

- **Resource Hierarchy:** Sensor readings are modelled as sub-resources of sensors (`/sensors/{id}/readings`), reflecting the physical relationship between sensors and their data.
- **HATEOAS:** The discovery endpoint at `GET /api/v1/` returns links to all primary resource collections, allowing clients to navigate the API without hardcoding URLs.
- **Error Handling:** All errors return structured JSON bodies with an error message, HTTP status code, and a documentation link. Raw stack traces are never exposed.
- **Logging:** Every request and response is logged via a JAX-RS filter without any logging code inside resource methods.

---

## Project Structure

```
SmartCampusAPI/
└── src/main/java/com/smartcampus/
    ├── MyApplication.java                          # JAX-RS entry point (@ApplicationPath)
    ├── dao/
    │   └── DataStore.java                          # In-memory HashMap storage
    ├── model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   ├── SensorReading.java
    │   └── ErrorMessage.java
    ├── resource/
    │   ├── DiscoveryResource.java                  # GET /api/v1/
    │   ├── RoomResource.java                       # /api/v1/rooms
    │   ├── SensorResource.java                     # /api/v1/sensors
    │   └── SensorReadingResource.java              # /api/v1/sensors/{id}/readings
    ├── exception/
    │   ├── RoomNotEmptyException.java
    │   ├── RoomNotEmptyExceptionMapper.java        # 409 Conflict
    │   ├── LinkedResourceNotFoundException.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java  # 422 Unprocessable Entity
    │   ├── SensorUnavailableException.java
    │   ├── SensorUnavailableExceptionMapper.java   # 403 Forbidden
    │   └── GlobalExceptionMapper.java              # 500 Internal Server Error
    └── filter/
        └── LoggingFilter.java                      # Logs all requests and responses
```

---

## Technology Stack

| Technology | Version |
|------------|---------|
| Java | 11 |
| JAX-RS (Jersey) | 2.32 |
| Apache Tomcat | 9 |
| Jackson (JSON) | via jersey-media-json-jackson |
| Build Tool | Maven |

---

## How to Build and Run

### Prerequisites

- JDK 11 or higher installed
- Apache Tomcat 9 installed and configured in NetBeans
- NetBeans IDE (with Maven support)
- Internet connection (for Maven to download dependencies on first build)

### Step 1 — Clone the Repository

```bash
git clone https://github.com/PasandulIndeepa/SmartCampusAPI.git
cd SmartCampusAPI
```

### Step 2 — Open in NetBeans

1. Open **NetBeans IDE**
2. Click **File → Open Project**
3. Navigate to the cloned `SmartCampusAPI` folder
4. Click **Open Project**

### Step 3 — Verify Tomcat is Configured

1. In NetBeans go to **Tools → Servers**
2. Confirm **Apache Tomcat 9** is listed
3. If not, click **Add Server → Apache Tomcat** and point it to your Tomcat installation folder

### Step 4 — Build the Project

1. Right click the project in the **Projects** panel
2. Click **Clean and Build**
3. Wait for Maven to download dependencies
4. Confirm you see **BUILD SUCCESS** in the output panel

### Step 5 — Run the Project

1. Right click the project
2. Click **Run**
3. NetBeans will deploy the WAR to Tomcat automatically
4. Tomcat starts on port **8080**

### Step 6 — Verify it's Running

Open your browser and go to:

```
http://localhost:8080/SmartCampusAPI/api/v1/
```

You should see a JSON response with API metadata.

---

## API Endpoints

### Base URL
```
http://localhost:8080/SmartCampusAPI/api/v1
```

### Discovery

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Returns API metadata and resource links |

### Rooms

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/rooms` | Get all rooms | 200 |
| POST | `/rooms` | Create a new room | 201 |
| GET | `/rooms/{roomId}` | Get a specific room | 200 / 404 |
| DELETE | `/rooms/{roomId}` | Delete a room (fails if sensors exist) | 200 / 409 |

### Sensors

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/sensors` | Get all sensors | 200 |
| GET | `/sensors?type={type}` | Filter sensors by type | 200 |
| POST | `/sensors` | Register a new sensor | 201 / 422 |
| GET | `/sensors/{sensorId}` | Get a specific sensor | 200 / 404 |

### Sensor Readings

| Method | Endpoint | Description | Status |
|--------|----------|-------------|--------|
| GET | `/sensors/{sensorId}/readings` | Get all readings for a sensor | 200 / 404 |
| POST | `/sensors/{sensorId}/readings` | Add a new reading | 201 / 403 / 404 |

---

## Sample curl Commands

### 1. Get API Discovery Info
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/
```

### 2. Get All Rooms
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

### 3. Create a New Room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"HALL-101\", \"name\": \"Main Hall\", \"capacity\": 100}"
```

### 4. Get All Sensors Filtered by Type
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### 5. Register a New Sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-002\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 21.0, \"roomId\": \"LAB-101\"}"
```

### 6. Add a Sensor Reading
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"READ-001\", \"value\": 25.5, \"timestamp\": 1714000000000}"
```

### 7. Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings
```

### 8. Attempt to Delete a Room with Sensors (409 Error)
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 9. Attempt to Create Sensor with Invalid Room (422 Error)
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d "{\"id\": \"TEMP-999\", \"type\": \"Temperature\", \"status\": \"ACTIVE\", \"currentValue\": 20.0, \"roomId\": \"FAKE-ROOM\"}"
```

---

## Error Handling

All errors return a structured JSON body:

```json
{
    "errorMessage": "Description of what went wrong",
    "errorCode": 409,
    "documentation": "https://smartcampus.ac.uk/api/docs/errors"
}
```

| Scenario | Exception | HTTP Status |
|----------|-----------|-------------|
| Delete room that has sensors | `RoomNotEmptyException` | 409 Conflict |
| Sensor references non-existent room | `LinkedResourceNotFoundException` | 422 Unprocessable Entity |
| POST reading to MAINTENANCE sensor | `SensorUnavailableException` | 403 Forbidden |
| Any unexpected runtime error | `GlobalExceptionMapper` | 500 Internal Server Error |

---

## Pre-loaded Sample Data

The API starts with the following data in memory:

**Rooms:**
- `LIB-301` — Library Quiet Study (capacity: 50)
- `LAB-101` — Computer Lab (capacity: 30)

**Sensors:**
- `TEMP-001` — Temperature sensor, ACTIVE, in LIB-301
- `CO2-001` — CO2 sensor, ACTIVE, in LAB-101
- `HUM-001` — Humidity sensor, MAINTENANCE, in LIB-301
