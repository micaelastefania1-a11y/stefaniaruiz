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
        return w1 * (1 - H / 100.0) + w2 * (T / 40.0) + w3 * (R / 1000.0);
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
                Double hVal = humedades.get(parcelaId);
                Double tVal = Controlador.temp;
                Double rVal = Controlador.rad;
                Boolean lVal = Controlador.lluvia;

                if (hVal == null || tVal == null || rVal == null || lVal == null) {
                    Thread.sleep(2000);
                    continue; // todavía no hay datos de sensores
                }

                double H = hVal;
                double T = tVal;
                double R = rVal;
                boolean L = lVal;

                // Calcular INR
                double val = inr(H, T, R);
                int mins = (!L) ? minutosRiego(val) : 0;

                // Ejecutar riego si corresponde
                if (mins > 0) {
                    synchronized (Controlador.lockBomba) {
                        if (!Controlador.fertirrigando) {
                            PrintWriter bomba = conexionesEV.get(6);
                            PrintWriter ev = conexionesEV.get(parcelaId);

                            if (bomba != null && ev != null) {
                                bomba.println("abrir");
                                bomba.flush();

                                ev.println("abrir");
                                ev.flush();

                                Thread.sleep(mins * 1000L);

                                ev.println("cerrar");
                                ev.flush();

                                bomba.println("cerrar");
                                bomba.flush();
                            }
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
