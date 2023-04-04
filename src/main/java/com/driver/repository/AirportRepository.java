package com.driver.repository;

import com.driver.model.Airport;
import com.driver.model.City;
import com.driver.model.Flight;
import com.driver.model.Passenger;
import io.swagger.models.auth.In;
import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class AirportRepository {

    HashMap<String,Airport> airportDB = new HashMap<>();
    HashMap<Integer,Passenger> passengerDb = new HashMap<>();
    HashMap<Integer,List<Integer>> flightPassengerDb = new HashMap<>();
    HashMap<Integer,Flight> flightDb = new HashMap<>();


    public void addAirport(Airport airport) {
        //Simply add airport details to your database
        //Return a String message "SUCCESS"
        airportDB.put(airport.getAirportName(),airport);
    }

    public String getLargestAirportName() {
        //Largest airport is in terms of terminals. 3 terminal airport is larger than 2 terminal airport
        //Incase of a tie return the Lexicographically smallest airportName
        int max = 0;
        String airportName = "";
        for (Airport a: airportDB.values()) {
            if(a.getNoOfTerminals() > max) {
                max = a.getNoOfTerminals();
                airportName = a.getAirportName();
            }
            else if(a.getNoOfTerminals() == max) {
                if(a.getAirportName().equals(airportName)) {
                    airportName = a.getAirportName();
                }
            }
        }
        return airportName;
    }

    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity) {
        //Find the duration by finding the shortest flight that connects these 2 cities directly
        //If there is no direct flight between 2 cities return -1.
        double shortestDuration = Integer.MAX_VALUE * 1.00;
        for (Flight f : flightDb.values()) {
            if(f.getToCity().equals(toCity) && f.getFromCity().equals(fromCity)) {
                shortestDuration = Math.min(shortestDuration, f.getDuration());
            }
        }
        if(shortestDuration == Integer.MAX_VALUE * 1.00) return -1;
        return shortestDuration;
    }

    public int getNumberOfPeopleOn(Date date, String airportName) {
        //Calculate the total number of people who have flights on that day on a particular airport
        //This includes both the people who have come for a flight and who have landed on an airport after their flight
        // public HashMap<Integer, Flight> flightDb = new HashMap<>();
        Airport airport = airportDB.get(airportName);
        if(Objects.isNull(airport)){
            return 0;
        }
        City city = airport.getCity();
        int cnt = 0;
        for (Flight flight : flightDb.values()) {
            if((flight.getFromCity().equals(city)||flight.getToCity().equals(city)) && flight.getFlightDate().equals(date)) {
                int flightID = flight.getFlightId();
                cnt += flightPassengerDb.get(flightID).size();
            }
        }
        return cnt;
    }

    public int calculateFlightFare(Integer flightId) {
        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight for the third person will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be just checking price
        int noOfPeopleWhoHaveAlreadyBooked = flightPassengerDb.get(flightId).size();
        return 3000 + noOfPeopleWhoHaveAlreadyBooked * 50;
    }

    public String bookATicket(Integer flightId, Integer passengerId) {
        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"
        if(Objects.nonNull(flightPassengerDb.get(flightId)) &&(flightPassengerDb.get(flightId).size()<flightDb.get(flightId).getMaxCapacity())) {
            List<Integer> passengerList = flightPassengerDb.get(flightId);
            if(passengerList.contains(passengerId)) {
                return "FAILURE";
            }
            passengerList.add(passengerId);
            flightPassengerDb.put(flightId,passengerList);
            return "SUCCESS";
        }
        else if(Objects.isNull(flightPassengerDb.get(flightId))) {
            flightPassengerDb.put(flightId,new ArrayList<>());
            List<Integer> passengerList = flightPassengerDb.get(flightId);

            if(passengerList.contains(passengerId)) {
                return "FAILURE";
            }
            passengerList.add(passengerId);
            flightPassengerDb.put(flightId,passengerList);
            return "SUCCESS";
        }
        return "FAILURE";
    }

    public String cancelATicket(Integer flightId, Integer passengerId) {
        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId

        List<Integer> passengerList = flightPassengerDb.get(flightId);
        if(passengerList == null) {
            return "FAILURE";
        }
        if(passengerList.contains(passengerId)) {
            passengerList.remove(passengerId);
            return "SUCCESS";
        }
        return "FAILURE";
    }

    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId) {
        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
        int cnt = 0;
        HashMap<Integer,List<Integer>> passengerFlightDb = new HashMap<>();

        for(Map.Entry<Integer,List<Integer>> entry: flightPassengerDb.entrySet()) {
            List<Integer> passengerList = entry.getValue();
            for(Integer p: passengerList) {
                if(p.equals(passengerId)) cnt++;
            }
        }
        return cnt;
    }

    public String addFlight(Flight flight) {
        flightDb.put(flight.getFlightId(),flight);
        return "SUCCESS";
        //Return a "SUCCESS" message string after adding a flight.
    }

    public String getAirportNameFromFlightId(Integer flightId) {
        //We need to get the starting airportName from where the flight will be taking off (Hint think of City variable if that can be of some use)
        //return null incase the flightId is invalid or you are not able to find the airportName
        if (flightDb.containsKey(flightId)) {
            City city = flightDb.get(flightId).getFromCity();
            for (Airport a : airportDB.values()) {
                if (a.getCity().equals(city)) {
                    return a.getAirportName();
                }
            }
        }
        return null;
    }

    public int calculateRevenueOfAFlight(Integer flightId) {
        //Calculate the total revenue that a flight could have
        //That is of all the passengers that have booked a flight till now and then calculate the revenue
        // will also decrease if some passenger cancels the flight
        int noOfPeopleBookedFlight = flightPassengerDb.get(flightId).size();
        int revenue = (noOfPeopleBookedFlight *(noOfPeopleBookedFlight + 1)) * 50;
        int totalRevenue = 3000 * noOfPeopleBookedFlight + revenue;
        return totalRevenue;
    }

    public String addPassenger(Passenger passenger) {
        //Add a passenger to the database
        //And return a "SUCCESS" message if the passenger has been added successfully..
        passengerDb.put(passenger.getPassengerId(),passenger);
        return "SUCCESS";
    }
}
