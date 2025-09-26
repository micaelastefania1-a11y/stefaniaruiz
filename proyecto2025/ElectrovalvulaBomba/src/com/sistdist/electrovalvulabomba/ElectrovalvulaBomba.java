package com.sistdist.electrovalvulabomba;

import java.io.*;
import java.net.*;

public class ElectrovalvulaBomba {
    public static void main(String[] args) {
        int id = 6; // ⚡ ID de la válvula de la bomba

        try {
            // Conexión al sistema central
            Socket socket = new Socket("127.0.0.1", 20000);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            // Me identifico ante el controlador
            out.println("electrovalvula;" + id);

            System.out.println("[EV-Bomba] Conectada al sistema central.");

            // Escucho órdenes
            String comando;
            while ((comando = in.readLine()) != null) {
                if (comando.equals("abrir")) {
                    System.out.println("[EV-Bomba] Válvula abierta → bomba de agua activada.");
                } else if (comando.equals("cerrar")) {
                    System.out.println("[EV-Bomba] Válvula cerrada → bomba de agua detenida.");
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
