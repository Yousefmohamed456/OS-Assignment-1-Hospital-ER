public class Semaphore {
    public Semaphore(int initialPermits) {}

    public void acquire() {}

    public boolean tryAcquire(long timeoutMillis) {
        return false;
    }

    public void release() {}
}
