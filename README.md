# SmartCampusAPI

A RESTful API for managing campus rooms and IoT environmental sensors, built with **Java 11**, **JAX-RS (Jersey 2.32)**, and deployable on **Apache Tomcat**.

---

## API Design Overview

### Architecture

The API follows a classic **three-layer JAX-RS architecture**:

```
Client Request
      │
      ▼
 Resource Layer          ← JAX-RS annotated classes (@Path, @GET, @POST, …)
      │
      ▼
 DAO / DataStore Layer   ← Singleton in-memory HashMap (thread-shared state)
      │
      ▼
 Model Layer             ← Plain Java Objects: Room, Sensor, SensorReading
```

- **Framework:** Jersey 2.32 (JAX-RS reference implementation)  
- **Transport:** JSON via Jackson (`jersey-media-json-jackson`)  
- **Base path:** `/api/v1` (configured via `@ApplicationPath` and `web.xml`)  
- **Storage:** In-memory `HashMap` within a static `DataStore` class (data is pre-seeded on startup; state is reset on server restart)

### Resource Hierarchy

```
/api/v1/
├── /rooms
│   ├── GET    – List all rooms
│   ├── POST   – Create a new room
│   └── /{roomId}
│       ├── GET    – Get a specific room
│       └── DELETE – Delete a room (fails if sensors are still assigned)
│
├── /sensors
│   ├── GET    – List all sensors (supports ?type= query filter)
│   ├── POST   – Create a new sensor (validates that the parent room exists)
│   └── /{sensorId}
│       ├── GET – Get a specific sensor
│       └── /readings
│           ├── GET  – List all readings for a sensor
│           └── POST – Submit a new reading (blocked if sensor is MAINTENANCE)
│
└── /          – Discovery endpoint (API version + resource links)
```

### Data Models

#### Room
| Field       | Type           | Description                          |
|-------------|----------------|--------------------------------------|
| `id`        | `String`       | Unique room identifier (e.g. `LIB-301`) |
| `name`      | `String`       | Human-readable room name             |
| `capacity`  | `int`          | Maximum occupancy                    |
| `sensorIds` | `List<String>` | IDs of sensors assigned to this room |

#### Sensor
| Field          | Type     | Description                                     |
|----------------|----------|-------------------------------------------------|
| `id`           | `String` | Unique sensor identifier (e.g. `TEMP-001`)      |
| `type`         | `String` | Sensor category (e.g. `Temperature`, `CO2`, `Humidity`) |
| `status`       | `String` | `ACTIVE` or `MAINTENANCE`                       |
| `currentValue` | `double` | Most recently recorded value                    |
| `roomId`       | `String` | Parent room this sensor belongs to              |

#### SensorReading
| Field       | Type     | Description                              |
|-------------|----------|------------------------------------------|
| `id`        | `String` | Auto-generated UUID                      |
| `timestamp` | `long`   | Unix epoch milliseconds (auto-set)       |
| `value`     | `double` | The measured sensor value                |

### Business Rules & Custom Exceptions

| Rule | HTTP Status | Exception |
|------|-------------|-----------|
| A room cannot be deleted if it still has sensors assigned | `409 Conflict` | `RoomNotEmptyException` |
| A sensor cannot be created with a non-existent `roomId` | `422 Unprocessable Entity` | `LinkedResourceNotFoundException` |
| A reading cannot be submitted to a sensor in `MAINTENANCE` status | `403 Forbidden` | `SensorUnavailableException` |
| Duplicate IDs on POST | `409 Conflict` | Inline response |
| Resource not found | `404 Not Found` | Inline response |

All exceptions are handled globally via `GlobalExceptionMapper` and return a JSON `ErrorMessage` body.

### Pre-Seeded Data (Available Immediately on Startup)

| Type    | ID         | Details |
|---------|------------|---------|
| Room    | `LIB-301`  | Library Quiet Study, capacity 50 |
| Room    | `LAB-101`  | Computer Lab, capacity 30 |
| Sensor  | `TEMP-001` | Temperature, ACTIVE, 22.5, in LIB-301 |
| Sensor  | `CO2-001`  | CO2, ACTIVE, 400.0, in LAB-101 |
| Sensor  | `HUM-001`  | Humidity, MAINTENANCE, 60.0, in LIB-301 |

