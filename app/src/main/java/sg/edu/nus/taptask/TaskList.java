package sg.edu.nus.taptask;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;

/**
 * Created by musho on 24/3/2015.
 */
// mock task data class to display mock list of tasks
public class TaskList {

    // attributes of one task - type, name and last triggered time. (Temporary)
    private static int[] taskTypeArray = {1, 4, 3, 4, 1, 2, 3}; // 1: call 2: reject call 3: SMS 4: volume
    private static String[] taskNameArray = {"Call mom", "Adjust Volume to 0", "SMS 'Help' to Gary", "Adjust volume to 70", "Call Sandra", "Reject call", "SMS 'I love you' to Grandma"};
    private static Date tempTime = new Date(2015, 3, 12, 8, 45);

    private static TaskList mInstance;
    private List<Task> tasks;

    // functions
    public static TaskList getInstance() {
        if (mInstance == null) {
            mInstance = new TaskList();
        }

        return mInstance;
    }

    public List<Task> getTasks() {
        if (tasks == null) {
            tasks = new ArrayList<Task>();

            for (int i = 0; i < taskTypeArray.length; i++) {
                Task task = new Task();
                task.name = taskNameArray[i];
                task.type = taskTypeArray[i];
                task.lastTriggeredTime = tempTime;
                tasks.add(task);
            }
        }
        return tasks;
    }

}
