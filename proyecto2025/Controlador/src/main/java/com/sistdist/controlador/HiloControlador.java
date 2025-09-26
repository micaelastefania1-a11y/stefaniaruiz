package com.sistdist.controlador;

import java.io.PrintWriter;
import java.util.Map;

public class HiloControlador extends Thread {
    private final Map<Integer, Double> humedades;
    private final Map<Integer, PrintWriter> conexionesEV;

    // pesos (ajustables)
    private final double w1 = 0.5, w2 = 0.3, w3 = 0.2;

    public HiloControlador(Map<Integer, Double> humedades, Map<Integer, PrintWriter> conexionesEV) {
        this.humedades = humedades;
        this.conexionesEV = conexionesEV;
    }

    private double inr(double H, double T, double R) {
        return w1 * (1 - H / 100.0) + w2 * (T / 40.0) + w3 * (R / 1000.0);
    }

    private int minutosRiegoPorRangos(double inr) {
        if (inr > 0.9) return 10;
        if (inr > 0.8 && inr < 0.9) return 7;
        if (inr > 0.7 && inr < 0.8) return 5;
        return 0;
    }

    @Override
    public void run() {
        while (true) {
            try {
                for (int parcelaId : humedades.keySet()) {
                    double H = humedades.get(parcelaId);

                    // INR requiere T y R; la lluvia solo afecta la decisión de regar
                    boolean trReady = Controlador.tempReady && Controlador.radReady;
                    if (!trReady) {
                        System.out.printf("[CTRL] Parcela %d | Humedad=%.6f | INR=PENDIENTE %n",
                                parcelaId, H);
                        continue;
                    }

                    double T = Controlador.temp;
                    double R = Controlador.rad;

                    double val = inr(H, T, R);

                    // Si no conocemos la lluvia aún, no regamos por seguridad.
                    boolean lluviaConocida = Controlador.lluviaReady;
                    boolean L = Controlador.lluvia;

                    int mins = 0;
                    if (lluviaConocida && !L) {
                        mins = minutosRiegoPorRangos(val);
                    }

                    String lluviaStr = lluviaConocida ? (L ? "SI" : "NO") : "?";
                    String accion;
                    if (!lluviaConocida) {
                        accion = "NO REGAR (lluvia ?)";
                    } else if (L) {
                        accion = "NO REGAR (LLUVIA)";
                    } else {
                        accion = (mins > 0) ? ("REGAR " + mins + " min") : "NO REGAR";
                    }

                    System.out.printf(
                            "[CTRL] Parcela %d | Humedad=%.6f | INR=%.3f | Lluvia=%s -> %s%n",
                            parcelaId, H, val, lluviaStr, accion
                    );

                    // Ejecutar riego solo si corresponde
                    if (mins > 0) {
                        synchronized (Controlador.lockBomba) {
                            if (!Controlador.fertirrigando) {
                                PrintWriter bomba = conexionesEV.get(6);
                                PrintWriter ev = conexionesEV.get(parcelaId);

                                if (bomba != null) { bomba.println("abrir"); bomba.flush(); }
                                if (ev != null) {
                                    ev.println("abrir"); ev.flush();
                                    Thread.sleep(mins * 1000L); // simular "minutos" en segundos
                                    ev.println("cerrar"); ev.flush();
                                }
                                if (bomba != null) { bomba.println("cerrar"); bomba.flush(); }
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
