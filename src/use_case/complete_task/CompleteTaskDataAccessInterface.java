package use_case.complete_task;

public interface CompleteTaskDataAccessInterface {
    boolean taskNameExists(String projectName, String taskName);

    void completeTask(String projectName, String taskName);
}
