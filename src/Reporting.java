import java.sql.*;
import java.util.Scanner;

public class Reporting {

    private static final String DB_URL = "jdbc:oracle:thin:@oracle.wpi.edu:1521:orcl";

    public static void main(String[] args) {
        if (args.length < 2) {
            printMenu();
            return;
        }

        String username = args[0];
        String password = args[1];

        try (Connection conn = DriverManager.getConnection(DB_URL, username, password)) {
            Scanner scanner = new Scanner(System.in);

            if (args.length == 2) {
                printMenu();
                return;
            }

            String mode = args[2];

            switch (mode) {
                case "1":
                    reportPatient(scanner, conn);
                    break;
                case "2":
                    reportDoctor(scanner, conn);
                    break;
                case "3":
                    reportAdmission(scanner, conn);
                    break;
                case "4":
                    updateAdmissionPayment(scanner, conn);
                    break;
                default:
                    printMenu();
                    break;
            }

        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printMenu() {
        System.out.println("1- Report Patients Basic Information");
        System.out.println("2- Report Doctors Basic Information");
        System.out.println("3- Report Admissions Information");
        System.out.println("4- Update Admissions Payment");
    }

    private static void reportPatient(Scanner scanner, Connection conn) throws SQLException {
        System.out.print("Enter Patient SSN: ");
        String ssn = scanner.nextLine().trim();

        String sql = "SELECT SSN, FirstName, LastName, Address FROM Patient WHERE SSN = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ssn);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Patient SSN: " + rs.getString("SSN"));
                    System.out.println("Patient First Name: " + rs.getString("FirstName"));
                    System.out.println("Patient Last Name: " + rs.getString("LastName"));
                    System.out.println("Patient Address: " + rs.getString("Address"));
                } else {
                    System.out.println("No patient found with SSN: " + ssn);
                }
            }
        }
    }

    private static void reportDoctor(Scanner scanner, Connection conn) throws SQLException {
        System.out.print("Enter Doctor ID: ");
        String id = scanner.nextLine().trim();

        String sql = """
            SELECT d.EmployeeID, e.FName, e.LName, d.gender, d.GraduatedFrom, d.specialty
            FROM Doctor d
            JOIN Employee e ON d.EmployeeID = e.ID
            WHERE d.EmployeeID = ?
        """;
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("Doctor ID: " + rs.getInt("EmployeeID"));
                    System.out.println("Doctor First Name: " + rs.getString("FName"));
                    System.out.println("Doctor Last Name: " + rs.getString("LName"));
                    System.out.println("Doctor Gender: " + rs.getString("gender"));
                    System.out.println("Doctor Graduated From: " + rs.getString("GraduatedFrom"));
                    System.out.println("Doctor Specialty: " + rs.getString("specialty"));
                } else {
                    System.out.println("No doctor found with ID: " + id);
                }
            }
        }
    }

    private static void reportAdmission(Scanner scanner, Connection conn) throws SQLException {
        System.out.print("Enter Admission Number: ");
        int admissionNum = scanner.nextInt();
        scanner.nextLine();

        String admSql = "SELECT Num, Patient_SSN, AdmissionDate, TotalPayment FROM Admission WHERE Num = ?";
        try (PreparedStatement admPstmt = conn.prepareStatement(admSql)) {
            admPstmt.setInt(1, admissionNum);
            try (ResultSet rs = admPstmt.executeQuery()) {
                if (!rs.next()) {
                    System.out.println("No admission found with number: " + admissionNum);
                    return;
                }
                System.out.println("Admission Number: " + rs.getInt("Num"));
                System.out.println("Patient SSN: " + rs.getString("Patient_SSN"));
                System.out.println("Admission date (start date): " + rs.getDate("AdmissionDate"));
                System.out.println("Total Payment: " + rs.getDouble("TotalPayment"));
            }
        }

        System.out.println("Rooms:");
        String roomSql = """
            SELECT RoomNum, startDate AS FromDate, endDate AS ToDate
            FROM StayIn
            WHERE AdmissionNum = ?
            ORDER BY startDate
        """;
        try (PreparedStatement roomPstmt = conn.prepareStatement(roomSql)) {
            roomPstmt.setInt(1, admissionNum);
            try (ResultSet rs = roomPstmt.executeQuery()) {
                while (rs.next()) {
                    System.out.printf("   RoomNum: %d   FromDate: %s   ToDate: %s%n",
                            rs.getInt("RoomNum"),
                            rs.getDate("FromDate"),
                            rs.getDate("ToDate"));
                }
            }
        }

        System.out.println("Doctors examined the patient in this admission:");
        String docSql = """
            SELECT DISTINCT DoctorID
            FROM Examine
            WHERE AdmissionNum = ?
            ORDER BY DoctorID
        """;
        try (PreparedStatement docPstmt = conn.prepareStatement(docSql)) {
            docPstmt.setInt(1, admissionNum);
            try (ResultSet rs = docPstmt.executeQuery()) {
                boolean hasDoctors = false;
                while (rs.next()) {
                    System.out.println("   Doctor ID: " + rs.getInt("DoctorID"));
                    hasDoctors = true;
                }
                if (!hasDoctors) {
                    System.out.println("   (No doctors examined this admission)");
                }
            }
        }
    }

    private static void updateAdmissionPayment(Scanner scanner, Connection conn) throws SQLException {
        System.out.print("Enter Admission Number: ");
        int admissionNum = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Enter the new total payment: ");
        double newPayment = scanner.nextDouble();
        scanner.nextLine();

        String sql = "UPDATE Admission SET TotalPayment = ? WHERE Num = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDouble(1, newPayment);
            pstmt.setInt(2, admissionNum);

            int rows = pstmt.executeUpdate();
            if (rows > 0) {
                System.out.println("Total payment updated successfully for admission " + admissionNum);
            } else {
                System.out.println("No admission found with number: " + admissionNum);
            }
        }
    }
}