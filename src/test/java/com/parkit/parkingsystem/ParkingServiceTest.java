package com.parkit.parkingsystem;

import com.parkit.parkingsystem.constants.ParkingType;
import com.parkit.parkingsystem.dao.ParkingSpotDAO;
import com.parkit.parkingsystem.dao.TicketDAO;
import com.parkit.parkingsystem.model.ParkingSpot;
import com.parkit.parkingsystem.model.Ticket;
import com.parkit.parkingsystem.service.ParkingService;
import com.parkit.parkingsystem.tools.TimeTool;
import com.parkit.parkingsystem.util.InputReaderUtil;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class ParkingServiceTest {
    
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

    private ParkingService parkingService;

    private final String vehicleRegistrationNumber = "ABCDEF";

    @BeforeEach
    public void init(){
        reset();
        this.parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
    }

    @Test
    public void processExitingVehicleTest() throws Exception {
        initCarTicket();
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
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));
        assertNotNull(ticket.getOutTime());
    }


    @Test
    public void processIncomingVehicleTest() throws Exception {

        // given a parkingspot available
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1);


        //when a car enter in the parking
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegistrationNumber);
        when(parkingSpotDAO.updateParking(any(ParkingSpot.class))).thenReturn(true);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processIncomingVehicle();

        // then a inTime is added to the ticket
        verify(parkingSpotDAO, times(1)).updateParking(any(ParkingSpot.class));


        ArgumentCaptor<Ticket> ticketArgumentCaptor = ArgumentCaptor.forClass(Ticket.class);
        verify(ticketDAO).saveTicket(ticketArgumentCaptor.capture());

        Ticket ticketSaved = ticketArgumentCaptor.getValue();
        assertNotNull(ticketSaved);
        assertEquals(vehicleRegistrationNumber, ticketSaved.getVehicleRegNumber());
        assertEquals(0, ticketSaved.getPrice());
        assertNotNull(ticketSaved.getInTime());
        assertNull(ticketSaved.getOutTime());

    }

    @Test
    public void processExitingVehicleTestUnableUpdate() throws Exception {
        initCarTicket();
        // GIVEN a vehicle in the parking
        when(inputReaderUtil.readVehicleRegistrationNumber()).thenReturn(vehicleRegistrationNumber);
        when(ticketDAO.getTicket(vehicleRegistrationNumber)).thenReturn(ticket);
        when(ticketDAO.getNbTicket(vehicleRegistrationNumber)).thenReturn(2);

        // when an updateTicket is unable to be processed
        when(ticketDAO.updateTicket(any(Ticket.class))).thenReturn(false);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);
        parkingService.processExitingVehicle();

        // then the parkingspot is not udpated
        verify(parkingSpotDAO, times(0)).updateParking(any(ParkingSpot.class));
    }

    @Test
    public void getNextParkingNumberIfAvailableTest() throws Exception{
        // Given a parking with available spots
        initCarTicket();
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when(parkingSpotDAO.getNextAvailableSlot(ParkingType.CAR)).thenReturn(1);
        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // When getting the next parking number
        ParkingSpot parkingspot = parkingService.getNextParkingNumberIfAvailable();

        // Then the next available slot is provided
        verify(parkingSpotDAO, times(1)).getNextAvailableSlot(ParkingType.CAR);
        assertEquals(1, parkingspot.getId());
        assertTrue(parkingspot.isAvailable());

    }

    @Test
    public void testGetNextParkingNumberIfAvailableParkingNumberNotFound() throws Exception{

        //given a parking
        initCarTicket();
        when(inputReaderUtil.readSelection()).thenReturn(1);
        when( parkingSpotDAO.getNextAvailableSlot(any())).thenReturn(0);

        ParkingService parkingService = new ParkingService(inputReaderUtil, parkingSpotDAO, ticketDAO);

        // when the parking is full
        ParkingSpot parkingspot = parkingService.getNextParkingNumberIfAvailable();

        //then ticket is not updated
        assertNull(parkingspot);
    }


    @Test
    public void GetNextParkingNumberIfAvailableParkingNumberWrongArgumentTest() throws Exception {
        //given a parking app
        //when  a vehicle enter the parking with an illegal input
        when(inputReaderUtil.readSelection()).thenReturn(3);
        ParkingSpot response = parkingService.getNextParkingNumberIfAvailable();

        //THEN
        assertNull(response);
    }




    private void initTicket(ParkingType parkingType){
        parkingSpot = new ParkingSpot(1, parkingType,true);
        ticket = new Ticket();
        ticket.setInTime(TimeTool.now().minusMinute(60).toDate());
        ticket.setParkingSpot(parkingSpot);
        ticket.setVehicleRegNumber(vehicleRegistrationNumber);
    }

    private void initCarTicket() {
        initTicket(ParkingType.CAR);
    }

}