package com.sistdist.sensorlluvia;

import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiloSensadoLluvia extends Thread {
    private final Socket conexion;
    private final PrintWriter out;
    private final Random rand = new Random();
    private boolean on = true;

    public HiloSensadoLluvia(Socket s, PrintWriter pw) {
        this.conexion = s;
        this.out = pw;
    }

    @Override
    public void run() {
        while (on) {
            try {
                // 20% de probabilidad de lluvia
                int valor = rand.nextDouble() < 0.2 ? 1 : 0;

                out.println(valor);
                out.flush();
                System.out.println("[SensorLluvia] Enviado = " + valor);

                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(HiloSensadoLluvia.class.getName()).log(Level.SEVERE, null, ex);
                on = false;
            }
        }
    }
}
