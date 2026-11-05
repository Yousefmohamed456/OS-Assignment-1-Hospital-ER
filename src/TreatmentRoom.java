public class TreatmentRoom {
    private final Semaphore rooms;

    public TreatmentRoom() {
        this.rooms = new Semaphore(2);
    }

    public boolean tryAcquire(long timeoutMillis) {
        return rooms.tryAcquire(timeoutMillis);
    }

    public void release() {
        rooms.release();
    }

    public int availableRooms() {
        return rooms.availablePermits();
    }
}
