
    public class Patient implements Runnable {

        private final int id;
        private final WaitingRoom waitingRoom;
        private final Hospital hospital;

        public Patient(int id,
                    WaitingRoom waitingRoom,
                    Hospital hospital) {
            this.id = id;
            this.waitingRoom = waitingRoom;
            this.hospital = hospital;
        }

        @Override
        public void run() {
            hospital.log("P" + id + " arrived.");

            // put() must block while the room is full.
            waitingRoom.put(this);

            hospital.log("P" + id + " entered the waiting room.");
        }

        public int getId() {
            return id;
        }
    }
