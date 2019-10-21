package com.example.parkwhere;

public class CarPark {

    private String car_park_no;
    private String address;
    private double latitude;
    private double longitude;
    private String car_park_type;
    private String parkingSystem;
    private String short_term_parking;
    private String night_parking;
    private String free_parking;
    private int car_park_decks;
    private double gantry_height;
    private boolean isCar_park_basement;

    public CarPark(String number, String address, double lat, double lng, String type, String system, String term, String night,String free, int decks, double height, boolean basement){

        car_park_no = number;
        this.address = address;
        latitude = lat;
        longitude = lng;
        car_park_type = type;
        parkingSystem = system;
        short_term_parking = term;
        night_parking = night;
        free_parking = free;
        car_park_decks = decks;
        gantry_height = height;
        isCar_park_basement = basement;
    }

    public CarPark(String number){
        car_park_no = number;
    }

    public String getCar_park_no(){
        return  car_park_no;
    }


    public String getAddress(){
        return  address;
    }
    public double getLatitude(){
        return latitude;
    }
    public double getLongitude(){
        return longitude;
    }
    public String getCar_park_type(){
        return car_park_type;
    }
    public  String getParkingSystem(){
        return parkingSystem;
    }
    public String getShort_term_parking(){
        return short_term_parking;
    }
    public String getFree_parking(){
        return free_parking;
    }
    public  int getCar_park_decks(){
        return car_park_decks;
    }
    public double getGantry_height(){
        return gantry_height;
    }
    public boolean isCar_park_basement(){
        return  isCar_park_basement;
    }

    public void setCar_park_no(String number){
        this.car_park_no = number;

    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setCar_park_basement(boolean car_park_basement) {
        isCar_park_basement = car_park_basement;
    }

    public void setCar_park_decks(int car_park_decks) {
        this.car_park_decks = car_park_decks;
    }

    public void setCar_park_type(String car_park_type) {
        this.car_park_type = car_park_type;
    }

    public void setFree_parking(String free_parking) {
        free_parking = free_parking;
    }

    public void setGantry_height(double gantry_height) {
        this.gantry_height = gantry_height;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setParkingSystem(String parkingSystem) {
        this.parkingSystem = parkingSystem;
    }

    public void setShort_term_parking(String short_term_parking) {
        this.short_term_parking = short_term_parking;
    }
}
