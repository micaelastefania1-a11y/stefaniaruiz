package com.sistdist.sensortemperatura;

import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public class SensorTemperatura {
    public static void main(String[] args) {
        try {
            InetAddress IPServidor = InetAddress.getByName("127.0.0.1");
            Socket cliente = new Socket(IPServidor, 20000);
            PrintWriter pw = new PrintWriter(cliente.getOutputStream());

            // Handshake inicial
            pw.println("sensorTemperatura;0"); 
            pw.flush();

            // Hilo que manda datos continuamente
            HiloSensadoTemperatura hilo = new HiloSensadoTemperatura(cliente, pw);
            hilo.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
