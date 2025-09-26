package com.sistdist.controlador;

import java.io.*;
import java.net.*;

public class HiloReceptorEV extends Thread {
    private final int evId;
    private final BufferedReader br;

    public HiloReceptorEV(Socket socket, int evId) throws IOException {
        this.evId = evId;
        this.br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    }

    @Override
    public void run() {
        try {
            String entrada;
            while ((entrada = br.readLine()) != null) {
                // cada EV responde con algo como "EV1:ok_abierta"
                System.out.println("[CTRL] Confirmación recibida de EV" + evId + ": " + entrada);
            }
        } catch (IOException e) {
            System.out.println("[CTRL] Conexión cerrada con EV" + evId);
        }
    }
}

