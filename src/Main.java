import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;
import service.InMemoryTaskManager;

public class Main {

    public static void main(String[] args) {

        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();

        //Создание
        Task task1 = new Task("model.Task 1", "Task1 description", Status.NEW);
        Task task2 = new Task("model.Task 2", "Task2 description", Status.DONE);
        inMemoryTaskManager.addNewTask(task1);
        inMemoryTaskManager.addNewTask(task2);

        Epic epic1 = new Epic("model.Epic 1", "Epic1 description");
        Epic epic2 = new Epic("model.Epic 2", "Epic2 description");
        inMemoryTaskManager.addNewEpic(epic1);
        inMemoryTaskManager.addNewEpic(epic2);

        Subtask subtask11 = new Subtask("Epic1 Subtask1", "Epic1 Subtask1 description", Status.NEW, epic1.getId());
        Subtask subtask12 = new Subtask("Epic1 Subtask2", "Epic1 Subtask2 description", Status.DONE, epic1.getId());
        Subtask subtask21 = new Subtask("Epic2 Subtask1", "Epic2 Subtask1 description", Status.DONE, epic2.getId());
        inMemoryTaskManager.addNewSubtask(subtask11);
        inMemoryTaskManager.addNewSubtask(subtask12);
        inMemoryTaskManager.addNewSubtask(subtask21);

        inMemoryTaskManager.getTasks();
        inMemoryTaskManager.getEpics();
        inMemoryTaskManager.getSubtasks();

        //Обновление
        task1.setStatus(Status.NEW);
        inMemoryTaskManager.updateTask(task1);
        System.out.println("Статус task1 после обновление " + task1.getStatus());
        task2.setStatus(Status.NEW);
        inMemoryTaskManager.updateTask(task2);
        System.out.println("Статус task2 после обновление " + task2.getStatus());

        subtask11.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubtask(subtask11);
        System.out.println("Статус subtask11 после обновление " + subtask11.getStatus());
        subtask12.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubtask(subtask12);
        System.out.println("Статус subtask12 после обновление " + subtask12.getStatus());
        inMemoryTaskManager.updateEpic(epic1);
        System.out.println("Статус epic1 после обновление " + epic1.getStatus() + ". Если DONE - значит все верно.");

        subtask21.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubtask(subtask21);
        System.out.println("Статус subtask21 после обновление " + subtask12.getStatus());
        inMemoryTaskManager.updateEpic(epic1);
        System.out.println("Статус epic2 после обновление " + epic2.getStatus() + ". Если DONE - значит все верно.");

        subtask11.setStatus(Status.NEW);
        inMemoryTaskManager.updateSubtask(subtask11);
        System.out.println("Статус epic1 после обновление " + epic1.getStatus() + ". Если IN_PROGRESS - значит все верно.");

        //Удаление
        inMemoryTaskManager.deleteTaskById(task1.getId());
        inMemoryTaskManager.deleteEpicById(epic1.getId());
    }
}
