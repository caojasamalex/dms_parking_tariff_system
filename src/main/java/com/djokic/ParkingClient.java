package com.djokic;

import java.util.List;
import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class ParkingClient implements Runnable {
    private final BlockingQueue<ParkingEvent> parkingEventBlockingQueue;
    private final Tarifnik tarifnik;
    private final List<Integer> dostupniAutomati;
    private final int maxEventsToGenerate;
    private final ParkingZona parkingZona;

    private volatile int generatedEvents = 0;

    public ParkingClient(
            BlockingQueue<ParkingEvent> parkingEventBlockingQueue,
            Tarifnik tarifnik,
            List<Integer> dostupniAutomati,
            int maxEventsToGenerate,
            ParkingZona parkingZona
            ){
        this.parkingEventBlockingQueue = parkingEventBlockingQueue;
        this.tarifnik = tarifnik;
        this.dostupniAutomati = dostupniAutomati;
        this.maxEventsToGenerate = maxEventsToGenerate;
        this.parkingZona = parkingZona;
    }

    @Override
    public void run() {
        if(parkingZona == null || !parkingZona.isOtvorena() || parkingZona.isUnistena()) {
            return;
        }

        Random random = new Random();
        try{
            if(generatedEvents < maxEventsToGenerate){
                ParkingEvent event = ParkingEvent.newRandomParkingEvent(tarifnik, dostupniAutomati);
                parkingEventBlockingQueue.put(event);
                generatedEvents++;
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(Thread.currentThread().getName() + " je prekinut tokom generisanja dogadjaja !");
        }
    }
}
