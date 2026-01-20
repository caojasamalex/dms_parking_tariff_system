package com.djokic;

public class Main {
    public static void main(String[] args) {
        String nazivParkingZone = "Centar";
        int brojAutomata = 4;
        double srednjeVremeDolaska = 1;
        int[] ceneTarifnika1 = {100, 150, 200};
        int[] ceneTarifnika2 = {150, 240, 260, 300};

        ParkingAutomat bazniAutomat = new ParkingAutomat();
        Tarifnik tarifnik1 = new Tarifnik(ceneTarifnika1);
        Tarifnik tarifnik2 = new Tarifnik(ceneTarifnika2);
        bazniAutomat.setTarifnik(tarifnik1);

        ParkingZona parkingZona = new ParkingZona(nazivParkingZone, brojAutomata, srednjeVremeDolaska, bazniAutomat);
        System.out.println("Kreirana parking zona - " + parkingZona);

        try{
            parkingZona.otvoriParkingZonu(tarifnik1);

            System.out.println("Parking zona otvorena - pocinjemo simulaciju - Cekamo 10 sekundi...");
            Thread.sleep(10000);

            System.out.println("Stanje nakon 10 sekundi: ");

            System.out.println(parkingZona);
            System.out.println("Ukupan naplaceni iznos: " + String.format("%.2f", parkingZona.getUkupanNaplaceniIznos()));

            parkingZona.zatvoriParkingZonu();
            parkingZona.otvoriParkingZonu(tarifnik2);
            System.out.println("Zona ponovno otvorena sa novim tarifnikom - " + tarifnik2);
            Thread.sleep(5000);
            System.out.println("Stanje zone nakon 5 sekundi sa novim tarifnikom: ");
            System.out.println(parkingZona);

            System.out.println("Nastavak simulacije - Cekamo jos 5 sekundi...");
            Thread.sleep(5000);

            parkingZona.zatvoriParkingZonu();

            System.out.println("Stanje nakon zatvaranja parking zone:");
            System.out.println(parkingZona);
            System.out.println("Ukupan naplaceni iznos: " + String.format("%.2f", parkingZona.getUkupanNaplaceniIznos()));

            parkingZona.unistiParkingZonu();
            System.out.println("Stanje nakon unistenja parking zone:");
            System.out.println(parkingZona);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            try{
                parkingZona.zatvoriParkingZonu();
            } catch (Exception ignored){

            }

            try{
                parkingZona.unistiParkingZonu();
            } catch(Exception ignored){

            }
        }
    }
}