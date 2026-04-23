package com.smartcampus.dao;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.SensorReading;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStore {

    // Static HashMaps - shared across entire application
    private static final Map<String, Room> rooms = new HashMap<>();
    private static final Map<String, Sensor> sensors = new HashMap<>();
    private static final Map<String, List<SensorReading>> sensorReadings = new HashMap<>();

    // Static block - pre-loads some sample data when app starts
    static {
        // Sample Rooms
        Room room1 = new Room("LIB-301", "Library Quiet Study", 50);
        Room room2 = new Room("LAB-101", "Computer Lab", 30);
        rooms.put(room1.getId(), room1);
        rooms.put(room2.getId(), room2);

        // Sample Sensors
        Sensor sensor1 = new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LIB-301");
        Sensor sensor2 = new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "LAB-101");
        Sensor sensor3 = new Sensor("HUM-001", "Humidity", "MAINTENANCE", 60.0, "LIB-301");

        sensors.put(sensor1.getId(), sensor1);
        sensors.put(sensor2.getId(), sensor2);
        sensors.put(sensor3.getId(), sensor3);

        // Link sensors to rooms
        room1.getSensorIds().add("TEMP-001");
        room2.getSensorIds().add("CO2-001");
        room1.getSensorIds().add("HUM-001");

        // Sample Readings
        List<SensorReading> readings1 = new ArrayList<>();
        readings1.add(new SensorReading(22.5));
        sensorReadings.put("TEMP-001", readings1);

        List<SensorReading> readings2 = new ArrayList<>();
        readings2.add(new SensorReading(400.0));
        sensorReadings.put("CO2-001", readings2);
    }

    // ---- ROOM METHODS ----
    public static Map<String, Room> getRooms() {
        return rooms;
    }

    public static Room getRoom(String id) {
        return rooms.get(id);
    }

    public static void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public static void deleteRoom(String id) {
        rooms.remove(id);
    }

    // ---- SENSOR METHODS ----
    public static Map<String, Sensor> getSensors() {
        return sensors;
    }

    public static Sensor getSensor(String id) {
        return sensors.get(id);
    }

    public static void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    // ---- SENSOR READING METHODS ----
    public static List<SensorReading> getReadings(String sensorId) {
        return sensorReadings.getOrDefault(sensorId, new ArrayList<>());
    }

    public static void addReading(String sensorId, SensorReading reading) {
        sensorReadings
            .computeIfAbsent(sensorId, k -> new ArrayList<>())
            .add(reading);
    }
}