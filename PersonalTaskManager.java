import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManager {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private JSONArray loadTasksFromDb() {
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
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
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasksData.toJSONString());
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

    public static void main(String[] args) {
        PersonalTaskManager manager = new PersonalTaskManager();
        manager.addNewTask("Mua sách", "Sách lập trình", "2025-07-20", "Cao");
    }
}
