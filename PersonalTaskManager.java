import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManager {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JSONArray loadTasksFromDb() {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(DB_FILE_PATH), StandardCharsets.UTF_8))) {
            Object obj = new JSONParser().parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            }
        } catch (IOException | ParseException e) {
            System.err.println("Loi doc file: " + e.getMessage());
        }
        return new JSONArray();
    }

    private void saveTasksToDb(JSONArray tasksData) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(DB_FILE_PATH), StandardCharsets.UTF_8))) {
            writer.write(tasksData.toJSONString());
        } catch (IOException e) {
            System.err.println("Loi ghi file: " + e.getMessage());
        }
    }

    private boolean isValidPriority(String priority) {
        return priority.equals("Thap") || priority.equals("Trung binh") || priority.equals("Cao");
    }

    private boolean isDuplicateTask(JSONArray tasks, String title, String dueDate) {
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (task.get("title").toString().equalsIgnoreCase(title) &&
                task.get("due_date").toString().equals(dueDate)) {
                return true;
            }
        }
        return false;
    }

    public JSONObject addNewTask(String title, String description, String dueDateStr, String priority) {
        if (title == null || title.trim().isEmpty()) {
            System.out.println("Loi: Tieu de khong duoc de trong.");
            return null;
        }

        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            System.out.println("Loi: Ngay den han khong duoc de trong.");
            return null;
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Loi: Ngay den han khong hop le (YYYY-MM-DD).");
            return null;
        }

        if (!isValidPriority(priority)) {
            System.out.println("Loi: Muc do uu tien khong hop le (Thap, Trung binh, Cao).");
            return null;
        }

        JSONArray tasks = loadTasksFromDb();
        if (isDuplicateTask(tasks, title, dueDate.format(DATE_FORMATTER))) {
            System.out.println("Loi: Nhiem vu da ton tai.");
            return null;
        }

        JSONObject newTask = new JSONObject();
        newTask.put("id", UUID.randomUUID().toString());
        newTask.put("title", title);
        newTask.put("description", description);
        newTask.put("due_date", dueDate.format(DATE_FORMATTER));
        newTask.put("priority", priority);
        newTask.put("status", "Chua hoan thanh");

        tasks.add(newTask);
        saveTasksToDb(tasks);

        System.out.println("Da them nhiem vu thanh cong.");
        return newTask;
    }

    public void viewTasks() {
        JSONArray tasks = loadTasksFromDb();
        if (tasks.isEmpty()) {
            System.out.println("Danh sach nhiem vu trong.");
            return;
        }
        System.out.println("Danh sach cac nhiem vu:");
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            System.out.println("- " + task.get("title") + " | " + task.get("due_date") +
                               " | " + task.get("priority") + " | " + task.get("status"));
        }
    }

    public void editTask(String taskId, String newTitle, String newDescription, String newDueDateStr, String newPriority) {
        JSONArray tasks = loadTasksFromDb();
        boolean found = false;
        for (Object obj : tasks) {
            JSONObject task = (JSONObject) obj;
            if (task.get("id").toString().equals(taskId)) {
                if (newTitle != null && !newTitle.trim().isEmpty()) {
                    task.put("title", newTitle);
                }
                if (newDescription != null) {
                    task.put("description", newDescription);
                }
                if (newDueDateStr != null && !newDueDateStr.trim().isEmpty()) {
                    try {
                        LocalDate.parse(newDueDateStr, DATE_FORMATTER);
                        task.put("due_date", newDueDateStr);
                    } catch (DateTimeParseException e) {
                        System.out.println("Loi: Ngay den han khong hop le.");
                        return;
                    }
                }
                if (newPriority != null && isValidPriority(newPriority)) {
                    task.put("priority", newPriority);
                }
                found = true;
                break;
            }
        }
        if (found) {
            saveTasksToDb(tasks);
            System.out.println("Cap nhat nhiem vu thanh cong.");
        } else {
            System.out.println("Khong tim thay nhiem vu voi ID da nhap.");
        }
    }

    public void deleteTask(String taskId) {
        JSONArray tasks = loadTasksFromDb();
        boolean removed = tasks.removeIf(obj -> ((JSONObject) obj).get("id").toString().equals(taskId));
        if (removed) {
            saveTasksToDb(tasks);
            System.out.println("Xoa nhiem vu thanh cong.");
        } else {
            System.out.println("Khong tim thay nhiem vu de xoa.");
        }
    }

    public static void main(String[] args) {
        PersonalTaskManager manager = new PersonalTaskManager();
        Scanner scanner = new Scanner(System.in, "UTF-8");

        while (true) {
            System.out.println("\n----- QUAN LY NHIEM VU -----");
            System.out.println("1. Them nhiem vu");
            System.out.println("2. Xem danh sach nhiem vu");
            System.out.println("3. Chinh sua nhiem vu");
            System.out.println("4. Xoa nhiem vu");
            System.out.println("5. Thoat");
            System.out.print("Chon chuc nang: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Tieu de: ");
                    String title = scanner.nextLine();
                    System.out.print("Mo ta: ");
                    String desc = scanner.nextLine();
                    System.out.print("Ngay den han (YYYY-MM-DD): ");
                    String date = scanner.nextLine();
                    System.out.print("Muc do uu tien (Thap/Trung binh/Cao): ");
                    String priority = scanner.nextLine();
                    manager.addNewTask(title, desc, date, priority);
                    break;
                case "2":
                    manager.viewTasks();
                    break;
                case "3":
                    System.out.print("Nhap ID nhiem vu can sua: ");
                    String editId = scanner.nextLine();
                    System.out.print("Tieu de moi (bo trong neu khong doi): ");
                    String newTitle = scanner.nextLine();
                    System.out.print("Mo ta moi (bo trong neu khong doi): ");
                    String newDesc = scanner.nextLine();
                    System.out.print("Ngay den han moi (YYYY-MM-DD hoac bo trong): ");
                    String newDate = scanner.nextLine();
                    System.out.print("Uu tien moi (Thap/Trung binh/Cao hoac bo trong): ");
                    String newPriority = scanner.nextLine();
                    manager.editTask(editId, newTitle, newDesc, newDate, newPriority);
                    break;
                case "4":
                    System.out.print("Nhap ID nhiem vu can xoa: ");
                    String deleteId = scanner.nextLine();
                    manager.deleteTask(deleteId);
                    break;
                case "5":
                    System.out.println("Thoat chuong trinh.");
                    return;
                default:
                    System.out.println("Lua chon khong hop le.");
            }
        }
    }
}
