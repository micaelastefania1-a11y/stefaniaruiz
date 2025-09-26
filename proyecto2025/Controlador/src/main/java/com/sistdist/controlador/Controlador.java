package com.sistdist.controlador;

import java.io.*;
import java.net.*;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Controlador {
    // Estado compartido
    static final Map<Integer, Double> humedades = new ConcurrentHashMap<>(); // por parcela

    static volatile double temp = 22.0, rad = 300.0;
    static volatile boolean lluvia = false;

    // Flags de disponibilidad de sensores globales
    static volatile boolean tempReady = false;
    static volatile boolean radReady = false;
    static volatile boolean lluviaReady = false;

    static final int NUM_PARCELAS = 5;
    static final Bomba bomba = new Bomba();

    // Evitar riego concurrente con fertirriego
    static final Object lockBomba = new Object();
    static volatile boolean fertirrigando = false;

    // Conexiones activas a electroválvulas
    static final Map<Integer, PrintWriter> conexionesEV = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        // Crear un hilo por cada parcela (para humedad y EVs por parcela)
        for (int i = 1; i <= NUM_PARCELAS; i++) {
            new HiloParcela(i, humedades, conexionesEV).start();
        }

        // Hilo de control (INR y decisiones)
        HiloControlador hc = new HiloControlador(humedades, conexionesEV);
        hc.start();

        // Hilo de fertirrigación
        new Fertirrigacion().start();

        // Servidor de sensores + electrovalvulas
        try (ServerSocket server = new ServerSocket(20000)) {
            System.out.println("\nCONTROLADOR escuchando en el puerto 20000\n");
            while (true) {
                Socket s = server.accept();
                BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String header = bf.readLine(); // ej: "sensorHumedad;3"
                if (header == null) {
                    s.close();
                    continue;
                }

                String[] parts = header.split(";");
                String tipo = parts[0];
                int id = parts.length > 1 ? parseIntSafe(parts[1], -1) : -1;

                switch (tipo) {
                    case "electrovalvula": {
                        int evId = id;
                        PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                        conexionesEV.put(evId, pw);
                        System.out.println("[CTRL] EV " + evId + " conectada.");
                        new HiloReceptorEV(s, evId).start();
                        break;
                    }
                    case "sensorHumedad": {
                        new HiloReceptorHumedad(s, id).start();
                        break;
                    }
                    case "sensorTemperatura": {
                        new HiloReceptorTemperatura(s, id).start();
                        break;
                    }
                    case "sensorRadiacion": {
                        new HiloReceptorRadiacion(s, id).start();
                        break;
                    }
                    case "sensorLluvia": {
                        new HiloReceptorLluvia(s, id).start();
                        break;
                    }
                    case "fertirriego": {
                        PrintWriter fert = new PrintWriter(s.getOutputStream(), true);
                        conexionesEV.put(7, fert);
                        System.out.println("[CTRL] Fertirrigación conectada.");
                        break;
                    }
                    default: {
                        System.out.println("Tipo no reconocido: " + tipo);
                        s.close();
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private static int parseIntSafe(String s, int def) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return def;
        }
    }
}
