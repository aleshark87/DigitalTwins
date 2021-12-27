package application;

/*
 * RECAP MOMENTANEO
 * Funzionano le notifiche da entrambi i lati.
 * Mandare segnale manutenzione, macchina che lo legge e che quindi si ferma ad una officina.
 * Parte tempo di manutenzione con digital twin dell'officina che ha la macchina in manutenzione
 * 
 * Ci vuole uno restyling completo del formato in cui è il codice attualmente, per poter accogliere un nuovo digital twin.
 * Non serve fare un nuovo progetto, basta usare l'istanza digitaltwin
 */

public final class Launcher {

    private Launcher() { }

    /**
     * @param args unused
     */
    public static void main(final String[] args) {
        Client dittoClient = new Client();
    }
    
}
