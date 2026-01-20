package com.djokic;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class ParkingWorker implements Runnable {
    private final BlockingQueue<ParkingEvent> parkingEventBlockingQueue;
    private final ConcurrentHashMap<Integer, ParkingAutomat> parkingAutomati;
    private final String zonaNaziv;
    private final ParkingZona parkingZona;

    public ParkingWorker(
            BlockingQueue<ParkingEvent> parkingEventBlockingQueue,
            ConcurrentHashMap<Integer, ParkingAutomat> parkingAutomati,
            String zonaNaziv,
            ParkingZona parkingZona
    ) {
        this.parkingEventBlockingQueue = parkingEventBlockingQueue;
        this.parkingAutomati = parkingAutomati;
        this.zonaNaziv = zonaNaziv;
        this.parkingZona = parkingZona;
    }

    @Override
    public void run() {
        if(parkingZona == null || !parkingZona.isOtvorena() || parkingZona.isUnistena()) {
            return;
        }

        try{
            while(true){
                ParkingEvent event = parkingEventBlockingQueue.take();
                if(event == ParkingEvent.POISON_PILL){
                    System.out.println(Thread.currentThread().getName() + ": Poison pill - Gasenje !");
                    break;
                }

                processParkingEvent(event);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println(Thread.currentThread().getName() + " je prekinut tokom obrade dogadjaja !");
        }
    }

    private void processParkingEvent(ParkingEvent event) {
        try{
            ParkingAutomat parkingAutomat = parkingAutomati.get(event.automatId);
            if(parkingAutomat == null){
                System.err.println(Thread.currentThread().getName() + ": Parking automat " + event.automatId + " ne postoji!");
                return;
            }

            double naplaceniIznos = parkingAutomat.naplatiParkiranje(
                    new TarifiranoVozilo(event.tarifnaZona),
                    event.brojSati
            );

            System.out.println(zonaNaziv + " - " + Thread.currentThread().getName() + ": Vozilo (zona = " + event.tarifnaZona + ")" +
                    " parkirano na automat " + parkingAutomat.getId() + ". Trajanje: " + event.brojSati + "h. Naplaceno: "
                    + String.format("%.2f", naplaceniIznos));
        } catch (Exception e){
            System.err.println(zonaNaziv + " - " + Thread.currentThread().getName() + " - Greska pri obradi parking dogadjaja: " + e.getMessage());
        }
    }
}
