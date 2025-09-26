package com.sistdist.controlador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;



public class Controlador {
    // Estado compartido
    static final Map<Integer, Double> humedades = new ConcurrentHashMap<>(); // por parcela

    static volatile double temp = 22.0, rad = 300.0; 
    static volatile boolean lluvia = false;
    static final int NUM_PARCELAS = 5;
    static final Bomba bomba = new Bomba();
    
    //Evitar riego concurrente con fertirriego
    static final Object lockBomba = new Object();
    static volatile boolean fertirrigando = false;
    
    // Conexiones activas a electroválvulas
    static final Map<Integer, PrintWriter> conexionesEV = new ConcurrentHashMap<>();

    
    public static void main(String[] args) {
        
        // Crear un hilo por cada parcela
        for (int i = 1; i <= NUM_PARCELAS; i++) {
            new HiloParcela(i, humedades, conexionesEV).start();
        }

     
        // Hilo de control (INR y decisiones)
        HiloControlador hc = new HiloControlador(humedades, conexionesEV);
        hc.start();

        // Hilo de sensores globales
        //new Thread(Controlador::simularSensoresGlobales).start();

        // Hilo de fertirrigación
        new Fertirrigacion().start();

        // Servidor de sensores + electrovalvulas
        try (ServerSocket server = new ServerSocket(20000)) {
            System.out.println("CONTROLADOR escuchando en 20000...");
            while (true) {
                Socket s = server.accept();
                BufferedReader bf = new BufferedReader(new InputStreamReader(s.getInputStream()));
                String header = bf.readLine(); // ej: "sensorHumedad;3"
                if (header == null) { s.close(); continue; }
                String[] parts = header.split(";");
                String tipo = parts[0];
                int id = parts.length > 1 ? Integer.parseInt(parts[1]) : -1;

                switch (tipo) {
                    
                    case "electrovalvula":
                         int evId = Integer.parseInt(parts[1]);
                         PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
                         conexionesEV.put(evId, pw);
                         System.out.println("[CTRL] EV " + evId + " conectada.");
                         new HiloReceptorEV(s,evId).start(); // para confirmaciones ok_abierta/ok_cerrada
                         break;
                    case "sensorHumedad":
                        HiloReceptorHumedad hrh = new HiloReceptorHumedad(s, id);
                        hrh.start();
                        break;
                        
                    case "sensorTemperatura":
                        temp = Double.parseDouble(parts[1]);
                        System.out.println("Temperatura actual: " + temp);
                        s.close();
                        break;
                        
                    case "sensorRadiacion":
                        rad = Double.parseDouble(parts[1]);
                        System.out.println("Radiacion actual: " + rad);
                        s.close();
                        break;
                    
                    case "fertirriego":
                        PrintWriter fert = new PrintWriter(s.getOutputStream(), true);
                        conexionesEV.put(7, fert);
                        System.out.println("[CTRL] Fertirrigación conectada.");
                        break;    
                        
                    case "sensorLluvia":
                        HiloReceptorLluvia hrl = new HiloReceptorLluvia(s, id);
                        hrl.start();
                        break;
                    default:
                        System.out.println("Tipo no reconocido: " + tipo);
                        s.close();
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    // Simulación rápida de T/R/L
    private static void simularSensoresGlobales() {
        Random r = new Random();
        while (true) {
            try {
                temp = Math.max(5, Math.min(40, temp + r.nextGaussian()*0.4));
                rad  = Math.max(0, Math.min(1000, rad + r.nextGaussian()*20));
                if (r.nextDouble() < 0.05) lluvia = !lluvia;

                //System.out.printf("[GLOBAL] T=%.1fC R=%.0f Lluvia=%s%n", temp, rad, lluvia?"SI":"NO");
                Thread.sleep(3000);
            } catch (InterruptedException e) { return; }
        }
    }
}
