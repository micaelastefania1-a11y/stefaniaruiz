package com.sistdist.electrovalvulafertirrigacion;

import java.io.*;
import java.net.*;

public class ElectrovalvulaFertirriego {
    public static void main(String[] args) {
        int id = 7; // ID de la fertirrigación
        try (Socket socket = new Socket("127.0.0.1", 20000);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            // Handshake inicial
            out.println("electrovalvula;" + id);
            out.flush();
            System.out.println("[EV-FERTI] Conectada al controlador como EV" + id);

            String comando;
            while ((comando = in.readLine()) != null) {
                if ("abrir".equalsIgnoreCase(comando)) {
                    System.out.println("[EV-FERTI] Válvula de fertirrigación abierta.");
                    out.println("EV" + id + ":ok_abierta");
                } else if ("cerrar".equalsIgnoreCase(comando)) {
                    System.out.println("[EV-FERTI] Válvula de fertirrigación cerrada.");
                    out.println("EV" + id + ":ok_cerrada");
                } else {
                    System.out.println("[EV-FERTI] Comando desconocido: " + comando);
                    out.println("EV" + id + ":error");
                }
                out.flush();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

