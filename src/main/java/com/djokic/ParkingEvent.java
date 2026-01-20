package com.djokic;

import java.util.List;
import java.util.Random;

public class ParkingEvent {
    public final int automatId;
    public final int tarifnaZona;
    public final int brojSati;

    public static final ParkingEvent POISON_PILL = new ParkingEvent(-1, -1, -1);

    public ParkingEvent(int automatId, int tarifnaZona, int brojSati) {
        this.automatId = automatId;
        this.tarifnaZona = tarifnaZona;
        this.brojSati = brojSati;
    }

    public static ParkingEvent newRandomParkingEvent(Tarifnik tarifnik, List<Integer> dostupniAutomati) {
        Random random = new Random();

        int minZona = 0;
        int maxZona = tarifnik.getBrojZona() - 1;
        int randomTarifnaZona = random.nextInt(maxZona - minZona + 1) + minZona;

        int randomBrojSati = random.nextInt(24) + 1;

        int randomAutomatId = dostupniAutomati.get(random.nextInt(dostupniAutomati.size()));

        return new ParkingEvent(randomAutomatId, randomTarifnaZona, randomBrojSati);
    }
}
