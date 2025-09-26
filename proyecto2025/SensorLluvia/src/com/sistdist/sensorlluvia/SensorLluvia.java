package com.sistdist.sensorlluvia;

import java.io.PrintWriter;
import java.net.*;
import java.util.Random;

public class SensorLluvia {
    public static void main(String[] args) {
        try {
            InetAddress ipServidor = InetAddress.getByName("127.0.0.1");
            Socket cliente = new Socket(ipServidor, 20000);
            PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);

            out.println("sensorLluvia;1");
            out.flush();
            System.out.println("[SensorLluvia] Conectado al controlador.");

            Random rand = new Random();
            boolean llueve = rand.nextBoolean(); // arranque aleatorio
            final double pCambiarSiLlueve = 0.2;  // prob. de que pare
            final double pCambiarSiNoLlueve = 0.15; // prob. de que empiece

            while (true) {
                // proceso de Markov simple: a veces cambia de estado
                if (llueve && rand.nextDouble() < pCambiarSiLlueve) llueve = false;
                else if (!llueve && rand.nextDouble() < pCambiarSiNoLlueve) llueve = true;

                int valor = llueve ? 1 : 0;
                out.println(valor);
                out.flush();
                System.out.println("[SensorLluvia] Valor enviado = " + valor);
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

//Si querés otro porcentaje de lluvia en la variante A, cambiá pLluvia (por ejemplo, 0.3 = 30%).