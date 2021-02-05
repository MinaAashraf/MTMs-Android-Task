package com.mtms_task.POJO;

import com.google.firebase.firestore.Exclude;

import java.util.ArrayList;
import java.util.Date;

public class Request {
    private Double sourceLatitude, sourceLongitude, destinationLatitude, destinationLongitude;
    private Date time;
    private String clientName, clientPhone;
    private ArrayList<String> drivers;
    Float clientRating;
    @Exclude
    private String documentId;

    public Request() {
    }

    public Request(Double sourceLatitude, Double sourceLongitude, Double destinationLatitude, Double destinationLongitude, Date time, String clientName, String clientPhone, Float clientRating, ArrayList<String> drivers) {
        this.sourceLatitude = sourceLatitude;
        this.sourceLongitude = sourceLongitude;
        this.destinationLatitude = destinationLatitude;
        this.destinationLongitude = destinationLongitude;
        this.time = time;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.clientRating = clientRating;
        this.drivers = drivers;
    }

    public Double getSourceLatitude() {
        return sourceLatitude;
    }

    public void setSourceLatitude(Double sourceLatitude) {
        this.sourceLatitude = sourceLatitude;
    }

    public Double getSourceLongitude() {
        return sourceLongitude;
    }

    public void setSourceLongitude(Double sourceLongitude) {
        this.sourceLongitude = sourceLongitude;
    }

    public Double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(Double destinationLatitude) {
        this.destinationLatitude = destinationLatitude;
    }

    public Double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(Double destinationLongitude) {
        this.destinationLongitude = destinationLongitude;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public String getClientPhone() {
        return clientPhone;
    }

    public void setClientPhone(String clientPhone) {
        this.clientPhone = clientPhone;
    }

    public Float getClientRating() {
        return clientRating;
    }

    public void setClientRating(Float clientRating) {
        this.clientRating = clientRating;
    }

    public ArrayList<String> getDrivers() {
        return drivers;
    }

    public void setDrivers(ArrayList<String> drivers) {
        this.drivers = drivers;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}
