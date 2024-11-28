import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.sql.*;
import java.awt.Desktop;
import java.util.Map;
import java.io.*;

public class SmartStudyPortal {
    static Scanner sc = new Scanner(System.in);
    static Connection con = null;
    int blank_ans = 0;
    static PreparedStatement pst = null;
    static String lastLoginUserId; // It will store the userid of the student who last logined
    static String userIdForFaculty = "f@1234"; // faculty's default userid
    static String passwordForFaculty = "Faculty@1234"; // faculty's default Password
    int num = 1; // it will be use for Exam number(like Exam-1,Exam-2,.......)

    public static void main(String[] args) throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        String dbURL = "jdbc:mysql://localhost:3306/lju";
        String dbUser = "root";
        String dbPass = "";
        con = DriverManager.getConnection(dbURL, dbUser, dbPass);
        SmartStudyPortal ssp = new SmartStudyPortal();
        ssp.choice();
    }

    // ****************************************************************************************
    void choice() throws Exception {
        int choice;
        do {
            System.out.println("""
                    ---------------------- Enter your choice ----------------------
                    [1] Login
                    [2] Registration
                    [3] Forgot Password
                    [4] Exit""");
            System.out.print("=> Choice = ");
            choice = sc.nextInt();
            switch (choice) {
                case 1 -> login();
                case 2 -> registration();
                case 3 -> forgotPassword();
                case 4 -> System.exit(0);
                default -> System.out.println("Enter valid choice");
            }
        } while (choice != 4);

    }

    // ****************************************************************************************
    void login() throws Exception {
        System.out.println("------------------------ Login Options ------------------------");
        System.out.println("login as :\n(1) Student\n(2) faculty");
        System.out.print("=> Choice = ");
        String login_choice = sc.next();
        if (login_choice.equalsIgnoreCase("1") || login_choice.equalsIgnoreCase("student")) {
            studentLogin();
        } else if (login_choice.equalsIgnoreCase("2") || login_choice.equalsIgnoreCase("faculty")) {
            facultyLogin();
        } else {
            System.out.println("# Enter valid choice #");
            login();
        }
    }

    // ****************************************************************************************
    void studentLogin() throws Exception {
        System.out.println("------------------------ Login Details ------------------------");
        System.out.print("Enter Your Userid = ");
        String userId = sc.next();
        String sql = "Select count(*) from Studentdata where userid = ? ";
        pst = con.prepareStatement(sql);
        pst.setString(1, userId);
        ResultSet rs = pst.executeQuery();
        rs.next();
        if (rs.getInt(1) == 1) {
            System.out.print("Enter Your Password = ");
            String password = sc.next();
            String sql1 = "Select password from Studentdata where userid = ? ";
            pst = con.prepareStatement(sql1);
            pst.setString(1, userId);
            ResultSet rs1 = pst.executeQuery();
            rs1.next();
            if (rs1.getString(1).equals(password)) {
                lastLoginUserId = userId;
                System.out.println("# login successful");
                afterLoginForStudent();
            } else {
                System.out.println("# You entered wrong Password");
                choice();
            }
        } else {
            System.out.println("# You entered wrong UserId");
            choice();
        }
    }
    // ****************************************************************************************

    void facultyLogin() throws Exception {
        System.out.println("------------------------ Login Details ------------------------");
        System.out.print("Enter userId = ");
        String userId = sc.next();
        if (userId.equals(userIdForFaculty)) {
            System.out.print("Enter password = ");
            String password = sc.next();
            if (password.equals(passwordForFaculty)) {
                System.out.println("# login successful");
                afterLoginForFaculty();
            } else {
                System.out.println("# wrong password");
                choice();
            }
        } else {
            System.out.println("# wrong userid");
            choice();
        }
    }
    // ****************************************************************************************

    void registration() throws Exception {
        System.out.println("------------------------- Registration ------------------------");
        System.out.print("Enter Your name = ");
        String registrationName = sc.next();
        String registrationUserId = cheakUserId();
        String registrationPass = cheakPassword();
        String sql = "insert into studentdata (userid,name,password) values (?,?,?)";
        pst = con.prepareStatement(sql);
        pst.setString(1, registrationUserId);
        pst.setString(2, registrationName);
        pst.setString(3, registrationPass);
        int res = pst.executeUpdate();
        if (res == 1) {
            System.out.println("-> Successfully Registered");
        } else {
            System.out.println("-> failed ");
        }
    }
    // ****************************************************************************************

    String cheakUserId() throws Exception {
        System.out.print("Enter Your Userid = ");
        String registrationUserId = sc.next();
        String sql = "Select count(*) from Studentdata where userid = ? ";
        pst = con.prepareStatement(sql);
        pst.setString(1, registrationUserId);
        ResultSet rs = pst.executeQuery();
        rs.next();
        if (rs.getInt(1) == 0) {
            return registrationUserId;
        } else {
            System.out.println("UserId already Registered");
            cheakUserId();
            return "";
        }
    }
    // ****************************************************************************************

    String cheakPassword() {
        Pattern specailCharPatten = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Pattern UpperCasePatten = Pattern.compile("[A-Z ]");
        Pattern lowerCasePatten = Pattern.compile("[a-z ]");
        Pattern digitCasePatten = Pattern.compile("[0-9 ]");
        String password = null;
        for (; true;) {
            System.out.println("""
                    Password Guide Line
                    1)Password must be 8 or more charcters
                    2)Password must contains special character
                    3)Password must contains lowercase
                    4)Password must contains Uppercase
                    5)Password must contains digit
                    """);
            System.out.print("Please enter new password : ");
            password = sc.next();
            if (password.length() >= 8) {
                if (specailCharPatten.matcher(password).find()) {
                    if (UpperCasePatten.matcher(password).find()) {
                        if (lowerCasePatten.matcher(password).find()) {
                            if (digitCasePatten.matcher(password).find()) {
                                break;
                            }
                        }
                    }
                }
            }
        }
        return password;
    }

    // *********************************************************************************
    void forgotPassword() throws Exception {
        System.out.println("----------------------- Forgot Password -----------------------");
        System.out.print("Enter Your Userid = ");
        String userId = sc.next();
        String sql = "Select count(*) from Studentdata where userid = ? ";
        pst = con.prepareStatement(sql);
        pst.setString(1, userId);
        String newPassword = null;
        ResultSet rs = pst.executeQuery();
        rs.next();
        if (rs.getInt(1) == 1) {
            newPassword = cheakPassword();
            for (; true;) {
                Random r = new Random();
                int i = r.nextInt(9) + 1;
                int j = r.nextInt(6);
                int k = r.nextInt(3);
                String captcha = "B" + i + "EF" + j + "IU" + k;
                System.out.println("Captcha : " + captcha);
                System.out.print("Ans = ");
                String ansOfCapcha = sc.next();
                if (captcha.equals(ansOfCapcha)) {
                    String forgotsql = "UPDATE studentdata SET password = ? WHERE userid = ?";
                    pst = con.prepareStatement(forgotsql);
                    pst.setString(1, newPassword);
                    pst.setString(2, userId);
                    pst.executeUpdate();
                    System.out.println("# Password successfully changed");
                    break;
                }
            }
        } else {
            System.out.println("# Enter valid id");
        }
    }
    // ****************************************************************************************

    void afterLoginForStudent() throws Exception {
        int choice;
        do {
            System.out.println("""
                    ---------------------- Select your Pefrence ---------------------
                    (1) Want to read study materials
                    (2) Genrate a practice exam
                    (3) Attend exam genrated by faculty
                    (4) Ask doubts to faculty
                    (5) Answers of your previous doubts
                    (6) Logout""");
            System.out.print("choice = ");
            choice = sc.nextInt();
            switch (choice) {
                case 1 -> readStudyMaterial();
                case 2 -> genratePracticeExam();
                case 3 -> attendExam();
                case 4 -> studentAskDoubt();
                case 5 -> studentDoubtSolution();
                case 6 -> choice();
                default -> System.out.println("# Please Enter valid choice .");
            }
        } while (choice != 6);
    }
    // ****************************************************************************************

    void readStudyMaterial() throws Exception {
        System.out.println("------------------------- Study Material ------------------------");
        File myFile = new File(
                "D:\\Smart study portal\\Study material");
        String files[] = myFile.list();
        int i = 1;
        for (String s : files) {
            System.out.println("(" + i + ") " + s);
            i++;
        }
        System.out.print("Choice = ");
        int choice = sc.nextInt();
        String choice_sub = files[choice - 1];
        if (Desktop.isDesktopSupported()) {
            File f = new File(
                    "D:\\Smart study portal\\Study material/" + choice_sub);
            Desktop.getDesktop().open(f);
        }
        afterLoginForStudent();
    }
    // ****************************************************************************************

    void genratePracticeExam() throws Exception {
        String sub[] = { "JAVA", "DBMS" };
        System.out.println("""
                ----------------------------------------------
                Select subject:
                (1) JAVA
                (2) DBMS """);
        System.out.print("choice = ");
        int choice = sc.nextInt();
        System.out.print("Enter marks = ");
        int mark = sc.nextInt();
        String Que_file_path = "D:\\Smart study portal\\Practice Exam/" + sub[choice - 1]
                + "_QUESTIONS.txt";
        String Ans_file_path = "D:\\Smart study portal\\Practice Exam/" + sub[choice - 1]
                + "_Ans.txt";
        LinkedHashMap<String, String> practiceExam = new LinkedHashMap<>();
        String seprate_containt_que[] = createDataArray(Que_file_path);
        String seprate_containt_ans[] = createDataArray(Ans_file_path);
        for (int i = 0; i < seprate_containt_que.length; i++) {
            practiceExam.put(seprate_containt_que[i].trim(), seprate_containt_ans[i].trim());
        }
        ArrayList<String> al = new ArrayList<>();
        Iterator itr = practiceExam.entrySet().iterator();
        while (itr.hasNext()) {
            Map.Entry e = (Map.Entry) itr.next();
            al.add((String) e.getKey());
        }
        Collections.shuffle(al);
        LinkedHashMap<String, String> ans_practiceExam = new LinkedHashMap<>();
        Iterator itr1 = al.iterator();
        int count = 0;
        int i = 1;
        while (itr1.hasNext()) {
            if (count == mark) {
                break;
            }
            String question = (String) itr1.next();
            System.out.printf("(%d) %s\n", i, question);
            System.out.print("Ans = ");
            String ans = sc.next();
            System.out.println();
            ans_practiceExam.put(question, ans);
            count++;
            i++;
        }
        genrateResult(practiceExam, ans_practiceExam, "PracticeExam");
    }

    // ****************************************************************************************

    void attendExam() throws Exception {
        String sql_cheak = "select count(*) from exam";
        pst = con.prepareStatement(sql_cheak);
        ResultSet rs = pst.executeQuery();
        rs.next();
        if (rs.getInt(1) == 0) {
            System.out.println("No exam craeted");
        } else {
            String sqlcheakIfDone = "select count(*) from lastExam_marks where user_id = ?";
            pst = con.prepareStatement(sqlcheakIfDone);
            pst.setString(1, lastLoginUserId);
            ResultSet rst = pst.executeQuery();
            rst.next();
            if (rst.getInt(1) == 1) {
                System.out.println("You have already completed this exam .");
            } else {
                String get_examPaper = "Select * from exam";
                pst = con.prepareStatement(get_examPaper);
                ResultSet examPaper = pst.executeQuery();
                LinkedHashMap<String, String> Exam = new LinkedHashMap<>();
                LinkedHashMap<String, String> Ans = new LinkedHashMap<>();
                int marks = 0;
                sc.nextLine();
                while (examPaper.next()) {
                    int index = examPaper.getInt(1);
                    String que = examPaper.getString(2);
                    String ans = examPaper.getString(3);
                    System.out.printf("(%d) %s\n", index, que);
                    System.out.print("Ans = ");
                    String ansFromStudent = sc.nextLine();
                    if (ansFromStudent.isEmpty()) {
                        blank_ans++;
                    }
                    System.out.println();
                    if (ansFromStudent.equalsIgnoreCase(ans)) {
                        marks++;
                    }
                    Exam.put(que, ans);
                    Ans.put(que, ansFromStudent);
                }
                String find_name = "Select name from studentdata where userid = ?";
                pst = con.prepareStatement(find_name);
                pst.setString(1, lastLoginUserId);
                ResultSet rstForname = pst.executeQuery();
                rstForname.next();
                String name = rstForname.getString(1);
                String insert_mark = "insert into lastExam_marks values(?,?,?)";
                pst = con.prepareStatement(insert_mark);
                pst.setString(1, lastLoginUserId);
                pst.setString(2, name);
                pst.setInt(3, marks);
                pst.executeUpdate();
                genrateResult(Exam, Ans, "Exam");
            }
        }
    }

    // ****************************************************************************************

    void studentAskDoubt() throws Exception {
        System.out.println("------------------------- Doubt Submition ----------------------- ");
        System.out.print("Enter your doubt : ");
        sc.nextLine();
        String doubt = sc.nextLine();
        String sql_fatchName = "Select name from studentdata where userid = ?";
        pst = con.prepareStatement(sql_fatchName);
        pst.setString(1, lastLoginUserId);
        ResultSet rst = pst.executeQuery();
        rst.next();
        String name = rst.getString(1);
        String sql = "insert into doubt (userid,name,question) values (?,?,?)";
        pst = con.prepareStatement(sql);
        pst.setString(1, lastLoginUserId);
        pst.setString(2, name);
        pst.setString(3, doubt);
        int res = pst.executeUpdate();
        if (res == 1) {
            System.out.println("-> doubt Successfully submited");
        } else {
            System.out.println("-> *failed ");
        }
    }
    // ****************************************************************************************

    void studentDoubtSolution() throws Exception {
        System.out.println("------------------------ Doubt's Solution ----------------------- ");
        String sql_fatchName = "Select question,ans from doubt_solution where userid = ?";
        pst = con.prepareStatement(sql_fatchName);
        pst.setString(1, lastLoginUserId);
        ResultSet rst = pst.executeQuery();
        int i = 1;
        if (rst.next() == false) {
            System.out.println("-> You have not asked any doubts or else your doubts have not been resolved");
        } else {
            do {
                System.out.println("(" + i + ")question = " + rst.getString(1));
                System.out.println("   Ans = " + rst.getString(2));
                System.out.println();
                i++;
            } while (rst.next());
            String sql_deletedata = "delete from doubt_solution where userid = ?";
            pst = con.prepareStatement(sql_deletedata);
            pst.setString(1, lastLoginUserId);
            pst.executeUpdate();
        }
    }
    // ****************************************************************************************

    void afterLoginForFaculty() throws Exception {
        int choice;
        do {
            System.out.println("""
                    -----------------------------------------------------------------
                    Select prefrence :
                    [1] Genrate Exam
                    [2] attach study material
                    [3] give answers of question
                    [4] want to see result of last exam
                    [5] Logout""");
            System.out.print("choice = ");
            choice = sc.nextInt();
            switch (choice) {
                case 1 -> genrateExam();
                case 2 -> attchStudyMaterial();
                case 3 -> doubtSolve();
                case 4 -> showResult();
                case 5 -> choice();
            }
        } while (choice != 5);

    }
    // ****************************************************************************************

    void genrateExam() throws Exception {
        System.out.println("Enter path of Question file = ");
        sc.nextLine();
        String Que_file_path = sc.nextLine();
        System.out.println("-> " + Que_file_path);
        System.out.println("Enter path of Answer file = ");
        String Ans_file_path = sc.nextLine();
        System.out.println("-> " + Ans_file_path);
        String seprate_containt_que[] = createDataArray(Que_file_path);
        String seprate_containt_ans[] = createDataArray(Ans_file_path);
        String sql_delete = "delete from exam";
        pst = con.prepareStatement(sql_delete);
        pst.executeUpdate();
        String sql_deletedata = "delete from lastexam_marks";
        pst = con.prepareStatement(sql_deletedata);
        pst.executeUpdate();
        for (int i = 0; i < seprate_containt_que.length; i++) {
            String sql = "insert into exam values(?,?,?)";
            pst = con.prepareStatement(sql);
            pst.setInt(1, (i + 1));
            pst.setString(2, seprate_containt_que[i].trim());
            pst.setString(3, seprate_containt_ans[i].trim());
            pst.executeUpdate();
        }
    }

    String[] createDataArray(String path) throws Exception {
        File f = new File(path);
        FileInputStream fis = new FileInputStream(f);
        byte b[] = new byte[(int) f.length()];
        fis.read(b);
        String file_containt = new String(b);
        String seprate_containt[] = file_containt.split("\\n\\s*\\n");
        return seprate_containt;
    }

    // ****************************************************************************************

    void attchStudyMaterial() throws Exception {
        System.out.println("Enter path of file");
        String path = sc.next();
        int num1 = path.lastIndexOf("/");
        int num2 = path.lastIndexOf(".");
        String filename = "";
        for (int i = num1 + 1; i < num2; i++) {
            filename = filename + path.charAt(i);
        }
        System.out.println("file name = " + filename);
        File f = new File(path);
        FileInputStream fis = new FileInputStream(f);
        byte b[] = new byte[(int) f.length()];
        fis.read(b);
        FileOutputStream fos = new FileOutputStream(
                "D:\\Smart study portal\\Study material/" + filename + ".pdf");
        fos.write(b);
        fis.close();
        fos.close();
    }
    // ****************************************************************************************

    void doubtSolve() throws Exception {
        System.out.println("---------------------------- Doubt's  --------------------------- ");
        String sql_count = "Select count(*) from doubt ";
        pst = con.prepareStatement(sql_count);
        ResultSet rst1 = pst.executeQuery();
        rst1.next();
        if (rst1.getInt(1) == 0) {
            System.out.println("no doubts left");
        } else {
            String sql_doubtSolveList = "Select name,userid,question from doubt ";
            pst = con.prepareStatement(sql_doubtSolveList);
            ResultSet rst = pst.executeQuery();
            int num = 1;
            System.out.println("doubts: ");
            while (rst.next()) {
                System.out.println("(" + num + ") name = " + rst.getString(1));
                System.out.println("    userid = " + rst.getString(2));
                System.out.println("    Question = " + rst.getString(3));
                num++;
                sc.nextLine();
                System.out.print("Ans = ");
                String ans = sc.nextLine();
                String sql = "insert into doubt_solution (userid,question,ans) values (?,?,?)";
                pst = con.prepareStatement(sql);
                pst.setString(1, rst.getString(2));
                pst.setString(2, rst.getString(3));
                pst.setString(3, ans);
                int res = pst.executeUpdate();
                if (res == 1) {
                    String sql_deleteDoubt = "DELETE FROM doubt WHERE question = ? ";
                    pst = con.prepareStatement(sql_deleteDoubt);
                    pst.setString(1, rst.getString(3));
                    pst.executeUpdate();
                    System.out.println("-> solution Successfully sent");
                } else {
                    System.out.println("-> #failed ");
                }
                System.out.print("""
                        [1] continue
                        [2] Exit
                        choice = """);
                int choiceForExit = sc.nextInt();
                if (choiceForExit == 2) {
                    break;
                }
            }
        }
    }
    // ****************************************************************************************

    void genrateResult(LinkedHashMap<String, String> Que, LinkedHashMap<String, String> Ans, String type)
            throws IOException {
        String path = null;
        if (type.equals("PracticeExam")) {
            path = "C:/Users/LENOVO/Downloads//marksheet.txt";
        } else if (type.equals("Exam")) {
            path = "D:\\Smart study portal\\Exam Result/Exam-" + num + ".txt";
            num++;
        }
        File f = new File(path);
        PrintWriter pw = new PrintWriter(f);
        String format = """
                => Result :-

                Question   |   Your   |  Correct   |  R/W
                 number    |    Ans   |    Ans     |     """;
        pw.println(format);
        pw.println("-----------+----------+------------+--------");
        Iterator itr = Ans.entrySet().iterator();
        int mark = 0;
        int i = 1;
        String space;
        while (itr.hasNext()) {
            Map.Entry e = (Map.Entry) itr.next();
            String question = (String) e.getKey();
            String defaultAns = Que.get(question);
            String ansByStudent = Ans.get(question);
            if ((ansByStudent).equalsIgnoreCase(defaultAns)) {
                pw.printf("  %-8s |    %-5s |     %-6s | %-7s", "Que-" + i, ansByStudent, defaultAns, "Right");
                mark++;
            } else {
                pw.printf("  %-8s |    %-5s |     %-6s | %-7s", "Que-" + i, ansByStudent, defaultAns, "Wrong");

            }
            pw.println();
            i++;
        }
        pw.println();
        pw.println("Total matks = " + Ans.size());
        pw.println("Obtained marks = " + mark);
        pw.println("blank questions : " + blank_ans);
        pw.close();
        Desktop.getDesktop().open(f);
    }
    // ****************************************************************************************

    void showResult() throws Exception {

        System.out.println("-------------------------------");
        File f = new File("D://Smart study portal//src/result.txt");
        PrintWriter pw = new PrintWriter(f);
        int totalmarks = 0;
        int noOfStudent = 0;
        String format = """
                => Result:-

                      Userid    |      name      |    marks """;
        pw.println(format);
        pw.println("----------------+----------------+-------------");
        String get_marks = "Select * from lastexam_marks";
        pst = con.prepareStatement(get_marks);
        ResultSet rst = pst.executeQuery();
        while (rst.next()) {
            String userId = rst.getString(1);
            String name = rst.getString(2);
            int marks = rst.getInt(3);
            pw.printf("   %-11s  |      %-8s  |      %-4d", userId, name, marks);
            pw.println();
            totalmarks = totalmarks + marks;
            noOfStudent++;
        }
        pw.println();
        double avgmarks = totalmarks / noOfStudent;
        pw.println("avg marks of students = " + avgmarks);
        pw.close();
        Desktop.getDesktop().open(f);
    }
}