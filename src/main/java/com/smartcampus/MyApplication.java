package com.smartcampus;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

@ApplicationPath("/api/v1")
public class MyApplication extends Application {
    // Jersey will automatically scan and register
    // all resource classes via web.xml package scanning
    // No need to manually register anything here
}