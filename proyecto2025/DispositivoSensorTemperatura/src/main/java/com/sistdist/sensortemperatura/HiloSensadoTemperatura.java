package com.sistdist.sensortemperatura;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiloSensadoTemperatura extends Thread {
    private boolean on;
    private double temperatura;
    private final Socket cnxServidor;
    private final PrintWriter pw;
    private final Random rand;

    public HiloSensadoTemperatura(Socket s, PrintWriter out) {
        this.cnxServidor = s;
        this.pw = out;
        this.on = false;
        this.rand = new Random();
    }

    private double generarTemperatura() {
        // Ejemplo: valores entre 10 y 35 grados
        return 10 + rand.nextDouble() * 25;
    }

    public void encender() { on = true; }
    public void apagar()   { on = false; }

    public double leerTemperatura() { return temperatura; }

    @Override
    public void run() {
        on = true;
        while (on) {
            temperatura = generarTemperatura();
            System.out.printf("[SENSOR-T] Generada temperatura: %.1f°C%n", temperatura);

            pw.println(temperatura);
            pw.flush();

            try {
                Thread.sleep(5000); // cada 5 seg
            } catch (InterruptedException ex) {
                Logger.getLogger(HiloSensadoTemperatura.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
