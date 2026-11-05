public class TreatmentRoom {
    private Semaphore Semaphore1;

    public TreatmentRoom() {
        this.Semaphore1 = new Semaphore(2);
    }

    public boolean tryAcquire(long timeout) {
        return Semaphore1.tryAcquire(timeout); // bnktb el w2t b millsecond 
    }

    public void release() {
        Semaphore1.release();
    }
}