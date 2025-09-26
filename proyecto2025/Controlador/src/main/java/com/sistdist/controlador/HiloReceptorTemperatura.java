package com.sistdist.controlador;

import java.io.*;
import java.net.Socket;
import java.util.logging.*;

public class HiloReceptorTemperatura extends Thread {
    private final int id;
    private final BufferedReader br;

    public HiloReceptorTemperatura(Socket s, int id) {
        this.id = id;
        try { this.br = new BufferedReader(new InputStreamReader(s.getInputStream())); }
        catch (IOException ex) { throw new RuntimeException(ex); }
    }

    @Override
    public void run() {
        System.out.println("[CTRL] Receptor de TEMPERATURA iniciado (id=" + id + ")");
        while (true) {
            try {
                String entrada = br.readLine();
                if (entrada == null) break;

                // Soporta "sensorTemperatura;23.5" o directamente "23.5"
                String[] parts = entrada.trim().split(";");
                String valorStr = parts[parts.length - 1];
                double t = Double.parseDouble(valorStr);

                Controlador.temp = t;
                Controlador.tempReady = true;

                System.out.printf("[CTRL] Temp(id=%d) -> %.2f°C%n", id, t);
            } catch (IOException ex) {
                Logger.getLogger(HiloReceptorTemperatura.class.getName()).log(Level.SEVERE, null, ex);
                break;
            } catch (NumberFormatException nfe) {
                System.out.println("[CTRL] Valor de temperatura inválido: " + nfe.getMessage());
            }
        }
    }
}
