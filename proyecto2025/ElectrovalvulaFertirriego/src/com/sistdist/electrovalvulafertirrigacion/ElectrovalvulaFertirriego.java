package com.sistdist.electrovalvulafertirrigacion;

import java.io.*;
import java.net.*;

public class ElectrovalvulaFertirriego {
    public static void main(String[] args) {
        int id = 7; // ID para la EV de fertirrigación
        try (Socket socket = new Socket("127.0.0.1", 20000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Handshake inicial con el controlador
            out.println("electrovalvula;" + id);
            out.flush();
            System.out.println("[EV-FERTI] Conectada al controlador.");

            String comando;
            while ((comando = in.readLine()) != null) {
                if ("abrir".equalsIgnoreCase(comando)) {
                    System.out.println("[EV-FERTI] Válvula de fertirrigación abierta.");
                    out.println("EV" + id + ":ok_abierta"); // confirmación al controlador
                } else if ("cerrar".equalsIgnoreCase(comando)) {
                    System.out.println("[EV-FERTI] Válvula de fertirrigación cerrada.");
                    out.println("EV" + id + ":ok_cerrada"); // confirmación al controlador
                } else {
                    System.out.println("[EV-FERTI] Comando desconocido: " + comando);
                    out.println("EV" + id + ":error_comando");
                }
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

