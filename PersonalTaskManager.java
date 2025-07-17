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
            System.err.println("Lỗi đọc file: " + e.getMessage());
        }
        return new JSONArray();
    }

    private void saveTasksToDb(JSONArray tasksData) {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(DB_FILE_PATH), StandardCharsets.UTF_8))) {
            writer.write(tasksData.toJSONString());
        } catch (IOException e) {
            System.err.println("Lỗi ghi file: " + e.getMessage());
        }
    }

    private boolean isValidPriority(String priority) {
        return priority.equals("Thấp") || priority.equals("Trung bình") || priority.equals("Cao");
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
            System.out.println("Lỗi: Tiêu đề không được để trống.");
            return null;
        }

        if (dueDateStr == null || dueDateStr.trim().isEmpty()) {
            System.out.println("Lỗi: Ngày đến hạn không được để trống.");
            return null;
        }

        LocalDate dueDate;
        try {
            dueDate = LocalDate.parse(dueDateStr, DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            System.out.println("Lỗi: Ngày đến hạn không hợp lệ (YYYY-MM-DD).");
            return null;
        }

        if (!isValidPriority(priority)) {
            System.out.println("Lỗi: Mức độ ưu tiên không hợp lệ (Thấp, Trung bình, Cao).");
            return null;
        }

        JSONArray tasks = loadTasksFromDb();
        if (isDuplicateTask(tasks, title, dueDate.format(DATE_FORMATTER))) {
            System.out.println("Lỗi: Nhiệm vụ đã tồn tại.");
            return null;
        }

        JSONObject newTask = new JSONObject();
        newTask.put("id", UUID.randomUUID().toString());
        newTask.put("title", title);
        newTask.put("description", description);
        newTask.put("due_date", dueDate.format(DATE_FORMATTER));
        newTask.put("priority", priority);
        newTask.put("status", "Chưa hoàn thành");

        tasks.add(newTask);
        saveTasksToDb(tasks);

        System.out.println("Đã thêm nhiệm vụ thành công.");
        return newTask;
    }

    public void viewTasks() {
        JSONArray tasks = loadTasksFromDb();
        if (tasks.isEmpty()) {
            System.out.println("Danh sách nhiệm vụ trống.");
            return;
        }
        System.out.println("Danh sách các nhiệm vụ:");
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
                        System.out.println("Lỗi: Ngày đến hạn không hợp lệ.");
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
            System.out.println("Cập nhật nhiệm vụ thành công.");
        } else {
            System.out.println("Không tìm thấy nhiệm vụ với ID đã nhập.");
        }
    }

    public void deleteTask(String taskId) {
        JSONArray tasks = loadTasksFromDb();
        boolean removed = tasks.removeIf(obj -> ((JSONObject) obj).get("id").toString().equals(taskId));
        if (removed) {
            saveTasksToDb(tasks);
            System.out.println("Xóa nhiệm vụ thành công.");
        } else {
            System.out.println("Không tìm thấy nhiệm vụ để xóa.");
        }
    }

    public static void main(String[] args) {
        PersonalTaskManager manager = new PersonalTaskManager();
        Scanner scanner = new Scanner(System.in, "UTF-8");

        while (true) {
            System.out.println("\n----- QUẢN LÝ NHIỆM VỤ -----");
            System.out.println("1. Thêm nhiệm vụ");
            System.out.println("2. Xem danh sách nhiệm vụ");
            System.out.println("3. Chỉnh sửa nhiệm vụ");
            System.out.println("4. Xóa nhiệm vụ");
            System.out.println("5. Thoát");
            System.out.print("Chọn chức năng: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    System.out.print("Tiêu đề: ");
                    String title = scanner.nextLine();
                    System.out.print("Mô tả: ");
                    String desc = scanner.nextLine();
                    System.out.print("Ngày đến hạn (YYYY-MM-DD): ");
                    String date = scanner.nextLine();
                    System.out.print("Mức độ ưu tiên (Thấp/Trung bình/Cao): ");
                    String priority = scanner.nextLine();
                    manager.addNewTask(title, desc, date, priority);
                    break;
                case "2":
                    manager.viewTasks();
                    break;
                case "3":
                    System.out.print("Nhập ID nhiệm vụ cần sửa: ");
                    String editId = scanner.nextLine();
                    System.out.print("Tiêu đề mới (bỏ trống nếu không đổi): ");
                    String newTitle = scanner.nextLine();
                    System.out.print("Mô tả mới (bỏ trống nếu không đổi): ");
                    String newDesc = scanner.nextLine();
                    System.out.print("Ngày đến hạn mới (YYYY-MM-DD hoặc bỏ trống): ");
                    String newDate = scanner.nextLine();
                    System.out.print("Ưu tiên mới (Thấp/Trung bình/Cao hoặc bỏ trống): ");
                    String newPriority = scanner.nextLine();
                    manager.editTask(editId, newTitle, newDesc, newDate, newPriority);
                    break;
                case "4":
                    System.out.print("Nhập ID nhiệm vụ cần xóa: ");
                    String deleteId = scanner.nextLine();
                    manager.deleteTask(deleteId);
                    break;
                case "5":
                    System.out.println("Thoát chương trình.");
                    return;
                default:
                    System.out.println("Lựa chọn không hợp lệ.");
            }
        }
    }
}
