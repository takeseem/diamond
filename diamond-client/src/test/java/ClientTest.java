import com.taobao.diamond.manager.DiamondManager;
import com.taobao.diamond.manager.ManagerListener;
import com.taobao.diamond.manager.impl.DefaultDiamondManager;

import java.util.concurrent.Executor;

/**
 * Created with IntelliJ IDEA.
 * User: gaozhenlong
 * Date: 17-5-14
 * Time: 下午10:27
 * To change this template use File | Settings | File Templates.
 */
public class ClientTest {
    public static void main(String[] args) {

//        System.setProperty("http.proxyHost", "localhost");
//        System.setProperty("http.proxyPort", "8888");
//        System.setProperty("https.proxyHost", "localhost");
//        System.setProperty("https.proxyPort", "8888");

        DiamondManager manager = new DefaultDiamondManager("1", new ManagerListener() {
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                System.out.println("receive config: " + configInfo);
            }
        });
//        manager.get
        String configInfo = manager.getAvailableConfigureInfomation(1000);
        System.out.println("config: " + configInfo);

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
