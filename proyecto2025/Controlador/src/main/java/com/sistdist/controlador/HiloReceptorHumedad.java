package com.sistdist.controlador;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.Map;

public class HiloReceptorHumedad extends Thread {
    private final Socket socket;
    private final int parcelaId;
    private final Map<Integer, Double> humedades;

    public HiloReceptorHumedad(Socket socket, int parcelaId) {
        this.socket = socket;
        this.parcelaId = parcelaId;
        this.humedades = Controlador.humedades; // uso el mapa compartido
    }

    @Override
    public void run() {
        System.out.printf("Receptor humedad iniciado para parcela %d%n", parcelaId);
        try (BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                try {
                    double valor = Double.parseDouble(linea.trim());
                    // Actualizo el estado compartido
                    humedades.put(parcelaId, valor);
                    // *** Logueo explícito de la humedad recibida ***
                    System.out.printf("[CTRL] Parcela %d | Humedad=%.14f%n", parcelaId, valor);
                } catch (NumberFormatException e) {
                    System.out.printf("[CTRL] Parcela %d | dato de humedad inválido: '%s'%n", parcelaId, linea);
                }
            }
        } catch (IOException e) {
            System.out.printf("[CTRL] Parcela %d | conexión de humedad cerrada (%s)%n", parcelaId, e.getMessage());
        }
    }
}
