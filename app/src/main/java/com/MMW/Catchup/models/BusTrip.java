package com.MMW.Catchup.models;

import java.io.Serializable;

public class BusTrip implements Serializable{

    private String busNo;
    private String startTime;
    private String endTime;
    private String startLocation;
    private String busType;

    public BusTrip(){

    }

    public BusTrip(String busNo, String startTime, String endTime, String startLocation,String busType) {
        this.busNo = busNo;
        this.startTime = startTime;
        this.endTime = endTime;
        this.startLocation=startLocation;
        this.busType=busType;
    }

    public String getBusNo() {
        return busNo;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public String getBusType() {
        return busType;
    }
}
