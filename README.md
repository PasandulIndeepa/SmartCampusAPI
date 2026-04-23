# SmartCampusAPI

A RESTful API for managing Rooms and Sensors across a Smart Campus, built using **JAX-RS (Jersey 2.32)** on **Apache Tomcat 9**.

---

## Table of Contents

- [API Overview](#api-overview)
- [Technology Stack](#technology-stack)
  [How to Build and Run](#how-to-build-and-run)
- [Sample curl Commands](#sample-curl-commands)
- [Coursework Questions and Answers]()

---

## API Overview

The Smart Campus API provides a backend service for campus facilities managers to monitor and manage:

- **Rooms** - Physical spaces on campus (labs, libraries, halls)
- **Sensors** - Hardware devices deployed inside rooms (temperature, CO2, occupancy)
- **Sensor Readings** - Measurement logs recorded by each sensor

The API follows REST principles with a versioned base path at `/api/v1`. All responses are in JSON format. The data is stored entirely in-memory using `HashMap` and `ArrayList` and no database is required.

### Key Design Decisions

- **Resource Hierarchy:** Sensor readings are modelled as sub-resources of sensors (`/sensors/{id}/readings`), reflecting the physical relationship between sensors and their data.
- **HATEOAS:** The discovery endpoint at `GET /api/v1/` returns links to all primary resource collections, allowing clients to navigate the API without hardcoding URLs.
- **Error Handling:** All errors return structured JSON bodies with an error message, HTTP status code, and a documentation link. Raw stack traces are never exposed.
- **Logging:** Every request and response is logged via a JAX-RS filter without any logging code inside resource methods.



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

### Step 1. Clone the Repository

```bash
git clone https://github.com/PasandulIndeepa/SmartCampusAPI.git
cd SmartCampusAPI
```

### Step 2. Open in NetBeans

1. Open **NetBeans IDE**
2. Click **File then Open Project**
3. Navigate to the cloned `SmartCampusAPI` folder
4. Click **Open Project**

### Step 3. Verify Tomcat is Configured

1. In NetBeans go to **Tools then Servers**
2. Confirm **Apache Tomcat 9** is listed
3. If not, click **Add Server then Apache Tomcat** and point it to your Tomcat installation folder

### Step 4. Build the Project

1. Right click the project in the **Projects** panel
2. Click **Clean and Build**
3. Wait for Maven to download dependencies
4. Confirm you see **BUILD SUCCESS** in the output panel

### Step 5. Run the Project

1. Right click the project
2. Click **Run**
3. NetBeans will deploy the WAR to Tomcat automatically
4. Tomcat starts on port **8080**

### Step 6. Verify it's Running

Open your browser and go to:

```
http://localhost:8080/SmartCampusAPI/api/v1/
```

You should see a JSON response with API metadata.

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

