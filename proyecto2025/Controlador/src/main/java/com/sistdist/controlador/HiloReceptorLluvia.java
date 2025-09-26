package com.sistdist.controlador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HiloReceptorLluvia extends Thread {
    private final int id;
    private final BufferedReader br;

    public HiloReceptorLluvia(Socket s, int id) {
        this.id = id;
        try {
            this.br = new BufferedReader(new InputStreamReader(s.getInputStream()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void run() {
        System.out.println("[CTRL] Receptor de LLUVIA iniciado (id=" + id + ")");
        while (true) {
            try {
                String linea = br.readLine();
                if (linea == null) break;

                // Soporta "sensorLluvia;1" o simplemente "1"/"0"
                String[] parts = linea.trim().split(";");
                String valorStr = parts[parts.length - 1];

                int v = Integer.parseInt(valorStr); // 0 o 1
                Controlador.lluvia = (v != 0);
                Controlador.lluviaReady = true;

                System.out.printf("[CTRL] Lluvia(id=%d) -> %s%n", id, Controlador.lluvia ? "SI" : "NO");
            } catch (NumberFormatException nfe) {
                System.out.println("[CTRL] Valor de lluvia inválido: " + nfe.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(HiloReceptorLluvia.class.getName())
                      .log(Level.INFO, "Conexión de lluvia cerrada: {0}", ex.getMessage());
                break;
            }
        }
    }
}
