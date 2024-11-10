import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SharedInventoryManagement {
    private final int maxCapacity;
    private int currentInventory = 0;
    // Lock to ensure thread safety
    private final Lock lock = new ReentrantLock();
    // Inventory is not full (for suppliers)
    private final Condition notFull = lock.newCondition();
    // Inventory is not empty (for customers)
    private final Condition notEmpty = lock.newCondition();

    public SharedInventoryManagement(int maxCapacity) {
        this.maxCapacity = maxCapacity;
    }

    // Method for suppliers to add items to the inventory
    public void addItems(int amount) throws InterruptedException {
        lock.lock();
        try {
            // Wait if adding items would exceed the maximum capacity
            while (currentInventory + amount > maxCapacity) {
                notFull.await();
            }
            currentInventory += amount;
            System.out.println("Supplier added " + amount + " items. Current inventory: " + currentInventory);
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    // Method for customers to remove items from the inventory
    public void removeItems(int amount) throws InterruptedException {
        lock.lock();
        try {
            // Wait if there are not enough items to remove
            while (currentInventory < amount) {
                notEmpty.await();
            }
            currentInventory -= amount;
            System.out.println("Customer removed " + amount + " items. Current inventory: " + currentInventory);
            notFull.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        SharedInventoryManagement inventory = new SharedInventoryManagement(100);

        // Supplier thread to add items to the inventory
        Thread supplierThread = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    inventory.addItems(5);
                    Thread.sleep(500); //
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Customer thread to remove items from the inventory
        Thread customerThread = new Thread(() -> {
            try {
                for (int i = 0; i < 5; i++) {
                    inventory.removeItems(3);
                    Thread.sleep(2000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        // Start both supplier and customer threads
        supplierThread.start();
        customerThread.start();
    }
}
