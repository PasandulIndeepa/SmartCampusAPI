# SmartCampusAPI

A RESTful API for managing Rooms and Sensors across a Smart Campus, built using **JAX-RS (Jersey 2.32)** on **Apache Tomcat 9**.

---

## Table of Contents

- [API Overview](#api-overview)
- [How to Build and Run](#how-to-build-and-run)
- [Sample curl Commands](#sample-curl-commands)
- [Coursework Questions and Answers](#coursework-questions-and-answers)

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

## Coursework Questions and Answers

Part 1 - Setup & Discovery

Question 1.1 - JAX-RS Resource Lifecycle.

JAX-RS will automatically generate a new instance of a resource class with each request. This implies that instance variables are re-created each request and can not contain shared data. This is the reason we have static HashMaps in DataStore - static fields are part of the class, not of the instance, hence the data is retained between all requests. In its absence, all data that is stored would be lost every time it is called.

Question 1.2 - Why HATEOAS is important.

HATEOAS implies that the API response contains the links to the related resources so that the clients will be able to navigate without coding out the URLs. As an example our discovery endpoint directs links to/api/v1/rooms and/api/v1/sensors. In case of a change of URLs, clients will not stop since they use the links in the response instead of using the old documentation.

Part 2 - Room Management

Question 2.1 - IDs Only vs Full Objects.

Returning just IDs maintains small responses, but requires the client to make additional requests to obtain information - 100 rooms would require 101 requests. Sending whole objects is more expensive in bandwidth, but provides all the information in a single request, much more feasible. We do not send back partial objects because we have simple and lightweight data.

Question 2.2 - DELETE idempotency?

Yes. The initial DELETE deletes the room and sends 200 OK. Any subsequent request with the same request will result in 404 Not Found since the room has been lost. The server state is the same between consecutive calls - the room is not present - that meets idempotency. The code of response varies but the result remains the same.

Part 3 - Sensor Operations

Question 3.1 @Consumes mismatch

When a client makes a text/plain or application/xml request to an endpoint annotated with @Consumes(APPLICATION_JSON), JAX-RS will automatically reject that request with 415 Unsupported Media Type, without it even reaching the resource method. The resource approach is never implemented.

Question 3.2 - @QueryParam vs. path-based filtering.

type=CO2 is right since it is an optional modifier on a collection, and not a resource identifier. Incidentally, putting it in the path such as /sensors/type/CO2 suggests that it is a different resource. Multi filters scale well, too - querying with query parameters such as type=CO2 and status=ACTIVE is understandable, but querying with /sensors/type/CO2/status/ACTIVE is not.


Part 4 - Sub-Resources

Question 41 1- Sub-Resource Locator advantages.

Rather than describe all the nested paths in a single huge class, the locator pattern uses specialized classes to describe the nested paths. SensorResource forwards /sensors/{id}/readings to SensorReadingResource. This keeps the classes small and narrow, simplifies the maintenance of the code and reflects the hierarchy of the data in the real world directly within the code structure.

Part 5 - Handling of errors and logging.

Question 5.1 — 422 vs 404

404 is the non-existence of the URL. However, with a sensor that has a bogus roomId, the URL works, and the JSON is fine but the issue is a bad value within the payload. 422 Unprocessable Entity is an appropriate error and it is an error that the request was received and the content is not semantically valid, which was the case.

Question 5.2 - Stack trace security risks.

Stack traces display internal package names, version of a library, file paths and logic flow. This is used by attackers to locate known vulnerabilities in certain versions of libraries. Our GlobalExceptionMapper simply catches all exceptions and sends a generic 500 message, providing no useful information to the attackers.

Question 5.3 - Filters vs manual logging.

The on-method duplication of Logger.info() would be 20+ endpoints with duplicated logging code. When the format is modified, all the methods must be updated. An automatic ContainerRequestFilter and ContainerResponseFilter intercepts all requests and responses with zero logging code in resource methods, so it remains clean and only business logic


