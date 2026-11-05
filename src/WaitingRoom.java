import java.util.LinkedList;
import java.util.Queue;

public class WaitingRoom {
    private final int capacity;
    private final Queue<Patient> queue = new LinkedList<>();

    private final Semaphore availableSeats;
    private final Semaphore availablePatients;
    private final Semaphore mutex = new Semaphore(1);

    private final Hospital hospital;
    private volatile boolean closed = false;

    public WaitingRoom(int capacity, Hospital hospital) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Waiting room capacity must be positive");
        }
        this.capacity = capacity;
        this.hospital = hospital;
        this.availableSeats = new Semaphore(capacity);
        this.availablePatients = new Semaphore(0);
    }

    public boolean put(Patient patient) {
        if (closed) {
            return false;
        }

        boolean hasSeat = availableSeats.tryAcquire(0);

        if (!hasSeat) {
            hospital.logEvent("The waiting room is full.",
                    "P" + patient.getId()
                            + " is waiting outside for an available seat.");

            while (!closed) {
                if (availableSeats.tryAcquire(100)) {
                    hasSeat = true;
                    break;
                }
            }

            if (!hasSeat) {
                return false;
            }
        }

        mutex.acquire();
        if (closed) {
            mutex.release();
            availableSeats.release();
            return false;
        }
        queue.add(patient);
        mutex.release();

        hospital.log("P" + patient.getId() + " entered the waiting room.");
        availablePatients.release();
        return true;
    }

    public Patient take() {
        while (true) {
            if (availablePatients.tryAcquire(100)) {
                break;
            }
            if (closed && isEmpty()) {
                return null;
            }
        }

        mutex.acquire();
        Patient patient = queue.poll();
        mutex.release();

        if (patient == null) {
            availablePatients.release();
            return null;
        }

        return patient;
    }

    public void releaseSeat() {
        availableSeats.release();
    }

    public void close() {
        closed = true;
    }

    public int size() {
        mutex.acquire();
        try {
            return queue.size();
        } finally {
            mutex.release();
        }
    }

    public int getCapacity() {
        return capacity;
    }

    private boolean isEmpty() {
        mutex.acquire();
        try {
            return queue.isEmpty();
        } finally {
            mutex.release();
        }
    }
}
