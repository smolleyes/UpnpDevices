package fr.ubukey;

import org.cybergarage.upnp.ControlPoint;
import org.cybergarage.upnp.device.NotifyListener;
import org.cybergarage.upnp.device.SearchResponseListener;
import org.cybergarage.upnp.ssdp.SSDPPacket;
import android.util.Log;

public class MyControlPoint extends ControlPoint implements /*DeviceChangeListener,*/ NotifyListener, SearchResponseListener {
    public MyControlPoint() {
        addNotifyListener(this);
        addSearchResponseListener(this);
    }

    @Override
    public void deviceNotifyReceived(SSDPPacket packet) { // NotifyListener
        Log.v("UpnpDevices","deviceNotifyReceived");
    }

    @Override
    public void deviceSearchResponseReceived(SSDPPacket packet) { // SearchResponseListener
        Log.v("UpnpDevices","deviceSearchReceived");
    }
}
