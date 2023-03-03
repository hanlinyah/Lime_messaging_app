import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class getProperties {
        private static Properties properties=new Properties();
        private static int PORT;
        private static String SPILIT;
    public static int getport(){
        try {
            properties.load(new FileReader("src/setting.properties", StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        PORT= Integer.parseInt(properties.getProperty("port"));
        return PORT;
    }
    public static String getspilit(){
        try {
            properties.load(new FileReader("src/setting.properties", StandardCharsets.UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        SPILIT= properties.getProperty("spilit").toString();
        return SPILIT;
    }

}
