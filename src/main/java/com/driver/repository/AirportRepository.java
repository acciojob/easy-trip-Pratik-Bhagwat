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
    HashMap<Flight,List<Passenger>> flightPassengerDb = new HashMap<>();
    List<Flight> flightList = new ArrayList<>();


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
        }
        return airportName;
    }

    public double getShortestDurationOfPossibleBetweenTwoCities(City fromCity, City toCity) {
        //Find the duration by finding the shortest flight that connects these 2 cities directly
        //If there is no direct flight between 2 cities return -1.
        double shortestDuration = Integer.MAX_VALUE * 1.00;
        for (Flight f : flightList) {
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

        int cnt = 0;
        for (Flight flight : flightPassengerDb.keySet()) {
            if((flight.getFromCity().equals(airportName)||flight.getToCity().equals(airportName)) && flight.getFlightDate().equals(date)) {
                List<Passenger> passengerList = flightPassengerDb.get(flight);
                cnt += passengerList.size();
            }
        }
        return cnt;
    }

    public int calculateFlightFare(Integer flightId) {
        //Calculation of flight prices is a function of number of people who have booked the flight already.
        //Price for any flight will be : 3000 + noOfPeopleWhoHaveAlreadyBooked*50
        //Suppose if 2 people have booked the flight already : the price of flight for the third person will be 3000 + 2*50 = 3100
        //This will not include the current person who is trying to book, he might also be just checking price
        int noOfPeopleWhoHaveAlreadyBooked = 0;
        for(Flight flight : flightPassengerDb.keySet()) {
           if (flight.getFlightId() == flightId) {
               List<Passenger> passengerList = flightPassengerDb.get(flight);
               noOfPeopleWhoHaveAlreadyBooked = passengerList.size();
               break;
           }
        }
        return 3000 + noOfPeopleWhoHaveAlreadyBooked * 50;
    }

    public String bookATicket(Integer flightId, Integer passengerId) {
        //If the numberOfPassengers who have booked the flight is greater than : maxCapacity, in that case :
        //return a String "FAILURE"
        //Also if the passenger has already booked a flight then also return "FAILURE".
        //else if you are able to book a ticket then return "SUCCESS"
        int flightFare = calculateFlightFare(flightId);


        for (Flight flight : flightList) {
            if(flight.getFlightId() == flightId) {
                Passenger passenger = new Passenger();
                passenger.setPassengerId(passengerId);

                List<Passenger> passengerList = flightPassengerDb.get(flight);

                if(passengerList.contains(passenger)) {
                    return "FAILURE";
                }

                if(passengerList.size() < flight.getMaxCapacity()) {
                    passengerList.add(passenger);
                    flightPassengerDb.put(flight,passengerList);
                    return "SUCCESS";
                }
                else if(passengerList.size() >= flight.getMaxCapacity()) {
                    return "FAILURE";
                }
            }
        }
        return null;
    }

    public String cancelATicket(Integer flightId, Integer passengerId) {
        //If the passenger has not booked a ticket for that flight or the flightId is invalid or in any other failure case
        // then return a "FAILURE" message
        // Otherwise return a "SUCCESS" message
        // and also cancel the ticket that passenger had booked earlier on the given flightId

        Passenger passenger = null;
        for(Integer id : passengerDb.keySet()) {
            if(id == passengerId) {
                passenger = passengerDb.get(id);
                break;
            }
        }

        for(Flight flight : flightPassengerDb.keySet()) {
            if(flight.getFlightId() == flightId) {
                List<Passenger> passengerList = flightPassengerDb.get(flight);
                if(!passengerList.contains(passenger)) {
                    return "FAILURE";
                }
                else if(passengerList.contains(passenger)) {
                    passengerList.remove(passenger);
                }
            }
        }
        return "SUCCESS";
    }

    public int countOfBookingsDoneByPassengerAllCombined(Integer passengerId) {
        //Tell the count of flight bookings done by a passenger: This will tell the total count of flight bookings done by a passenger :
        Passenger passenger = null;
        for(Integer id : passengerDb.keySet()) {
            if(id == passengerId) {
                passenger = passengerDb.get(id);
                break;
            }
        }
        int cnt = 0;
        for (List <Passenger> p : flightPassengerDb.values()) {
            if(p.contains(passenger)) cnt++;
        }
        return cnt;
    }

    public String addFlight(Flight flight) {
        flightList.add(flight);
        return "SUCCESS";
        //Return a "SUCCESS" message string after adding a flight.
    }

    public String getAirportNameFromFlightId(Integer flightId) {
        //We need to get the starting airportName from where the flight will be taking off (Hint think of City variable if that can be of some use)
        //return null incase the flightId is invalid or you are not able to find the airportName
        String airportName = "";
        for (Flight f: flightList) {
            if(f.getFlightId() == flightId) {
                airportName =  f.getFromCity().name();
                break;
            }
        }
        return airportName;
    }

    public int calculateRevenueOfAFlight(Integer flightId) {
        //Calculate the total revenue that a flight could have
        //That is of all the passengers that have booked a flight till now and then calculate the revenue
        //Revenue will also decrease if some passenger cancels the flight
        List<Passenger> passengers = null;
        for (Flight f : flightPassengerDb.keySet()) {
            if(f.getFlightId() == flightId) {
                passengers = flightPassengerDb.get(f);
                break;
            }
        }
        int revenue = 0;
        for(int i = 0; i < passengers.size(); i++) {
            revenue += 3000 + (i * 50);
        }
        return revenue;
    }

    public String addPassenger(Passenger passenger) {
        //Add a passenger to the database
        //And return a "SUCCESS" message if the passenger has been added successfully..
        passengerDb.put(passenger.getPassengerId(),passenger);
        return "SUCCESS";
    }
}
