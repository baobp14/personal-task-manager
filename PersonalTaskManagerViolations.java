import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class PersonalTaskManagerViolations {

    private static final String DB_FILE_PATH = "tasks_database.json";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Phuong thuc tro giup de tai du lieu (se duoc goi lap lai)
    private static JSONArray loadTasksFromDb() {
        JSONParser parser = new JSONParser();
        try (FileReader reader = new FileReader(DB_FILE_PATH)) {
            Object obj = parser.parse(reader);
            if (obj instanceof JSONArray) {
                return (JSONArray) obj;
            }
        } catch (IOException | ParseException e) {
            System.err.println("Loi khi doc file database: " + e.getMessage());
        }
        return new JSONArray();
    }

    // Phuong thuc tro giup de luu du lieu
    private static void saveTasksToDb(JSONArray tasksData) {
        try (FileWriter file = new FileWriter(DB_FILE_PATH)) {
            file.write(tasksData.toJSONString());
            file.flush();
        } catch (IOException e) {
            System.err.println("Loi khi ghi vao file database: " + e.getMessage());
        }
    }

    /**
     * Chuc nang them nhiem vu moi
     *
     * @param title Tieu de nhiem vu.
     * @param description Mo ta nhiem vu.
     * @param dueDateStr Ngay den han (dinh dang YYYY-MM-DD).
     * @param priorityLevel Muc do uu tien ("Thap", "Trung binh", "Cao").
     * @param isRecurring Boolean co phai la nhiem vu lap lai khong.
     * @return JSONObject cua nhiem vu da them, hoac null neu co loi.
     */
    public JSONObject addNewTaskWithViolations(String title, String description,
                                                String dueDateStr, String priorityLevel,
                                                boolean isRecurring) {

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
            System.out.println("Loi: Ngay den han khong hop le. Vui long su dung dinh dang YYYY-MM-DD.");
            return null;
        }
        String[] validPriorities = {"Thap", "Trung binh", "Cao"};
        boolean isValidPriority = false;
        for (String validP : validPriorities) {
            if (validP.equals(priorityLevel)) {
                isValidPriority = true;
                break;
            }
        }
        if (!isValidPriority) {
            System.out.println("Loi: Muc do uu tien khong hop le. Vui long chon tu: Thap, Trung binh, Cao.");
            return null;
        }

        // Tai du lieu
        JSONArray tasks = loadTasksFromDb();

        // Kiem tra trung lap
        for (Object obj : tasks) {
            JSONObject existingTask = (JSONObject) obj;
            if (existingTask.get("title").toString().equalsIgnoreCase(title) &&
                existingTask.get("due_date").toString().equals(dueDate.format(DATE_FORMATTER))) {
                System.out.println(String.format("Loi: Nhiem vu '%s' da ton tai voi cung ngay den han.", title));
                return null;
            }
        }

        String taskId = UUID.randomUUID().toString(); // YAGNI: Co the dung so nguyen tang dan don gian hon.

        JSONObject newTask = new JSONObject();
        newTask.put("id", taskId);
        newTask.put("title", title);
        newTask.put("description", description);
        newTask.put("due_date", dueDate.format(DATE_FORMATTER));
        newTask.put("priority", priorityLevel);
        newTask.put("status", "Chua hoan thanh");
        newTask.put("created_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        newTask.put("last_updated_at", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
        newTask.put("is_recurring", isRecurring); // YAGNI: Them thuoc tinh nay du chua co chuc nang xu ly nhiem vu lap lai
        if (isRecurring) {
            newTask.put("recurrence_pattern", "Chua xac dinh");
        }

        tasks.add(newTask);

        // Luu du lieu
        saveTasksToDb(tasks);

        System.out.println(String.format("Da them nhiem vu moi thanh cong voi ID: %s", taskId));
        return newTask;
    }

    public static void main(String[] args) {
        PersonalTaskManagerViolations manager = new PersonalTaskManagerViolations();
        System.out.println("\nThem nhiem vu hop le:");
        manager.addNewTaskWithViolations(
            "Mua sach",
            "Sach Cong nghe phan mem.",
            "2025-07-20",
            "Cao",
            false
        );

        System.out.println("\nThem nhiem vu trung lap (minh hoa DRY - lap lai code doc/ghi DB va kiem tra trung):");
        manager.addNewTaskWithViolations(
            "Mua sach",
            "Sach Cong nghe phan mem.",
            "2025-07-20",
            "Cao",
            false
        );

        System.out.println("\nThem nhiem vu lap lai (minh hoa YAGNI - them tinh nang khong can thiet ngay):");
        manager.addNewTaskWithViolations(
            "Tap the duc",
            "Tap gym 1 tieng.",
            "2025-07-21",
            "Trung binh",
            true 
        );

        System.out.println("\nThem nhiem vu voi tieu de rong:");
        manager.addNewTaskWithViolations(
            "",
            "Nhiem vu khong co tieu de.",
            "2025-07-22",
            "Thap",
            false
        );
    }
}
