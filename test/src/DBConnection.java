import java.sql.*;

public class DBConnection {
    private static String driver = "com.mysql.jdbc.Driver";
    private static String url = "jdbc:mysql://192.168.20.205:3306/practice";
    private static String username = "wangxd";
    private static String password = "wangxd#123";
    private static Connection conn = null;
    static {
        try{
            Class.forName(driver);
        } catch(ClassNotFoundException e){
            e.printStackTrace();
        }
    }

    public static Connection getConnection(){
        if(conn==null){
            try{
                conn = DriverManager.getConnection(url,username,password);
                return conn;
            }catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return conn;
    }
    public static void CloseAll(Connection conn, PreparedStatement sta, ResultSet rs) {
        if (conn != null) {
            try {
                conn.close();
                DBConnection.conn = null;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (sta != null) {
            try {
                sta.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