---

##  Prerequisites

Ensure the following are installed before proceeding:

| Tool | Version | Download |
|------|---------|---------|
| JDK  | 11 or higher | https://adoptium.net |
| Apache Maven | 3.6+ | https://maven.apache.org/download.cgi |
| Apache Tomcat | 9.x | https://tomcat.apache.org/download-90.cgi |

Verify your installations:
```bash
java -version
mvn -version
```

---

##  Build & Launch Instructions

### Step 1 — Clone the Repository

```bash
git clone https://github.com/PasandulIndeepa/SmartCampusAPI.git
cd SmartCampusAPI
```

### Step 2 — Build the WAR file with Maven

```bash
mvn clean package
```

This compiles all source files and produces the deployable archive at:
```
target/SmartCampusAPI-1.0-SNAPSHOT.war
```

### Step 3 — Deploy to Apache Tomcat

Copy the generated WAR file into Tomcat's `webapps` directory:

**On Windows:**
```cmd
copy target\SmartCampusAPI-1.0-SNAPSHOT.war C:\apache-tomcat-9.x.x\webapps\SmartCampusAPI.war
```

**On macOS / Linux:**
```bash
cp target/SmartCampusAPI-1.0-SNAPSHOT.war /opt/tomcat/webapps/SmartCampusAPI.war
```

### Step 4 — Start the Tomcat Server

**On Windows:**
```cmd
C:\apache-tomcat-9.x.x\bin\startup.bat
```

**On macOS / Linux:**
```bash
/opt/tomcat/bin/startup.sh
```

### Step 5 — Verify the Server is Running

Open your browser and navigate to:
```
http://localhost:8080/SmartCampusAPI/api/v1/
```

You should receive a JSON response like:
```json
{
  "name": "Smart Campus API",
  "version": "1.0",
  "description": "Sensor and Room Management API",
  "resources": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```

### (Optional) Alternative — Deploy via Tomcat Manager

1. Open `http://localhost:8080/manager/html` in your browser.
2. Log in with your Tomcat manager credentials.
3. Scroll to the **"WAR file to deploy"** section.
4. Browse to `target/SmartCampusAPI-1.0-SNAPSHOT.war` and click **Deploy**.

### Stopping the Server

**On Windows:**
```cmd
C:\apache-tomcat-9.x.x\bin\shutdown.bat
```

**On macOS / Linux:**
```bash
/opt/tomcat/bin/shutdown.sh
```

---

##  Base URL

All endpoints are relative to:
```
http://localhost:8080/SmartCampusAPI/api/v1
```

---

##  Sample cURL Commands

> **Note:** The server pre-seeds sample data (`LIB-301`, `TEMP-001`, etc.) on startup, so these commands work immediately without any prior setup.

---

### 1. Get All Rooms

Retrieve the full list of campus rooms currently stored in the system.

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms \
     -H "Accept: application/json"
```

**Expected Response (`200 OK`):**
```json
[
  {
    "id": "LIB-301",
    "name": "Library Quiet Study",
    "capacity": 50,
    "sensorIds": ["TEMP-001", "HUM-001"]
  },
  {
    "id": "LAB-101",
    "name": "Computer Lab",
    "capacity": 30,
    "sensorIds": ["CO2-001"]
  }
]
```

---

### 2. Create a New Room

Register a new campus room with a unique ID, name, and seating capacity.

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
     -H "Content-Type: application/json" \
     -H "Accept: application/json" \
     -d '{
           "id": "HALL-202",
           "name": "Lecture Hall B",
           "capacity": 120
         }'
```

**Expected Response (`201 Created`):**
```json
{
  "id": "HALL-202",
  "name": "Lecture Hall B",
  "capacity": 120,
  "sensorIds": []
}
```

---

### 3. Create a New Sensor (linked to an existing room)

