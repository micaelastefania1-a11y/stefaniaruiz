
package com.sistdist.sensorhumedad1;

import java.io.PrintWriter;
import java.lang.Math;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author lesca
 */
public class HiloSensado extends Thread {
    
    private boolean on;
    private double humedad;
    Socket cnxServidor;
    PrintWriter pw;
    
    public HiloSensado(Socket s, PrintWriter imp){
        on = false;
        cnxServidor = s;
        pw = imp;
    }
    
    public double generarHumedad(){
        return Math.random() * 100;
    }
    
    public void encender(){
        on = true;
    }
    
    public void apagar(){
        on = false;
    }
    
    public double leerHumedad(){
        return humedad;
    }    
    
    public void run(){
        on = true;
        while(on){
            humedad = generarHumedad();
            
            System.out.println(humedad);
            
            pw.println(humedad);
            pw.flush();

            try {
                Thread.sleep(5000);
            } catch (InterruptedException ex) {
                Logger.getLogger(HiloSensado.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
