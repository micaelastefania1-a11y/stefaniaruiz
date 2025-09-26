package com.sistdist.controlador;
//Un componente compartido que suministra agua tanto para el riego como para el sistema de fertirrigación

import java.util.concurrent.locks.ReentrantLock;

public class Bomba {
    private final ReentrantLock lock = new ReentrantLock(true); // fair

    public void adquirir(String quien) {
        lock.lock();
        System.out.println("[" + quien + "] adquirio la bomba.");
    }

    public void liberar(String quien) {
        System.out.println("[" + quien + "] libero la bomba.");
        lock.unlock();
    }
}