Register a new temperature sensor and assign it to a room. The `roomId` must refer to an existing room; otherwise, the API returns `422 Unprocessable Entity`.

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
     -H "Content-Type: application/json" \
     -H "Accept: application/json" \
     -d '{
           "id": "TEMP-002",
           "type": "Temperature",
           "status": "ACTIVE",
           "currentValue": 21.0,
           "roomId": "HALL-202"
         }'
```

**Expected Response (`201 Created`):**
```json
{
  "id": "TEMP-002",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 21.0,
  "roomId": "HALL-202"
}
```

---

### 4. Filter Sensors by Type

Retrieve only sensors of a specific type using the `?type=` query parameter.

```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=Temperature" \
     -H "Accept: application/json"
```

**Expected Response (`200 OK`):**
```json
[
  {
    "id": "TEMP-001",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 22.5,
    "roomId": "LIB-301"
  }
]
```

---

### 5. Submit a Sensor Reading

Post a new environmental reading for an active sensor. The sensor's `currentValue` is automatically updated, and the new reading is stored in the sensor's history.

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
     -H "Content-Type: application/json" \
     -H "Accept: application/json" \
     -d '{
           "value": 24.3
         }'
```

**Expected Response (`201 Created`):**
```json
{
  "id": "a3f7b2c1-...",
  "timestamp": 1714000000000,
  "value": 24.3
}
```

---

### 6. Get All Readings for a Sensor

Retrieve the full reading history for a specific sensor.

```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/TEMP-001/readings \
     -H "Accept: application/json"
```

**Expected Response (`200 OK`):**
```json
[
  {
    "id": "...",
    "timestamp": 1714000000000,
    "value": 22.5
  },
  {
    "id": "...",
    "timestamp": 1714000020000,
    "value": 24.3
  }
]
```

---

### 7. Attempt to Read from a MAINTENANCE Sensor (Error Demonstration)

Demonstrates the `403 Forbidden` guard — sensor `HUM-001` starts in `MAINTENANCE` status and cannot accept new readings.

```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/HUM-001/readings \
     -H "Content-Type: application/json" \
     -H "Accept: application/json" \
     -d '{"value": 55.0}'
```

**Expected Response (`403 Forbidden`):**
```json
{
  "status": 403,
  "message": "Sensor HUM-001 is under MAINTENANCE and cannot accept new readings!"
}
```

---

##  Project Structure

```
SmartCampusAPI/
├── pom.xml                          # Maven build configuration
└── src/
    └── main/
        ├── java/com/smartcampus/
        │   ├── MyApplication.java                  # JAX-RS entry point (@ApplicationPath)
        │   ├── dao/
        │   │   └── DataStore.java                  # In-memory singleton data store
        │   ├── model/
        │   │   ├── Room.java
        │   │   ├── Sensor.java
        │   │   ├── SensorReading.java
        │   │   └── ErrorMessage.java
        │   ├── resource/
        │   │   ├── DiscoveryResource.java           # GET /api/v1/
        │   │   ├── RoomResource.java                # /api/v1/rooms
        │   │   ├── SensorResource.java              # /api/v1/sensors
        │   │   └── SensorReadingResource.java       # /api/v1/sensors/{id}/readings
        │   ├── exception/
        │   │   ├── GlobalExceptionMapper.java
        │   │   ├── LinkedResourceNotFoundException.java
        │   │   ├── RoomNotEmptyException.java
        │   │   └── SensorUnavailableException.java
        │   └── filter/
        │       └── LoggingFilter.java
        └── webapp/
            └── WEB-INF/
                └── web.xml                         # Servlet & URL mapping config
```

---

##  Technology Stack

| Component       | Technology                  |
|-----------------|-----------------------------|
| Language        | Java 11                     |
| REST Framework  | JAX-RS via Jersey 2.32      |
| JSON Binding    | Jackson (via Jersey)        |
| Build Tool      | Apache Maven 3.x            |
| Server          | Apache Tomcat 9.x           |
| Data Storage    | In-memory HashMap (no DB)   |
