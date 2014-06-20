import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.device.NotifyListener;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;

public class MyControlPoint extends ControlPoint implements /*DeviceChangeListener,*/ NotifyListener, SearchResponseListener {
    public MyControlPoint() {
        addNotifyListener(this);
        addSearchResponseListener(this);
    }

    @Override
    public void deviceNotifyReceived(SSDPPacket packet) { // NotifyListener
        System.out.println("deviceNotifyReceived");
    }

    @Override
    public void deviceSearchResponseReceived(SSDPPacket packet) { // SearchResponseListener
        System.out.println("deviceSearchReceived");
    }
}
