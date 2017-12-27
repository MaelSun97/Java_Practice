import java.io.File;
import java.util.concurrent.TimeUnit;

public class Test{

    public static void main(String[] args){
        DBConnection.getConnection();
        Jdbcfile fileSystem = new Jdbcfile();
        File root = new File(args[0]);
        int clock = 0;
        while(true) {
            fileSystem.watch(root, clock);
            fileSystem.clean(clock);
            clock = (clock+1)%10;
            int n = Integer.parseInt(args[1]);
            try {
                TimeUnit.MINUTES.sleep(n);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}