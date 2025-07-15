public class Main {

    public static void main(String[] args) {

        TaskManager taskManager = new TaskManager();

        //Создание
        Task task1 = new Task("Task 1", "Task1 description");
        Task task2 = new Task("Task 2", "Task2 description");
        taskManager.addNewTask(task1);
        taskManager.addNewTask(task2);

        Epic epic1 = new Epic("Epic 1", "Epic1 description");
        Epic epic2 = new Epic("Epic 2", "Epic2 description");
        taskManager.addNewEpic(epic1);
        taskManager.addNewEpic(epic2);

        Subtask subtask11 = new Subtask(epic1, "Epic1 Subtask1", "Epic1 Subtask1 description");
        Subtask subtask12 = new Subtask(epic1, "Epic1 Subtask2", "Epic1 Subtask2 description");
        Subtask subtask21 = new Subtask(epic2, "Epic2 Subtask1", "Epic2 Subtask1 description");
        taskManager.addNewSubtask(subtask11);
        taskManager.addNewSubtask(subtask12);
        taskManager.addNewSubtask(subtask21);

        taskManager.getAll();

        //Обновление
        task1.setStatus(Status.IN_PROGRESS);
        System.out.println("Статус task1 после обновление " + task1.getStatus());
        task2.setStatus(Status.DONE);
        System.out.println("Статус task2 после обновление " + task2.getStatus());
        
        subtask11.setStatus(Status.DONE);
        System.out.println("Статус subtask11 после обновление " + subtask11.getStatus());
        subtask12.setStatus(Status.DONE);
        System.out.println("Статус subtask12 после обновление " + subtask12.getStatus());
        //epic1.setStatus(Status.NEW); //Почему не работает? Я же переопределил.... ПАМАГИТЕЕЕЕЕ
        System.out.println("Статус epic1 после обновление " + epic1.getStatus() + ". Если DONE - значит все верно.");
            //Не понимаю почему не обновляется статус эпика. epic, который создаю в taskManager.updateEpicStatus
            //ссылается после первого цикла на объект и должен менять его статус, нет?

        subtask12.setStatus(Status.DONE);
        System.out.println("Статус subtask21 после обновление " + subtask12.getStatus());
        //epic2.setStatus(Status.NEW);
        System.out.println("Статус epic2 после обновление " + epic2.getStatus() + ". Если DONE - значит все верно.");

        subtask11.setStatus(Status.NEW);
        System.out.println("Статус epic1 после обновление " + epic1.getStatus() + ". Если IN_PROGRESS - значит все верно.");

        //Удаление
        taskManager.deleteById(task1.getTaskId());
        taskManager.deleteById(epic1.getEpicId());
    }
}
