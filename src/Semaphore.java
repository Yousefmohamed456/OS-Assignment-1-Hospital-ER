import java.util.concurrent.TimeUnit;

public class Semaphore {
    private int value;

    public Semaphore(int initialPermits) {
        if (initialPermits < 0) {
            throw new IllegalArgumentException("Initial permits cannot be negative");
        }
        this.value = initialPermits;
    }

    public synchronized void acquire() {
        boolean interrupted = false;

        while (value <= 0) {
            try {
                wait();
            } catch (InterruptedException e) {
                interrupted = true;
            }
        }

        value--;

        if (interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized boolean tryAcquire(long timeoutMillis) {
        if (timeoutMillis < 0) {
            throw new IllegalArgumentException("Timeout cannot be negative");
        }

        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);

        while (value <= 0) {
            long remaining = deadline - System.nanoTime();
            if (remaining <= 0) {
                return false;
            }

            try {
                TimeUnit.NANOSECONDS.timedWait(this, remaining);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }

        value--;
        return true;
    }

    public synchronized void release() {
        value++;
        notifyAll();
    }

    public synchronized int availablePermits() {
        return value;
    }
}
