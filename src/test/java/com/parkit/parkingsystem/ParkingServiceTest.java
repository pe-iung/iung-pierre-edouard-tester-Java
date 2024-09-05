package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.slf4j.event.LoggingEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {

//    @Mock
//    private static ParkingService parkingService;
    @Mock
    private static InputReaderUtil inputReaderUtil;
    @Mock
    private static ParkingSpotDAO parkingSpotDAO;
    @Mock
    private static TicketDAO ticketDAO;
    @Mock
    private static Ticket ticket;
    @Mock
    private static ParkingSpot parkingSpot;

    private final String vehicleRegistrationNumber = "ABCDEF";


    @BeforeEach
    public void setUpTest() {
        try {
            parkingSpot = new ParkingSpot(1, ParkingType.CAR,true);
            ticket = new Ticket();
            ticket.setInTime(new Date(System.currentTimeMillis() - (60*60*1000)));
            ticket.setParkingSpot(parkingSpot);
            ticket.setVehicleRegNumber(vehicleRegistrationNumber);

        } catch (Exception e) {
            e.printStackTrace();
            throw  new RuntimeException("Failed to set up test mock objects");
        }
    }

    @AfterEach
    public void restartData() {
        ticket = null;
        parkingSpot = null;
    }

    @Test
    public void processExitingVehicleTest() throws Exception {

        // GIVEN a vehicle in the parking
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegistrationNumber);
        when(ticketDAO.getTicket(vehicleRegistrationNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(vehicleRegistrationNumber)).thenReturn(2);
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(true);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        assertNull(ticket.getOutTime());

        // when vehicle exiting the parking
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        //then outTime is added to ticket
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        assertNotNull(ticket.getOutTime());
    }

    //testProcessIncomingVehicle :
    // test de l’appel de la méthode processIncomingVehicle()
    // où tout se déroule comme attendu.
    @Test
    public void processIncomingVehicleTest() throws Exception {
        // given a parkingspot available

        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1);

        //when a car enter in the parking
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegistrationNumber);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        assertTrue(parkingSpot.isAvailable());
        parkingService.processIncomingVehicle();

        // then a inTime is added to the ticket
        verify(parkingSpotDAO, Mockito.times(1)).updateParking(any(ParkingSpot.class));
        assertFalse(parkingSpot.isAvailable());
    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        // GIVEN a vehicle in the parking
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegistrationNumber);
        when(ticketDAO.getTicket(vehicleRegistrationNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(vehicleRegistrationNumber)).thenReturn(2);

        // when an updateTicket is unable to be processed
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // then the parkingspot is not udpated
        parkingService.processExitingVehicle();
        verify(parkingSpotDAO, Mockito.times(0)).updateParking(any(ParkingSpot.class));
    }

    //testGetNextParkingNumberIfAvailable :
    // test de l’appel de la méthode getNextParkingNumberIfAvailable()
    // avec pour résultat l’obtention d’un spot dont l’ID est 1 et qui est disponible.

    //testGetNextParkingNumberIfAvailableParkingNumberNotFound :
    // test de l’appel de la méthode getNextParkingNumberIfAvailable()
    // avec pour résultat aucun spot disponible (la méthode renvoie null).

    //testGetNextParkingNumberIfAvailableParkingNumberWrongArgument :
    // test de l’appel de la méthode getNextParkingNumberIfAvailable()
    // avec pour résultat aucun spot (la méthode renvoie null)
    // car l’argument saisi par l’utilisateur concernant le type de véhicule
    // est erroné (par exemple, l’utilisateur a saisi 3).

}