package com.djokic;

import java.util.concurrent.*;
import java.util.stream.Collectors;

public class ParkingZona {
    private final String nazivParkingZone;
    private ConcurrentHashMap<Integer, ParkingAutomat> parkingAutomati;

    private final double srednjeVremeDolaska;
    private Tarifnik tarifnik;
    private volatile boolean otvorena;
    private volatile boolean unistena;

    private ScheduledExecutorService eventGeneratorScheduler;
    private ExecutorService workerThreadPool;

    private BlockingQueue<ParkingEvent> parkingEventBlockingQueue;

    private final Object lock = new Object();

    private final int BROJ_EVENT_KLIJENATA = 2;
    private final int BROJ_RADNIKA = Runtime.getRuntime().availableProcessors();
    private final int MAX_EVENTS_PER_CLIENT = 100;
    private final int KAPACITET_REDA_DOGADJAJA = 150;

    public ParkingZona(String nazivParkingZone, int brojAutomata, double srednjeVremeDolaska, ParkingAutomat bazniParkingAutomat) {
        if(nazivParkingZone == null || nazivParkingZone.trim().isEmpty()) {
            throw new IllegalArgumentException("Naziv zone ne moze biti prazan !");
        }

        if(brojAutomata <= 0){
            throw new IllegalArgumentException("Broj automata mora biti pozitivan !");
        }

        if(srednjeVremeDolaska <= 0){
            throw new IllegalArgumentException("Srednje vreme dolaska mora biti pozitivno !");
        }

        if(bazniParkingAutomat == null){
            throw new IllegalArgumentException("Bazni parking automat nije validan !");
        }

        this.nazivParkingZone = nazivParkingZone;
        this.srednjeVremeDolaska = srednjeVremeDolaska;

        this.parkingAutomati = new ConcurrentHashMap<>();
        for(int i = 0; i < brojAutomata; i++){
            ParkingAutomat parkingAutomat = bazniParkingAutomat.kopiraj();
            parkingAutomati.put(parkingAutomat.getId(), parkingAutomat);
        }

        this.otvorena = false;
        this.unistena = false;
    }

    public void otvoriParkingZonu(Tarifnik tarifnik) {
        if(tarifnik == null){
            throw new IllegalArgumentException("Tarifnik nije validan !");
        }

        synchronized(lock){
            if(this.unistena){
                throw new IllegalStateException("Parking zona " + this.nazivParkingZone + " je unistena i nije moguce ponovno otvaranje !");
            }

            if(this.otvorena){
                throw new IllegalStateException("Parking zona " + this.nazivParkingZone + " je vec otvorena !");
            }

            this.tarifnik = tarifnik;
            for (ParkingAutomat parkingAutomat : parkingAutomati.values()) {
                parkingAutomat.setTarifnik(tarifnik);
            }

            this.otvorena = true;
            System.out.println("Parking zona " + this.nazivParkingZone + " je otvorena !");
            System.out.println("Tarifnik - " + this.tarifnik);

            pokreniSimulacijuDolazaka();
        }
    }

    public void zatvoriParkingZonu(){
        synchronized(lock){
            if(this.unistena){
                throw new IllegalStateException("Parking zona " + this.nazivParkingZone + " je unistena i nije moguce zatvoriti je !");
            }

            if(!this.otvorena){
                throw new IllegalStateException("Parking zona " + this.nazivParkingZone + " je vec zatvorena !");
            }

            zaustaviSimulacijuDolazaka();
            this.otvorena = false;
            System.out.println("Parking zona " + this.nazivParkingZone + " je zatvorena !");
        }
    }

    public void unistiParkingZonu(){
        synchronized(lock){
            if(this.unistena){
                throw new IllegalStateException("Parking zona " + this.nazivParkingZone + " je vec unistena !");
            }

            if(this.otvorena){
                zatvoriParkingZonu();
            }

            this.parkingAutomati.clear();
            this.tarifnik = null;
            this.unistena = true;
            System.out.println("Parking zona " + this.nazivParkingZone + " je unistena !");
        }
    }

    private void pokreniSimulacijuDolazaka() {
        this.parkingEventBlockingQueue = new LinkedBlockingQueue<>(this.KAPACITET_REDA_DOGADJAJA);

        this.eventGeneratorScheduler = Executors.newScheduledThreadPool(this.BROJ_EVENT_KLIJENATA);

        for(int i = 0; i < BROJ_EVENT_KLIJENATA; i++){
            this.eventGeneratorScheduler.scheduleWithFixedDelay(
                    new ParkingClient(
                            parkingEventBlockingQueue,
                            tarifnik,
                            parkingAutomati.keySet().stream().collect(Collectors.toList()),
                            MAX_EVENTS_PER_CLIENT,
                            this
                    ),
                    0,
                    (long) (srednjeVremeDolaska * 1000),
                    TimeUnit.MILLISECONDS
            );
        }

        workerThreadPool = Executors.newFixedThreadPool(BROJ_RADNIKA);

        for(int i = 0; i < BROJ_RADNIKA; i++){
            workerThreadPool.submit(
                    new ParkingWorker(
                        parkingEventBlockingQueue,
                        parkingAutomati,
                        this.nazivParkingZone,
                            this
                    )
            );
        }
    }

    private void zaustaviSimulacijuDolazaka() {
        if(eventGeneratorScheduler != null && !eventGeneratorScheduler.isShutdown()) {
            eventGeneratorScheduler.shutdownNow();
            try {
                if (!eventGeneratorScheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                    System.err.println("Parking zona " + this.nazivParkingZone + " - Greska: Generatori dogadjaja se nisu zaustavili !");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if(parkingEventBlockingQueue != null) {
            for (int i = 0; i < BROJ_RADNIKA; i++) {
                try {
                    parkingEventBlockingQueue.put(ParkingEvent.POISON_PILL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        if(workerThreadPool != null && !workerThreadPool.isShutdown()){
            workerThreadPool.shutdown();
            try {
                if (!workerThreadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                    System.err.println("Parking zona " + this.nazivParkingZone + " - Greksa: Radnici za obradu dogadjaja se nizu zaustavili !");
                    workerThreadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public double getUkupanNaplaceniIznos(){
        synchronized(lock){
            if(this.unistena){
                return 0;
            }

            return parkingAutomati.values().stream()
                    .mapToDouble(ParkingAutomat::getUkupanNaplaceniIznos)
                    .sum();
        }
    }

    @Override
    public String toString() {
        synchronized (this.lock) {
            String automatiStr = this.parkingAutomati.values().stream()
                    .map(ParkingAutomat::toString)
                    .collect(Collectors.joining(","));
            return this.nazivParkingZone + "(" + String.format("%.2f", getUkupanNaplaceniIznos()) + "): " + automatiStr + " (Status: " + (this.otvorena ? "Otvorena" : "Zatvorena") + (this.unistena ? ", Uništena" : "") + ")";
        }
    }

    public boolean isOtvorena() {
        return otvorena;
    }

    public boolean isUnistena() {
        return unistena;
    }
}
