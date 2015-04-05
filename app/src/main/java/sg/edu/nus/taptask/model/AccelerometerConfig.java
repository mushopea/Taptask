package sg.edu.nus.taptask.model;


// TODO: implement this to store/read/write accelerometer configuration
public class AccelerometerConfig {
    private static AccelerometerConfig ourInstance = new AccelerometerConfig();

    public static AccelerometerConfig getInstance() {
        return ourInstance;
    }

    private AccelerometerConfig() {
    }


}
