import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Reporting {
    public static void main(String[] args) throws SQLException {
        Connection connection = null;

        try {
            String url = "jdbc:oracle:thin:@oracle.wpi.edu:1521:orcl";
            String username = "";
            String password = "";

            connection = DriverManager.getConnection(
                    url,
                    username,
                    password
            );
        } catch (SQLException e) {
            System.out.println("Connection Failed! Check output console");
            e.printStackTrace();
            return;
        }

        if (connection != null) {
            System.out.println("You made it. Connection is successful. Take control of your database now!");
        } else {
            System.out.println("Failed to make connection!");
        }

        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}