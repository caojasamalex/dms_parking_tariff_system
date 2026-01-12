package com.djokic;

import java.util.Arrays;

public class Tarifnik {
    private int[] cenePoZonama;

    public Tarifnik(int[] cenePoZonama) {
        if(cenePoZonama == null || cenePoZonama.length == 0) {
            throw new IllegalArgumentException("Tarifnik mora da bude definisan !");
        }

        int i = 0;

        for(int cena: cenePoZonama) {
            if(cena < 0){
                throw new IllegalArgumentException("Cena zone " + i + " nije validna -> Unesite pozitivan broj !");
            }
            i++;
        }

        this.cenePoZonama = Arrays.copyOf(cenePoZonama, cenePoZonama.length);
    }

    public int getBrojZona(){
        return this.cenePoZonama.length;
    }

    public int getCenaZaZonu(int zona){
        if(zona < 0 || zona >= this.cenePoZonama.length) {
            throw new IllegalArgumentException("Zona " + zona + " ne postoji. Opseg zona: 0 - " + (this.cenePoZonama.length - 1));
        }

        return this.cenePoZonama[zona];
    }

    @Override
    public String toString() {
        return "Tarifnik {" + Arrays.toString(this.cenePoZonama) + '}';
    }
}