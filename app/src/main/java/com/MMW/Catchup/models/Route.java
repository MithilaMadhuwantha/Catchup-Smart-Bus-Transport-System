package com.MMW.Catchup.models;

public class Route {

    private String roadName;
    private String origin;
    private String destination;
    private String busNo;
    private boolean isLive;
    private int routeNumber;
    private String busType;

    public Route(){

    }

    public Route(String roadName, String origin, String destination, String busNo, boolean isLive,int routeNumber,String busType) {
        this.roadName = roadName;
        this.origin = origin;
        this.destination = destination;
        this.busNo = busNo;
        this.isLive = isLive;
        this.routeNumber=routeNumber;
        this.busType=busType;
    }

    public String getRoadName() {
        return roadName;
    }

    public String getOrigin() {
        return origin;
    }

    public String getDestination() {
        return destination;
    }

    public String getBusNo() {
        return busNo;
    }

    public boolean isLive() {
        return isLive;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public String getBusType() {
        return busType;
    }

    public void setBusType(String busType) {
        this.busType = busType;
    }
}
