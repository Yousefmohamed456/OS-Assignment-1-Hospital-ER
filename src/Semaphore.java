public class Semaphore {
    protected int value = 0;
    protected int waiting = 0; 
    protected  Semaphore(int initialPermits) {value = initialPermits;}

    public synchronized void acquire() {
        value--;
        if (value < 0) {
            while (waiting == 0) {
                try { wait(); } catch (InterruptedException e) { }
            }
            waiting--;
        }
    }

    public synchronized boolean tryAcquire(long timeout) {
        value--;
        if (value >= 0) return true;
        long timeoutEnd = System.currentTimeMillis() + timeout;
        while (waiting == 0) {
            long timeLeft = timeoutEnd - System.currentTimeMillis();
            if (timeLeft <= 0) break;
            try { wait(timeLeft); }
            catch (InterruptedException e) { break; }
        }
        if (waiting > 0) {
            waiting--;
            return true;
        }
        value++;
        return false;
    }
    

    public synchronized void release() {
        value++;
        if (value <= 0) {
            waiting++;
            notify();
        }

    }
}
