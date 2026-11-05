import java.util.concurrent.ThreadLocalRandom;

public class Doctor implements Runnable {

    private static final long TREATMENT_ROOM_TIMEOUT_MS = 3000;
    private static final int MIN_TREATMENT_MS = 1000;
    private static final int MAX_TREATMENT_MS = 5000;

    private final int id;
    private final WaitingRoom waitingRoom;
    private final TreatmentRoom treatmentRoom;
    private final Hospital hospital;

    public Doctor(int id,
            WaitingRoom waitingRoom,
            TreatmentRoom treatmentRoom,
            Hospital hospital) {
        this.id = id;
        this.waitingRoom = waitingRoom;
        this.treatmentRoom = treatmentRoom;
        this.hospital = hospital;
    }

    @Override
    public void run() {
        while (true) {
            Patient patient = waitingRoom.take();

            if (patient == null) {
                break;
            }

            try {
                hospital.logEvent("Doctor " + id + " is assigned to P" + patient.getId() + ".",
                        "Doctor " + id + " is trying to acquire a treatment room.");
            } finally {
                waitingRoom.releaseSeat();
            }

            if (!treatmentRoom.tryAcquire(TREATMENT_ROOM_TIMEOUT_MS)) {
                hospital.logEvent("Doctor " + id + " could not acquire a treatment room.",
                        "Doctor " + id + " is sending P" + patient.getId()
                                + " back to the waiting room.");

                patient.markRejected();
                hospital.log("Doctor " + id + " is now available.");
                continue;
            }

            hospital.logEvent("Doctor " + id + " acquired a treatment room.",
                    "Doctor " + id + " begins treating P" + patient.getId() + ".");

            boolean completed = treat(patient);

            if (!completed) {
                patient.markRejected();
                break;
            }

            hospital.onPatientTreated(patient);
            hospital.log("Doctor " + id + " is now available.");
            patient.markTreated();
        }
    }

    private boolean treat(Patient patient) {
        try {
            int treatmentTime = ThreadLocalRandom.current()
                    .nextInt(MIN_TREATMENT_MS, MAX_TREATMENT_MS + 1);
            Thread.sleep(treatmentTime);
            hospital.log("Doctor " + id + " finished treating P" + patient.getId() + ".");
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } finally {
            hospital.log("Doctor " + id + " released the treatment room.");
            treatmentRoom.release();
        }
    }

    public int getId() {
        return id;
    }
}
