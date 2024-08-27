package com.parkit.parkingsystem.service;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.model.Ticket;

public class FareCalculatorService {

    public void calculateFare(Ticket ticket){
        calculateFare(ticket, false);
    }

    public void calculateFare(Ticket ticket, Boolean isDiscount) {
        if ((ticket.getOutTime() == null) || (ticket.getOutTime().before(ticket.getInTime()))) {
            throw new IllegalArgumentException("Out time provided is incorrect:" + ticket.getOutTime().toString());
        }

        double inTime = ticket.getInTime().getTime();
        double outTime = ticket.getOutTime().getTime();
        double duration = ((outTime - inTime) / 1000.d) / 3600.0d;


        // init price to 0.0
        double price = 0.0;


        // the first 30min are supposed to be free
        if (duration <= 0.5) {
            ticket.setPrice(price);
            return;
        }


        // calculate price by parking type
        switch (ticket.getParkingSpot().getParkingType()) {
            case CAR: {
                price = duration * Fare.CAR_RATE_PER_HOUR;
                break;
            }
            case BIKE: {
                price = duration * Fare.BIKE_RATE_PER_HOUR;
                break;
            }
            default:
                throw new IllegalArgumentException("Unkown Parking Type");
        }

        // subtract discount
        if (isDiscount) {
            price *= 0.95d;
        }

        price = Math.round(price * 1000d) / 1000d;


        ticket.setPrice(price);
    }
}