import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Hospital {
    private final int waitingRoomCapacity;
    private final int numberOfDoctors;
    private final int numberOfPatients;

    private WaitingRoom waitingRoom;
    private TreatmentRoom treatmentRoom;

    private final List<Thread> patientThreads = new ArrayList<>();
    private final List<Thread> doctorThreads = new ArrayList<>();

    private final Object countLock = new Object();
    private final Object logLock = new Object();
    private int treatedCount = 0;

    public Hospital(int waitingRoomCapacity, int numberOfDoctors, int numberOfPatients) {
        if (waitingRoomCapacity <= 0) {
            throw new IllegalArgumentException("Waiting room capacity must be a positive integer");
        }
        if (numberOfDoctors <= 0) {
            throw new IllegalArgumentException("Number of doctors must be a positive integer");
        }
        if (numberOfPatients <= 0) {
            throw new IllegalArgumentException("Number of patients must be a positive integer");
        }

        this.waitingRoomCapacity = waitingRoomCapacity;
        this.numberOfDoctors = numberOfDoctors;
        this.numberOfPatients = numberOfPatients;
    }

    public void startSimulation() {
        waitingRoom = new WaitingRoom(waitingRoomCapacity, this);
        treatmentRoom = new TreatmentRoom();

        log(patientArrivalList());

        for (int i = 1; i <= numberOfDoctors; i++) {
            Doctor doctor = new Doctor(i, waitingRoom, treatmentRoom, this);
            Thread doctorThread = new Thread(doctor, "Doctor-" + i);
            doctorThreads.add(doctorThread);
            doctorThread.start();
        }

        for (int i = 1; i <= numberOfPatients; i++) {
            Patient patient = new Patient(i, waitingRoom, this);
            Thread patientThread = new Thread(patient, "Patient-" + i);
            patientThreads.add(patientThread);
            patientThread.start();
        }

        for (Thread t : patientThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                waitingRoom.close();
                log("Simulation interrupted while waiting for patients.");
                return;
            }
        }

        waitingRoom.close();

        for (Thread t : doctorThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Simulation interrupted while waiting for doctors.");
                return;
            }
        }

        synchronized (countLock) {
            if (treatedCount != numberOfPatients) {
                log("Simulation ended with " + treatedCount + " of "
                        + numberOfPatients + " patients treated.");
                return;
            }
        }

        log("All patients have been treated.");
        log("Simulation completed.");
    }

    private String patientArrivalList() {
        StringBuilder result = new StringBuilder("Patients arriving: ");
        for (int i = 1; i <= numberOfPatients; i++) {
            if (i > 1) {
                result.append(' ');
            }
            result.append('P').append(i);
        }
        return result.toString();
    }

    public void log(String message) {
        synchronized (logLock) {
            System.out.println(message);
        }
    }

    public void logEvent(String... messages) {
        synchronized (logLock) {
            for (String message : messages) {
                System.out.println(message);
            }
        }
    }

    public void onPatientTreated(Patient patient) {
        synchronized (countLock) {
            treatedCount++;
        }
    }

    public boolean areAllPatientsTreated() {
        synchronized (countLock) {
            return treatedCount == numberOfPatients;
        }
    }

    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);

            if (!scanner.hasNext()) {
                System.out.println();
                System.out.println("No more input. Exiting.");
                return -1;
            }

            if (scanner.hasNextInt()) {
                int value = scanner.nextInt();
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be a positive integer. Please try again.");
            } else {
                scanner.next();
                System.out.println("Invalid input. Please enter a positive integer.");
            }
        }
    }

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Hospital Emergency Room Simulation ===");
        System.out.println();

        int waitingRoomCapacity = readPositiveInt(scanner, "Enter waiting room capacity: ");
        if (waitingRoomCapacity < 0) {
            scanner.close();
            return;
        }

        int numberOfDoctors = readPositiveInt(scanner, "Enter number of doctors: ");
        if (numberOfDoctors < 0) {
            scanner.close();
            return;
        }

        int numberOfPatients = readPositiveInt(scanner, "Enter number of patients: ");
        if (numberOfPatients < 0) {
            scanner.close();
            return;
        }

        scanner.close();

        Hospital hospital = new Hospital(waitingRoomCapacity, numberOfDoctors, numberOfPatients);
        hospital.startSimulation();
    }
}
