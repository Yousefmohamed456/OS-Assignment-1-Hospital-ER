public class Patient implements Runnable {

    private final int id;
    private final WaitingRoom waitingRoom;
    private final Hospital hospital;

    private boolean treated = false;
    private boolean rejected = false;

    public Patient(int id, WaitingRoom waitingRoom, Hospital hospital) {
        this.id = id;
        this.waitingRoom = waitingRoom;
        this.hospital = hospital;
    }

    @Override
    public void run() {
        hospital.log("P" + id + " arrived.");

        while (true) {
            if (!waitingRoom.put(this)) {
                return;
            }

            synchronized (this) {
                while (!treated && !rejected) {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }

                if (treated) {
                    break;
                }

                rejected = false;
            }
        }

        hospital.log("P" + id + " left.");
    }

    public synchronized void markRejected() {
        rejected = true;
        notifyAll();
    }

    public synchronized void markTreated() {
        treated = true;
        notifyAll();
    }

    public int getId() {
        return id;
    }
}
