public class Doctor implements Runnable {
    public Doctor(int id,
                  WaitingRoom waitingRoom,
                  TreatmentRoom treatmentRoom,
                  Hospital hospital) {}

    @Override
    public void run() {}

    public int getId() {
        return 0;
    }

    public void treat(Patient patient) {}
}
