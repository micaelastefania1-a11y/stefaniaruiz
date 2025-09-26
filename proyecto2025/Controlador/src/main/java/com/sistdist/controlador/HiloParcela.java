package com.sistdist.controlador;

import java.io.PrintWriter;
import java.util.Map;

public class HiloParcela extends Thread {
    private final int parcelaId;
    private final Map<Integer, Double> humedades;
    private final Map<Integer, PrintWriter> conexionesEV;

    public HiloParcela(int parcelaId, Map<Integer, Double> humedades, Map<Integer, PrintWriter> conexionesEV) {
        this.parcelaId = parcelaId;
        this.humedades = humedades;
        this.conexionesEV = conexionesEV;
    }

    private double inr(double H, double T, double R) {
        double w1 = 0.5, w2 = 0.3, w3 = 0.2;
        return w1*(1 - H/100.0) + w2*(T/40.0) + w3*(R/1000.0);
    }

    private int minutosRiego(double val) {
        if (val > 0.9) return 10;
        if (val > 0.8) return 7;
        if (val > 0.7) return 5;
        return 0;
    }

    @Override
public void run() {
    while (true) {
        try {
            double H = humedades.get(parcelaId);
            double T = Controlador.temp;
            double R = Controlador.rad;
            boolean L = Controlador.lluvia;

            // Calcular INR
            double val = inr(H, T, R);

            // Tabla de decisión
            int mins = 0;
            if (!L) {
                if (val > 0.9) {
                    mins = 10;
                } else if (val > 0.8) {
                    mins = 7;
                } else if (val > 0.7) {
                    mins = 5;
                }
            }

            // -------- Log más completo --------
            System.out.printf(
                "[CTRL] INR=%.3f | LLUEVE=%s | FERTI=%s%n" +
                "Parcela %d - Riego: %s %s%n" +
                "Sensores -> Humedad=%.1f | Temp=%.1f°C | Lluvia=%s | Radiación=%.0f%n%n",
                val,
                (L ? "SI" : "NO"),
                (Controlador.fertirrigando ? "ON" : "OFF"),
                parcelaId,
                (mins > 0 ? "SI" : "NO"),
                (mins > 0 ? "(" + mins + " min)" : (L ? "(LLUVIA)" : "(INR bajo)")),
                H, T, (L ? "1" : "0"), R
            );
            // ----------------------------------

            // Ejecutar riego si corresponde
            if (mins > 0) {
                synchronized (Controlador.lockBomba) {
                    if (!Controlador.fertirrigando) {
                        PrintWriter bomba = conexionesEV.get(6);
                        PrintWriter ev = conexionesEV.get(parcelaId);

                        if (bomba != null) { bomba.println("abrir"); bomba.flush(); }
                        if (ev != null) {
                            ev.println("abrir"); ev.flush();
                            Thread.sleep(mins * 1000L); // simular minutos en segundos
                            ev.println("cerrar"); ev.flush();
                        }
                        if (bomba != null) { bomba.println("cerrar"); bomba.flush(); }
                    } else {
                        System.out.println("[CTRL] Parcela " + parcelaId + " salta riego (FERTI activo).");
                    }
                }
            }

            Thread.sleep(2000);
        } catch (InterruptedException e) {
            return;
        }
    }
}

}
