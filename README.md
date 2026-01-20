# Dokumentacija Sistema za Tarifiranje Parkinga

## Pregled
Ovaj projekat implementira simulaciju sistema za tarifiranje parkinga u Javi. Sistem simulira parking zonu sa više parking automata, gde vozila iz nasumičnih tarifnih zona dolaze u nasumičnim vremenskim intervalima, parkiraju se na nasumičan broj sati i naplaćuju se prema tarifniku.

## Struktura Klasa

### Interfejs Tarifirano
- Predstavlja entitet koji ima tarifnu zonu.
- Metode:
  - `getTarifnaZona()`: Vraća tarifnu zonu entiteta.

### Klasa TarifiranoVozilo
- Implementira interfejs Tarifirano.
- Predstavlja vozilo sa tarifnom zonom.
- Konstruktor:
  - `TarifiranoVozilo(int tarifnaZona)`: Kreira vozilo sa navedenom tarifnom zonom.
- Metode:
  - `getTarifnaZona()`: Vraća tarifnu zonu vozila.
  - `toString()`: Vraća string reprezentaciju vozila.

### Klasa Tarifnik
- Predstavlja tarifnik sa cenama za različite tarifne zone.
- Konstruktor:
  - `Tarifnik(int[] cenePoZonama)`: Kreira tarifnik sa navedenim cenama za zone.
- Metode:
  - `getBrojZona()`: Vraća broj zona u tarifniku.
  - `getCenaZaZonu(int zona)`: Vraća cenu za navedenu zonu.
  - `toString()`: Vraća string reprezentaciju tarifnika.

### Klasa ParkingAutomat
- Predstavlja parking automat sa jedinstvenim ID-om i tarifnikom.
- Konstruktor:
  - `ParkingAutomat()`: Kreira parking automat sa jedinstvenim ID-om.
- Metode:
  - `kopiraj()`: Kreira kopiju automata sa resetovanim naplaćenim iznosom.
  - `setTarifnik(Tarifnik tarifnik)`: Postavlja tarifnik za automat i resetuje naplaćeni iznos.
  - `naplatiParkiranje(Tarifirano vozilo, int brojZapocetihSati)`: Naplaćuje parkiranje za navedeno vozilo i broj započetih sati.
  - `getUkupanNaplaceniIznos()`: Vraća ukupan naplaćeni iznos od poslednjeg postavljanja tarifnika.
  - `getId()`: Vraća ID automata.
  - `toString()`: Vraća string reprezentaciju automata u formatu idBr(naplaćenIznos).

### Klasa ParkingEvent
- Predstavlja događaj parkiranja sa ID-om automata, tarifnom zonom i brojem sati.
- Konstruktor:
  - `ParkingEvent(int automatId, int tarifnaZona, int brojSati)`: Kreira događaj parkiranja sa navedenim parametrima.
- Statičke Metode:
  - `newRandomParkingEvent(Tarifnik tarifnik, List<Integer> dostupniAutomati)`: Kreira nasumičan događaj parkiranja sa nasumičnom tarifnom zonom, brojem sati i ID-om automata.

### Klasa ParkingClient
- Implementira Runnable.
- Generiše događaje parkiranja i dodaje ih u red.
- Konstruktor:
  - `ParkingClient(BlockingQueue<ParkingEvent> parkingEventBlockingQueue, Tarifnik tarifnik, List<Integer> dostupniAutomati, int maxEventsToGenerate, ParkingZona parkingZona)`: Kreira klijenta sa navedenim parametrima.
- Metode:
  - `run()`: Generiše nasumične događaje parkiranja i dodaje ih u red.

### Klasa ParkingWorker
- Implementira Runnable.
- Obrađuje događaje parkiranja iz reda.
- Konstruktor:
  - `ParkingWorker(BlockingQueue<ParkingEvent> parkingEventBlockingQueue, ConcurrentHashMap<Integer, ParkingAutomat> parkingAutomati, String zonaNaziv, ParkingZona parkingZona)`: Kreira radnika sa navedenim parametrima.
- Metode:
  - `run()`: Uzima događaje iz reda i obrađuje ih.
  - `processParkingEvent(ParkingEvent event)`: Obrađuje događaj parkiranja naplaćivanjem parkiranja.

### Klasa ParkingZona
- Predstavlja parking zonu sa imenom, automatima i parametrima simulacije.
- Konstruktor:
  - `ParkingZona(String nazivParkingZone, int brojAutomata, double srednjeVremeDolaska, ParkingAutomat bazniParkingAutomat)`: Kreira parking zonu sa navedenim parametrima.
- Metode:
  - `otvoriParkingZonu(Tarifnik tarifnik)`: Otvara zonu sa navedenim tarifnikom.
  - `zatvoriParkingZonu()`: Zatvara zonu.
  - `unistiParkingZonu()`: Uništava zonu.
  - `getUkupanNaplaceniIznos()`: Vraća ukupan naplaćeni iznos od poslednjeg otvaranja zone.
  - `toString()`: Vraća string reprezentaciju zone.
  - `isOtvorena()`: Vraća da li je zona otvorena.
  - `isUnistena()`: Vraća da li je zona uništena.

### Klasa Main
- Sadrži glavnu metodu koja demonstrira upotrebu sistema za tarifiranje parkinga.
- Kreira parking zonu, otvara je, ispisuje stanje nakon nekog vremena, zatvara je, ponovo je otvara sa novim tarifnikom, ispisuje stanje, zatvara je, ispisuje stanje, uništava je i ponovo ispisuje stanje.

## Bezbednost Niti i Sinhronizacija
- Klasa ParkingAutomat koristi sinhronizovane metode za osiguranje bezbednosti niti.
- Klasa ParkingZona koristi objekat za zaključavanje za sinhronizaciju.
- Klase ParkingClient i ParkingWorker koriste BlockingQueue za bezbednu komunikaciju između niti.
- Klasa ParkingZona pravilno upravlja životnim ciklusom niti, osiguravajući da su pravilno pokrenute i zaustavljene.

## Rukovanje Greškama
- Sve klase bacaju odgovarajuće izuzetke sa opisnim porukama za uslove greške.
- Klasa Main pravilno rukuje prekidima i osigurava da je zona zatvorena i uništena u finally bloku.

## Logika Simulacije
- Klasa ParkingZona kreira bazen niti za generatore događaja (instance ParkingClient) koji se izvršavaju u fiksnim intervalima na osnovu prosečnog vremena dolaska.
- Klasa ParkingClient generiše nasumične događaje parkiranja sa nasumičnim tarifnim zonama, brojem sati i ID-ovima automata.
- Klasa ParkingWorker obrađuje događaje naplaćivanjem parkiranja.
- Simulacija može biti pokrenuta, zaustavljena i ponovo pokrenuta sa novim tarifnikom.

## Zaključak
Sistem za tarifiranje parkinga uspešno implementira zahteve navedene u zadatku. Pruža fleksibilan i proširiv okvir za simulaciju parking zona sa više automata i različitim tarifnicima. Sistem je bezbedan za niti, pravilno rukuje greškama i prati dobre principe objektno-orijentisanog dizajna.
