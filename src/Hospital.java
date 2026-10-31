import java.util.ArrayList;
import java.util.InputMismatchException;
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

    private final Object shutdownLock = new Object();
    private int treatedCount = 0;
    private volatile boolean allPatientsTreated = false;

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
        log("Starting hospital simulation...");
        log("Waiting room capacity: " + waitingRoomCapacity);
        log("Number of doctors: " + numberOfDoctors);
        log("Number of patients: " + numberOfPatients);

        waitingRoom = new WaitingRoom(waitingRoomCapacity);
        treatmentRoom = new TreatmentRoom(2); 

        for (int i = 0; i < numberOfPatients; i++) {
            Patient patient = new Patient(i, waitingRoom, this);
            Thread patientThread = new Thread(patient, "Patient-" + i);
            patientThreads.add(patientThread);
            patientThread.start();
        }

        for (int i = 0; i < numberOfDoctors; i++) {
            Doctor doctor = new Doctor(i, waitingRoom, treatmentRoom, this);
            Thread doctorThread = new Thread(doctor, "Doctor-" + i);
            doctorThreads.add(doctorThread);
            doctorThread.start();
        }

        for (Thread t : patientThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Interrupted while waiting for patient thread: " + t.getName());
                return;
            }
        }

        synchronized (shutdownLock) {
            allPatientsTreated = true;
            shutdownLock.notifyAll();
        }
        log("All patients have completed treatment. Initiating doctor shutdown...");

        waitingRoom.close();

        for (Thread t : doctorThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log("Interrupted while waiting for doctor thread: " + t.getName());
                return;
            }
        }

        log("All patients have been treated.");
        log("Simulation completed.");
    }

    public void log(String message) {
        System.out.println(message);
    }

    public void onPatientTreated(Patient patient) {
        synchronized (shutdownLock) {
            treatedCount++;
            log("Patient " + patient.getId() + " has been treated. (" + treatedCount + "/" + numberOfPatients + ")");
            if (treatedCount >= numberOfPatients) {
                allPatientsTreated = true;
                shutdownLock.notifyAll();
            }
        }
    }

    public boolean areAllPatientsTreated() {
        synchronized (shutdownLock) {
            return allPatientsTreated;
        }
    }

    private static int readPositiveInt(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = scanner.nextInt();
                if (value > 0) {
                    return value;
                }
                System.out.println("Value must be a positive integer. Please try again.");
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a positive integer.");
                scanner.next(); 
            }
        }
    }
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("=== Hospital Emergency Room Simulation ===");
        System.out.println();

        int waitingRoomCapacity = readPositiveInt(scanner, "Enter waiting room capacity: ");
        int numberOfDoctors = readPositiveInt(scanner, "Enter number of doctors: ");
        int numberOfPatients = readPositiveInt(scanner, "Enter number of patients: ");

        scanner.close();

        Hospital hospital = new Hospital(waitingRoomCapacity, numberOfDoctors, numberOfPatients);
        hospital.startSimulation();
    }
}
