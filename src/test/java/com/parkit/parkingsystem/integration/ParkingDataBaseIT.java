package com.parkit.parkingsystem.integration;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.integration.config.DataBaseTestConfig;
import com.parkit.parkingsystem.integration.service.DataBasePrepareService;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.tools.TimeTool;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ParkingDataBaseIT {

    private static final DataBaseTestConfig dataBaseTestConfig = new DataBaseTestConfig();
    private static ParkingSpotDAO parkingSpotDAO;
    private static TicketDAO ticketDAO;
    private static DataBasePrepareService dataBasePrepareService;

    @Mock
    private static InputReaderUtil inputReaderUtil;

    @BeforeAll
    public static void setUp() throws Exception{
        parkingSpotDAO = new ParkingSpotDAO();
        parkingSpotDAO.dataBaseConfig = dataBaseTestConfig;
        ticketDAO = new TicketDAO();
        ticketDAO.dataBaseConfig = dataBaseTestConfig;
        dataBasePrepareService = new DataBasePrepareService();
    }

    @BeforeEach
    public void setUpPerTest() throws Exception {
        dataBasePrepareService.clearDataBaseEntries();

    }
    /**
     * test parking a car
     * given a parking with available spots
     * when we process an incoming vehicle
     * then a ticket is actually saved in DB
     */
    @Test
    public void testParkingACar() throws Exception {
        //given a parking with available spots
        final String vehicleRegistrationNumber ="ABCDEF";
        final ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // when we process an incoming vehicle
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegistrationNumber);

        parkingService.processIncomingVehicle();

        // then a ticket is actually saved in DB

        final Ticket ticket = ticketDAO.getTicket(vehicleRegistrationNumber);
        assertNotNull(ticket);
        assertEquals(vehicleRegistrationNumber, ticket.getVehicleRegNumber());
        assertNotNull(ticket.getInTime());
        assertNotNull(ticket.getParkingSpot());
        assertEquals(0, ticket.getPrice());
        assertNull(ticket.getOutTime());

    }

    /**
     * test exiting a car from parking
     * given a CAR already parked
     * when the car exit the parking 60min later
     * then a ticket is actually saved in DB
     */
    @Test
    public void testParkingLotExit() throws Exception {
        // given a CAR already parked

        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        final String vehicleRegNumber = "ABCDEF";

        final Ticket ticket = createIncomingTicket(parkingSpot, vehicleRegNumber, 60);
        ticketDAO.saveTicket(ticket);


        // when the car exit the parking 60min later
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();


        // then the fare are generated
        final Ticket savedTicket = ticketDAO.getTicket(vehicleRegNumber);

        assertNotNull(savedTicket.getOutTime());
        assertNotEquals(0, savedTicket.getPrice());
    }

    /**
     * test exiting a car from parking
     * given a CAR for a recurring user is already parked
     * when the car exit the parking 60min later
     * then the fare with 5% discount is generated
     */
    @Test
    public void testParkingLotExitRecurringUser() throws Exception {

        final ParkingSpot parkingSpot = new ParkingSpot(1, ParkingType.CAR, false);
        final String vehicleRegNumber = "ABCDEF";

        final Ticket oldTicket = createExitingTicketOneHourAgo(parkingSpot, vehicleRegNumber, 120, 3);
        ticketDAO.saveTicket(oldTicket);
        // register a ticket with time and price
        final Ticket newTicket = createIncomingTicket(parkingSpot, vehicleRegNumber, 60);
        ticketDAO.saveTicket(newTicket);

        // when the car exit the parking 60min later
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn("ABCDEF");

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        // then the fare with discount is generated
        final Ticket savedTicket = ticketDAO.getTicket(vehicleRegNumber);

        assertNotNull(savedTicket.getOutTime());
        assertNotEquals(0, savedTicket.getPrice());
        assertEquals(1.425, savedTicket.getPrice());
    }

    /**
     * create an Incoming Ticket
     * @param parkingSpot to know where the vehicle is parked
     * @param vehicleRegNumber to identify the vehicle
     * @param minusMinute to offset time and know how long the car was parked
     * @return Ticket
     */
    public Ticket createIncomingTicket(ParkingSpot parkingSpot, String vehicleRegNumber, long minusMinute){

        final long updatedInTime = TimeTool.now().minusMinute(minusMinute).toLong();
        final Date inTime = new Date();
        inTime.setTime(updatedInTime);

        final Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setInTime(inTime);

        return ticket;
    }

    /**
     * create an Exiting Ticket
     * the car keft the parking one hour ago
     * @param parkingSpot to know where the vehicle is parked
     * @param vehicleRegNumber identify the vehicle
     * @param minusMinute determine how long the car was parked
     * @param farePrice the fare the driver had to paid when leaving the parking
     * @return Ticket
     */
    private Ticket createExitingTicketOneHourAgo(ParkingSpot parkingSpot, String vehicleRegNumber, long minusMinute, double farePrice){
        final long lastOutTimeOneHourAgo = TimeTool.now().minusHour(1).toLong();
        final long updatedInTime = (lastOutTimeOneHourAgo - (minusMinute*60*1000));
        final Date inTime = new Date();
        final Date outTime = new Date();
        inTime.setTime(updatedInTime);
        outTime.setTime(lastOutTimeOneHourAgo);

        final Ticket ticket = new Ticket();
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(vehicleRegNumber);
        ticket.setInTime(inTime);
        ticket.setOutTime(outTime);
        ticket.setPrice(farePrice);

        return ticket;
    }

}
