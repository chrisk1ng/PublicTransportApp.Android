package com.chrisking.publictransportapp.classes;

import java.util.ArrayList;

/**
 * Created by ChrisKing on 2017/07/08.
 */

public class City {
    private String name;
    private String country;
    private String taxiName;
    private boolean hasInformal;

    private City(String name, String country, String taxiName, boolean hasInformal){
        this.name = name;
        this.country = country;
        this.taxiName = taxiName;
        this.hasInformal = hasInformal;
    }

    private City(String name, String country, boolean hasInformal){
        this(name, country, "", hasInformal);
    }

    public String getName() {
        return name;
    }

    public void setName(String value) {
        this.name = value;
    }

    public String getCountry() {
        return country;
    }

    public String getTaxiName() {
        return taxiName;
    }

    public boolean getHasInformal() {
        return hasInformal;
    }

    public static ArrayList<City> getLocalCityStore(){
        ArrayList<City> cities = new ArrayList<>();

        cities.add(new City("Accra", "Ghana", "Trotro", true));
        cities.add(new City("Algiers", "Algeria", false));
        cities.add(new City("Beirut", "Lebanon", false));
        cities.add(new City("Bloemfontein", "South Africa", "Taxi", true));
        cities.add(new City("Cairo", "Egypt", false));
        cities.add(new City("Cape Town", "South Africa", "Taxi", true));
        cities.add(new City("Casablanca", "Morocco", false));
        cities.add(new City("Constantine", "Algeria", false));
        cities.add(new City("Dar es Salaam", "Tanzania", "Daladala", true));
        cities.add(new City("Dubai", "UAE", false));
        cities.add(new City("Durban", "South Africa", "Taxi", true));
        cities.add(new City("East London", "South Africa", "Taxi", true));
        cities.add(new City("Gaborone", "Botswana", "Combi", true));
        cities.add(new City("George", "South Africa", false));
        cities.add(new City("Johannesburg", "South Africa", "Taxi", true));
        cities.add(new City("Kampala", "Uganda", "Kampala taxis", true));
        cities.add(new City("Lusaka", "Zambia", "Minibus", true));
        cities.add(new City("Nairobi", "Kenya", "Matatu", true));
        cities.add(new City("Oran", "Algeria", false));
        cities.add(new City("Port Elizabeth", "South Africa", "Taxi", true));
        cities.add(new City("Rabat", "Morocco", false));
        cities.add(new City("Salé", "Morocco", false));
        cities.add(new City("Tshwane", "South Africa", "Taxi", true));
        cities.add(new City("Other", "Unknown", false));

        return cities;
    }
}
