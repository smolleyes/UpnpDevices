package fr.ubukey;

import android.net.wifi.WifiManager;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.cybergarage.upnp.DeviceList;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.ServiceList;
import org.cybergarage.upnp.UPnP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.io.IOUtils;

public class UpnpDevices extends CordovaPlugin {
	WifiManager.MulticastLock lock;
	private CallbackContext callback;
	
	@Override
	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);

		WifiManager wifi = (WifiManager) this.cordova.getActivity()
				.getSystemService(android.content.Context.WIFI_SERVICE);
		lock = wifi.createMulticastLock("UpnpDevicesPluginLock");
		lock.setReferenceCounted(true);
		lock.acquire();

		Log.v("UpnpDevices", "Initialized");
	}
	
	@Override
	public boolean execute(String action, String arg,
			CallbackContext callbackContext) {
		this.callback = callbackContext;
		if (action.equals("start")) {
			cordova.getThreadPool().execute(new Runnable() {
					public void run() {
						searchDevices(); // Thread-safe.
					}
				});
		}
		PluginResult result = new PluginResult(Status.NO_RESULT);
		result.setKeepCallback(true);
		// return result;
		return true;
	}
	
    public void searchDevices() {
		Log.v("UpnpDevices", "Search started...");
        UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
        MyControlPoint controlPoint = new MyControlPoint();
        controlPoint.start();
        try {
            Thread.sleep(5000); // wait for devices to be found
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        DeviceList rootDevices = controlPoint.getDeviceList();
        int numDevices = rootDevices.size();
        JSONArray servers = new JSONArray();
    	JSONArray renderers = new JSONArray();
    	JSONArray avTransports = new JSONArray();
    	JSONArray connectionManagers = new JSONArray();
    	JSONObject devices = new JSONObject();
        if (numDevices > 0) {
            for (int i = 0; i < numDevices; i++) {
            	try {
            		JSONObject jo = new JSONObject();
            		String type = rootDevices.getDevice(i).getDeviceType();
            		String udn = rootDevices.getDevice(i).getUDN();
            		String modelName = rootDevices.getDevice(i).getModelName();
            		String uri = rootDevices.getDevice(i).getLocation();

            		URL url = new URL(uri);
            		URLConnection con = url.openConnection();
            		InputStream in = con.getInputStream();
            		String encoding = con.getContentEncoding();
            		encoding = encoding == null ? "UTF-8" : encoding;
            		String guid = IOUtils.toString(in, encoding);
            		
            		jo.put("_index", i);
            		jo.put("config", ""+guid+"");
            		jo.put("modelName", modelName);
            		jo.put("udn", udn);
            		jo.put("type", type);
            		jo.put("online", true);
            		jo.put("onserviceoffline", false);
            		jo.put("onserviceonline", false);
					Pattern pattern = Pattern.compile("http?:\\/\\/.+:[1-6][0-9]{0,4}");
					Matcher url = pattern.matcher(rootDevices.getDevice(i).getLocation());
					url.find();
					jo.put("baseUrl", url.group());
					jo.put("friendlyName", rootDevices.getDevice(i).getFriendlyName());
					try {
						jo.put("icon",url.group()+rootDevices.getDevice(i).getIcon(0).getURL());
					} catch (ArrayIndexOutOfBoundsException e) { 
						jo.put("icon","[]");
					}
					ServiceList sList = rootDevices.getDevice(i).getServiceList();
					int sLength = sList.size();
					if (sLength > 0) {
						for(int n=0; n < sLength; n++) {
							Service service = sList.getService(n);
							String name = service.getServiceID();
							jo.put("name", name);
							jo.put("id",udn+":"+type+name);
							jo.put("url",url.group()+service.getControlURL());
							if (name.indexOf("RenderingControl") > 0){
								renderers.put(jo);
							} else if (name.indexOf("AVTransport") > 0){
								avTransports.put(jo);
							} else if (name.indexOf("ContentDirectory") > 0){
								servers.put(jo);
							} else if (name.indexOf("ConnectionManager") > 0){
								connectionManagers.put(jo);
							}
							if(i+1 == numDevices){
			            		controlPoint.stop();
			            		devices.put("_servers",servers);
			            		devices.put("_connectionManagers",connectionManagers);
			            		devices.put("_renderers",renderers);
			            		devices.put("_avTransports",avTransports);
			            		sendCallback("result", devices);
			            	}
						}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }  
        } else {
			try {
				JSONObject jo = new JSONObject();
				jo.put("list","No UPNP root devices found");
				sendCallback("result", jo);
			} catch(JSONException e) {
				e.printStackTrace();
			}
        }
    }
    
    public void sendCallback(String action, JSONObject json) {
		JSONObject status = new JSONObject();
		try {
			status.put("action", action);
			status.put("result", json);
			Log.d("UpnpDevices", "Sending result: " + status.toString());

			PluginResult result = new PluginResult(PluginResult.Status.OK,
					status);
			result.setKeepCallback(true);
			this.callback.success(status);

		} catch (JSONException e) {

			e.printStackTrace();
		}

	}
}
