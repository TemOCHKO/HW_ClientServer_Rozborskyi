/*
package org.temochko;

import org.temochko.Models.Product;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Storage {
    private ArrayList<Product> storage;

    public Storage() {
        storage = new ArrayList<>();
        addStock(2, 500);
    }

    public int getStock(int id) {
        AtomicInteger stock = storage.get(id);
        if (stock == null) {
            return -1;
        }
        return stock.get();
    }

    public void addStock(int id, int amount) {
        storage.computeIfAbsent(id, k -> new AtomicInteger(0)).addAndGet(amount);
    }

    public boolean removeStock(int id, int amount) {
        AtomicInteger stock = storage.get(id);

        if (stock == null) {
            return false;
        }

        while (true) {
            int current = stock.get();
            if (current < amount) {
                return false;
            }

            if (stock.compareAndSet(current, current - amount)) {
                return true;
            }
        }
    }

};
*/
