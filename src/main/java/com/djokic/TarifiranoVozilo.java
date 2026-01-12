package com.djokic;

public class TarifiranoVozilo implements Tarifirano {
    private int tarifnaZona;

    public TarifiranoVozilo(int tarifnaZona) {
        if(tarifnaZona < 0){
            throw new IllegalArgumentException("Tarifna zona ne sme biti negativna !");
        }

        this.tarifnaZona = tarifnaZona;
    }

    @Override
    public int getTarifnaZona() {
        return this.tarifnaZona;
    }

    @Override
    public String toString() {
        return "Vozilo (zona =  " + this.tarifnaZona + ")";
    }
}
