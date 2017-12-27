import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.file.attribute.PosixFileAttributes;
import java.nio.file.attribute.PosixFilePermissions;
import java.sql.*;
import java.util.Date;

public class Jdbcfile {

    public void watch(File curDir, int clock) {

        /** get all the file in the current directory and store them in an array. **/

        File fileList[] = curDir.listFiles();
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.20.205:3306/practice", "wangxd", "wangxd#123");
            String sql = "select id,last_update_time from sys_file where file_path=? AND file_name=?";

            /** loop through all the child_file in the array **/

            for (File child : fileList ) {

                /** If a child file is actually a directory, start to scan that directory. (DFS a file system tree) **/

                if(child.isDirectory()) watch(child, clock);
                else {
                    PreparedStatement psta = conn.prepareStatement(sql);
                    psta.setString(1, curDir.getAbsolutePath());
                    psta.setString(2, child.getName());
                    ResultSet rs = psta.executeQuery();

                    /** if there is no records of this file in the file system Database, insert a new record. **/
                    if(!rs.next()){
                        insert(child, clock);
                        continue;
                    }

                    /** read the id and last_update_time of this file's record in the file system Database. **/
                    int id = rs.getInt("id");
                    setValid(id, clock);
                    /** Check whether current file is modified after last scan. If so, update the record of this file **/
                    Timestamp recordTimeStamp  = rs.getTimestamp("Last_update_time");
                    java.util.Date recordTime = new java.util.Date(recordTimeStamp.getTime());
                    java.util.Date lastModified = new Date(child.lastModified());

                    if (lastModified.after(recordTime)) {
                        update(id, child);
                    /*
                    File temp_file = new File();
                    temp_file.setLast_update_time(timeStamp);
                    temp_file.setId(rs.getInt("id"));
                    temp_file.setValid(rs.getBoolean("valid"));
                    temp_file.setExtension(rs.getString("extension"));
                    temp_file.setFile_name(rs.getString("file_name"));
                    temp_file.setFile_path(rs.getString("file_path"));
                    temp_file.setFilesize(rs.getInt("filesize"));
                    temp_file.setCreate_user(rs.getInt("create_user"));
                    temp_file.setLast_update_user(rs.getInt("last_update_user"));
                    temp_file.setStatus(rs.getInt("status"));
                    */
                    }
                    DBConnection.CloseAll(conn, psta, rs);

                    return;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void update(long id, File curfile){
        try {
            Path curPath = Paths.get(curfile.getAbsolutePath());
            Long filesize = Files.size(curPath);
            Timestamp recordTime =  new Timestamp(curfile.lastModified());
            PosixFileAttributes attr = Files.readAttributes(curPath, PosixFileAttributes.class);
            String owner = attr.owner().getName();
            String group = attr.group().getName();
            String permission = PosixFilePermissions.toString(attr.permissions());

            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.20.205:3306/practice", "wangxd", "wangxd#123");
            String sql = "update sys_file set filesize=?, last_update_time=?, owner=?, user_group=?, permission=? where id=?";
            PreparedStatement psta = conn.prepareStatement(sql);
            psta.setLong(1, filesize);
            psta.setTimestamp(2, recordTime);
            psta.setString(3, owner);
            psta.setString(4, group);
            psta.setString(5, permission);
            psta.setLong(6, id);
            psta.executeUpdate();
            DBConnection.CloseAll(conn, psta, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void insert(File curfile, int clock){
        try{
            String fileName = curfile.getName();
            Path curPath = Paths.get(curfile.getAbsolutePath());
            String filePath = curPath.toString();
            Long filesize = Files.size(curPath);
            BasicFileAttributes basicFileAttributes = Files.readAttributes(curPath, BasicFileAttributes.class);
            FileTime creatTime = basicFileAttributes.creationTime();
            Timestamp recordCreateTime = new Timestamp(creatTime.toMillis());
            Timestamp recordModifiedTime =  new Timestamp(curfile.lastModified());
            PosixFileAttributes posixFileAttributes = Files.readAttributes(curPath, PosixFileAttributes.class);
            String owner = posixFileAttributes.owner().getName();
            String group = posixFileAttributes.group().getName();
            String permission = PosixFilePermissions.toString(posixFileAttributes.permissions());

            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.20.205:3306/practice", "wangxd", "wangxd#123");
            String sql = "insert into sys_file (file_name, file_path, filesize, create_time, last_update_time, owner, user_group, permission, valid) values (?,?,?,?,?,?,?,?,?)";
            PreparedStatement psta =  conn.prepareStatement(sql);
            psta.setString(1, fileName);
            psta.setString(2, filePath);
            psta.setLong(3, filesize);
            psta.setTimestamp(4, recordCreateTime);
            psta.setTimestamp(5, recordModifiedTime);
            psta.setString(6, owner);
            psta.setString(7, group);
            psta.setString(8, permission);
            psta.setInt(9, clock);
            psta.executeUpdate();
            DBConnection.CloseAll(conn, psta, null);

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void setValid(long id, int mark){
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.20.205:3306/practice", "wangxd", "wangxd#123");
            String sql = "update sys_file set valid=? where id=?";
            PreparedStatement psta = conn.prepareStatement(sql);
            psta.setInt(1, mark);
            psta.setLong(2, id);
            psta.executeUpdate();
            DBConnection.CloseAll(conn, psta, null);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void clean(int mark){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            Connection conn = DriverManager.getConnection("jdbc:mysql://192.168.20.205:3306/practice", "wangxd", "wangxd#123");
            String sql = "delete from sys_file where NOT valid=?";
            PreparedStatement psta = conn.prepareStatement(sql);
            psta.setInt(1, mark);
            psta.executeUpdate();
            DBConnection.CloseAll(conn, psta, null);

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}