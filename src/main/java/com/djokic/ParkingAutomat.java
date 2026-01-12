package com.djokic;

import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

public class ParkingAutomat {
    private static final AtomicInteger sledeciId = new AtomicInteger(0);
    private final int id;
    private Tarifnik tarifnik;
    private double naplaceniIznos;
    private final Object lock = new Object();

    public ParkingAutomat() {
        this.id = sledeciId.getAndIncrement();
        this.naplaceniIznos = 0;
    }

    private ParkingAutomat(int id) {
        this.id = id;
        this.naplaceniIznos = 0;
        this.tarifnik = null;
    }

    public ParkingAutomat kopiraj(){
        ParkingAutomat parkingAutomat = new ParkingAutomat(this.id);

        return parkingAutomat;
    }

    public void setTarifnik(Tarifnik tarifnik) {
        synchronized (this.lock) {
            this.tarifnik = tarifnik;
            this.naplaceniIznos = 0;
        }
    }

    public double naplatiParkiranje(Tarifirano vozilo, int brojZapocetihSati){
        synchronized (this.lock) {
            if(this.tarifnik == null){
                throw new NullPointerException("Tarifnik nije definisan za automat " + this.id + " !");
            }

            if(brojZapocetihSati < 0){
                throw new IllegalArgumentException("Broj sati mora biti pozitivan za automat " + this.id + " !");
            }

            int tarifnaZonaVozila = vozilo.getTarifnaZona();
            if(tarifnaZonaVozila < 0 || tarifnaZonaVozila >= this.tarifnik.getBrojZona()){
                throw new NoSuchElementException("Tarifna zona vozila (" + vozilo + ") je van dozvoljenog opsega tarifnika (" + tarifnik.getBrojZona() + ")");
            }

            double cenaPoSatu = tarifnik.getCenaZaZonu(tarifnaZonaVozila);
            double iznosZaNaplatu = cenaPoSatu * brojZapocetihSati;

            this.naplaceniIznos += iznosZaNaplatu;

            return iznosZaNaplatu;
        }
    }

    public double getUkupanNaplaceniIznos(){
        synchronized (this.lock) {
            return this.naplaceniIznos;
        }
    }

    public int getId(){
        return this.id;
    }

    @Override
    public String toString() {
        synchronized (this.lock) {
            return "ParkingAutomat{" +
                    "id=" + id +
                    ", tarifnik=" + tarifnik +
                    ", neplaceniIznos=" + naplaceniIznos +
                    '}';
        }
    }
}
