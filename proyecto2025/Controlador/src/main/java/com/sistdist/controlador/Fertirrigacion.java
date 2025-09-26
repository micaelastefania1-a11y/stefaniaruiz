package com.sistdist.controlador;

public class Fertirrigacion extends Thread {
    @Override
    public void run() {
        while (true) {
            try {
                // Esperar un rato antes de arrancar fertirriego (ej: cada 60 seg)
                Thread.sleep(60000);

                synchronized (Controlador.lockBomba) {
                    Controlador.fertirrigando = true;

                    // Abrir electroválvula de fertirriego (ID=7)
                    if (Controlador.conexionesEV.get(7) != null) {
                        Controlador.conexionesEV.get(7).println("abrir");
                        Controlador.conexionesEV.get(7).flush();
                    }

                    System.out.println("[CTRL] >>> Iniciando fertirrigación...");

                    // Simular 5 seg de fertirriego
                    Thread.sleep(5000);

                    // Cerrar electroválvula de fertirriego
                    if (Controlador.conexionesEV.get(7) != null) {
                        Controlador.conexionesEV.get(7).println("cerrar");
                        Controlador.conexionesEV.get(7).flush();
                    }

                    Controlador.fertirrigando = false;
                    System.out.println("[CTRL] <<< Fertirrigación finalizada.");
                }

            } catch (InterruptedException e) {
                return; // salir del hilo si lo interrumpen
            }
        }
    }
}
