package com.sistdist.sensortemperatura;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class SensorTemperatura {

    public static void main(String[] args) {
        try {
            InetAddress ipServidor = InetAddress.getByName("127.0.0.1");
            Socket cliente = new Socket(ipServidor, 20000);
            PrintWriter out = new PrintWriter(cliente.getOutputStream(), true);

            System.out.println("[SensorTemperatura] Conectado al controlador.");

            Random rand = new Random();

            while (true) {
                // Simulación: temperatura entre 15°C y 35°C
                double temp = 15 + rand.nextDouble() * 20;

                // Enviar al controlador
                out.println("sensorTemperatura;" + temp);
                out.flush();

                System.out.println("[SensorTemperatura] Temperatura enviada: " + temp);

                Thread.sleep(5000); // cada 5 segundos
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

