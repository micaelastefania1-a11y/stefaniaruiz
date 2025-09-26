package com.sistdist.sensorradiacion;

import java.io.*;
import java.net.*;

public class SensorRadiacion {
    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1", 20000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Cabecera de identificación
            out.println("sensorRadiacion;-1"); // -1 ó cualquier id que uses

            while (true) {
                double rad = Math.max(0, Math.min(1000, 500 + (Math.random()-0.5)*200));
                out.println("sensorRadiacion;" + rad);
                out.flush();
                Thread.sleep(5000);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }
}
