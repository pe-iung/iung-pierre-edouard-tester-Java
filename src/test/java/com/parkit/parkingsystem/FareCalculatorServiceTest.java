package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.Fare;
import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.FareCalculatorService;
import com.parkit.parkingsystem.tools.TimeTool;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Date;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FareCalculatorServiceTest {

    private static FareCalculatorService fareCalculatorService;
    private Ticket ticket;

    @BeforeAll
    public static void setUp() {
        fareCalculatorService = new FareCalculatorService();
    }

    @BeforeEach
    public void setUpPerTest() {
        ticket = new Ticket();
    }

    private static Stream<Arguments> calculateFareArguments(){
        return Stream.of(
                Arguments.of(ParkingType.CAR, 0.25 , 0),
                Arguments.of(ParkingType.BIKE, 0.25 , 0),
                Arguments.of(ParkingType.CAR, 0.4 , 0 ),
                Arguments.of(ParkingType.BIKE, 0.4 , 0 ),
                Arguments.of(ParkingType.CAR, 0.90 , 0.90 * Fare.CAR_RATE_PER_HOUR),
                Arguments.of(ParkingType.BIKE, 0.90, 0.90 * Fare.BIKE_RATE_PER_HOUR),
                Arguments.of(ParkingType.CAR, 3 , 3 * Fare.CAR_RATE_PER_HOUR),
                Arguments.of(ParkingType.BIKE, 3, 3 * Fare.BIKE_RATE_PER_HOUR)
        );
    }

    /**
     * test the fare calculation for a parametrized list of fares
     * given a ticket for a bike or car
     * when fare is calculated
     * then the fare calculated is equal to the fare expected
     */
    @ParameterizedTest(name = "{index} - Calculate a fare for a {0} type, a duration of {1} hour and an expected price of {2}")
    @MethodSource("calculateFareArguments")
    public void calculateFare(ParkingType parkingType, double duration, double expectedPrice) {
        Date inTime = new Date();
        inTime.setTime(TimeTool.now().minusMinute( (long) (duration * 60)).toLong() );
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, parkingType, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(expectedPrice,ticket.getPrice());
    }

    /**
     * test the fare calculation error handling for an unknown vehicle type
     * given a ticket
     * when vehicle type is unknown
     * then an exception is raised
     */
    @Test
    public void calculateFareUnkownType() {
        Date inTime = new Date();
        inTime.setTime(TimeTool.now().minusMinute(60).toLong());
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, null, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(NullPointerException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    /**
     * test the fare calculation for future incoming time
     * given a ticket for a bike
     * when incoming time is in the future
     * then an exception is raised
     */
    @Test
    public void calculateFareBikeWithFutureInTime() {
        Date inTime = new Date();
        inTime.setTime(TimeTool.now().plusMinute(60).toLong());
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        assertThrows(IllegalArgumentException.class, () -> fareCalculatorService.calculateFare(ticket));
    }

    /**
     * test the fare calculation for less than 30min parking time
     * given a ticket for a bike
     * when parking duration is 45min
     * then fare should be 3/4th of hourly rate
     */
    @Test
    public void calculateFareBikeWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(TimeTool.now().minusMinute(45).toLong());//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((0.75 * Fare.BIKE_RATE_PER_HOUR), ticket.getPrice());
    }

    /**
     * test the fare calculation for less than 30min parking time
     * given a ticket for a car
     * when parking duration is 45min
     * then fare should be 3/4th of hourly rate
     */
    @Test
    public void calculateFareCarWithLessThanOneHourParkingTime() {
        Date inTime = new Date();
        inTime.setTime(TimeTool.now().minusMinute(45).toLong());//45 minutes parking time should give 3/4th parking fare
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(1.125, ticket.getPrice());
    }

    /**
     * test the fare calculation for less than 30min parking time
     * given a ticket for a car
     * when parking duration is more than a day
     * then fare should be 24 * car rate per hour
     */
    @Test
    public void calculateFareCarWithMoreThanADayParkingTime() {
        Date inTime = new Date();
        inTime.setTime(TimeTool.now().minusHour(24).toLong());//24 hours parking time should give 24 * parking fare per hour
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals((24 * Fare.CAR_RATE_PER_HOUR), ticket.getPrice());
    }

    /**
     * test the fare calculation for less than 30min parking time
     * given a ticket for a car
     * when parking duration is less than 30min
     * then fare calculated is free
     */
    @Test
    public void calculateFareCarWithLessThan30minutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime(TimeTool.now().minusMinute(30).toLong());//30 minutes parking time should be free
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(Fare.FREE_30_MIN, ticket.getPrice());
    }

    /**
     * test the fare calculation for less than 30min parking time
     * given a ticket for a bike
     * when parking duration is less than 30min
     * then fare calculated is free
     */
    @Test
    public void calculateFareBikeWithLessThan30minutesParkingTime() {
        Date inTime = new Date();
        inTime.setTime(TimeTool.now().minusMinute(30).toLong());//30 minutes parking time should be free
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        fareCalculatorService.calculateFare(ticket);
        assertEquals(Fare.FREE_30_MIN, ticket.getPrice());
    }

    /**
     * test the fare calculation for recurring user
     * given a recurring user
     * when fare is calculated for a car
     * then a 5% discount is applied
     */

    @Test
    public void calculateFareCarWithDiscount() {
        Date inTime = new Date();
        inTime.setTime(TimeTool.now().minusHour(2).toLong()); //2hours parking converted in millis
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        Boolean isDiscount = true;
        fareCalculatorService.calculateFare(ticket, isDiscount);
        assertEquals(2.85d, ticket.getPrice());
    }

    /**
     * test the fare calculation for recurring user
     * given a recurring user
     * when fare is calculated for a bike
     * then a 5% discount is applied
     */
    @Test
    public void calculateFareBikeWithDiscount() {
        Date inTime = new Date();
        inTime.setTime(TimeTool.now().minusHour(2).toLong()); //2hours parking converted in millis
        Date outTime = new Date();
        ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.BIKE, false);

        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setParkingSpot(parkingSpot);
        Boolean isDiscount = true;
        fareCalculatorService.calculateFare(ticket, isDiscount);
        assertEquals(1.9d, ticket.getPrice());
    }

}
