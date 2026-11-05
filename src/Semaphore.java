public class Semaphore {
    protected int value = 0;
    protected  Semaphore(int initialPermits) {value = initialPermits;}

    public synchronized void acquire() {
            while (value <= 0) {
                try { wait(); } catch (InterruptedException e) { }
            }
            value--; 
    }

    public synchronized boolean tryAcquire(long timeout) {
        long timeoytEnd = System.currentTimeMillis() + timeout;
        while (value <= 0) {
            long timeoutLeft = timeoytEnd - System.currentTimeMillis();
            if (timeoutLeft <= 0) return false;
            try { wait(timeoutLeft); }
            catch (InterruptedException e) { return false; }
        }
        value--;
        return true;
    }
    

    public synchronized void release() {
        value++;
        if (value <= 0) {
            notify();
        }

    }
}
