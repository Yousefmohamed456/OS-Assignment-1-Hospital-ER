import java.util.concurrent.ThreadLocalRandom;

public class Doctor implements Runnable {

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

        while (!Thread.currentThread().isInterrupted()) {

            Patient patient = waitingRoom.take();

            if (patient == null) {
                break;
            }

            hospital.log("Doctor " + (id + 1)
                    + " is assigned to P" + (patient.getId() + 1) + ".");

            hospital.log("Doctor " + (id + 1)
                    + " is trying to acquire a treatment room.");

            boolean acquired = treatmentRoom.tryAcquire(3000);

            if (!acquired) {

                hospital.log("Doctor " + (id + 1)
                        + " could not acquire a treatment room.");

                hospital.log("Doctor " + (id + 1)
                        + " is sending P" + (patient.getId() + 1)
                        + " back to the waiting room.");

                waitingRoom.put(patient);

                continue;
            }

            hospital.log("Doctor " + (id + 1)
                    + " acquired a treatment room.");

            treat(patient);

            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            hospital.onPatientTreated(patient);

            hospital.log("Doctor " + (id + 1)
                    + " is now available.");

            hospital.log("P" + (patient.getId() + 1) + " left.");
        }
    }

    public int getId() {
        return id;
    }

    public void treat(Patient patient) {

        hospital.log("Doctor " + (id + 1)
                + " begins treating P" + (patient.getId() + 1) + ".");

        try {

            int treatmentTime =
                    ThreadLocalRandom.current().nextInt(1000, 5001);

            Thread.sleep(treatmentTime);

            hospital.log("Doctor " + (id + 1)
                    + " finished treating P" + (patient.getId() + 1) + ".");

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

        } finally {
            treatmentRoom.release();

            hospital.log("Doctor " + (id + 1)
                    + " released the treatment room.");
        }
    }
}
