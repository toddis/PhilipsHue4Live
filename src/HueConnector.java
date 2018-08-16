import com.philips.lighting.hue.sdk.wrapper.HueLog;
import com.philips.lighting.hue.sdk.wrapper.Persistence;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnection;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.FoundDevicesCallback;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryCallback;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryResult;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeBuilder;
import com.philips.lighting.hue.sdk.wrapper.domain.DomainType;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.device.Device;
import com.philips.lighting.hue.sdk.wrapper.domain.device.DeviceConfiguration;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightConfiguration;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Group;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridge;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges;
import com.philips.lighting.hue.sdk.wrapper.utilities.HueColor;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class HueConnector {
  public Bridge bridge;
  public BridgeDiscovery bridgeDiscovery;
  public List<BridgeDiscoveryResult> bridgeDiscoveryResults;

  private BridgeDiscoveryCallback bridgeDiscoveryCallback = new BridgeDiscoveryCallback() {
    @Override
    public void onFinished(final List<BridgeDiscoveryResult> results, final ReturnCode returnCode) {
      // Set to null to prevent stopBridgeDiscovery from stopping it
      bridgeDiscovery = null;
      //updateUI(UIState.BridgeDiscoveryRunning, "Scanning the network for hue bridges...");

      if (returnCode == ReturnCode.SUCCESS) {
        bridgeDiscoveryResults = results;
        System.out.println("Found " + results.size() + " bridge(s) in the network.");
      } else if (returnCode == ReturnCode.STOPPED) {
        System.out.println("Bridge discovery stopped.");
      } else {
        System.out.println("Error doing bridge discovery: " + returnCode);
      }
    }
  };

  /**
   * The callback that receives bridge connection events
   */
  private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
    @Override
    public void onConnectionEvent(BridgeConnection bridgeConnection, ConnectionEvent connectionEvent) {
      // Log.i(TAG, "Connection event: " + connectionEvent);
      System.out.println("Connection event: " + connectionEvent);

      switch (connectionEvent) {
        case LINK_BUTTON_NOT_PRESSED:
          // updateUI(MainActivity.UIState.Pushlinking, "Press the link button to authenticate.");
          System.out.println("Press the link button to authenticate.");
          break;

        case COULD_NOT_CONNECT:
          System.out.println("Could not connect.");
          break;

        case CONNECTION_LOST:
          System.out.println("Connection lost. Attempting to reconnect.");
          break;

        case CONNECTION_RESTORED:
          System.out.println("Connection restored.");
          break;

        case DISCONNECTED:
          // User-initiated disconnection.
          break;

        default:
          break;
      }
    }

    @Override
    public void onConnectionError(BridgeConnection bridgeConnection, List<HueError> list) {
      for (HueError error : list) {
        // Log.e(TAG, "Connection error: " + error.toString());
        System.err.println("Connection error: " + error.toString());
      }
    }
  };

  /**
   * The callback the receives bridge state update events
   */
  private BridgeStateUpdatedCallback bridgeStateUpdatedCallback = new BridgeStateUpdatedCallback() {
    @Override
    public void onBridgeStateUpdated(Bridge bridge, BridgeStateUpdatedEvent bridgeStateUpdatedEvent) {
      // Log.i(TAG, "Bridge state updated event: " + bridgeStateUpdatedEvent);
      System.out.println("Bridge state updated event: " + bridgeStateUpdatedEvent);

      switch (bridgeStateUpdatedEvent) {
        case INITIALIZED:
          // The bridge state was fully initialized for the first time.
          // It is now safe to perform operations on the bridge state.
          //updateUI(MainActivity.UIState.Connected, "Connected!");
          break;

        case LIGHTS_AND_GROUPS:
          // At least one light was updated.
          break;

        default:
          break;
      }
    }
  };

  private FoundDevicesCallback foundDevicesCallback = new FoundDevicesCallback() {

    @Override
    public void onDevicesFound(Bridge bridge, List<Device> list, List<HueError> list1) {
      System.out.println("Found " + list.size() + " devices!");
    }

    @Override
    public void onDeviceSearchFinished(Bridge bridge, List<HueError> list) {
      System.out.println("Finished searching");
    }
  };

  /**
   * Start the bridge discovery search
   * Read the documentation on meethue for an explanation of the bridge discovery options
   */
  private void startBridgeDiscovery() {
    disconnectFromBridge();
    bridgeDiscovery = new BridgeDiscovery();
    // ALL Include [UPNP, IPSCAN, NUPNP] but in some nets UPNP and NUPNP is not working properly
    bridgeDiscovery.search(BridgeDiscovery.BridgeDiscoveryOption.ALL, bridgeDiscoveryCallback);
    System.out.print("Scanning the network for hue bridges...");
    while (bridgeDiscovery != null && bridgeDiscovery.isSearching()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        throw new RuntimeException(e);
      }
      System.out.print(".");
    }
    System.out.println();
  }

  /**
   * Use the BridgeBuilder to create a bridge instance and connect to it
   */
  private void connectToBridge(String bridgeIp) {
    stopBridgeDiscovery();
    disconnectFromBridge();

    bridge = new BridgeBuilder("ableton", "mymacbook")
        .setIpAddress(bridgeIp)
        .setConnectionType(BridgeConnectionType.LOCAL)
        .setBridgeConnectionCallback(bridgeConnectionCallback)
        .addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
        .build();

    System.out.print("Connecting to bridge at " + bridgeIp + " ...");

    BridgeConnection connection = bridge.getBridgeConnection(BridgeConnectionType.LOCAL);
    ReturnCode result = connection.connect();

    while (!connection.isConnected()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.print(".");
    }

    if (!connection.isAuthenticated())
      System.out.print("Need to authenticate...");
    while (!connection.isAuthenticated()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      System.out.print(".");
    }
    System.out.println();

    if (result != ReturnCode.SUCCESS) {
      throw new RuntimeException("Problem connecting to bridge. Return code: " + result);
    }
  }

  /**
   * Disconnect a bridge
   * The hue SDK supports multiple bridge connections at the same time,
   * but for the purposes of this demo we only connect to one bridge at a time.
   */
  private void disconnectFromBridge() {
    if (bridge != null) {
      bridge.disconnect();
      bridge = null;
    }
  }

  /**
   * Stops the bridge discovery if it is still running
   */
  private void stopBridgeDiscovery() {
    if (bridgeDiscovery != null) {
      bridgeDiscovery.stop();
      bridgeDiscovery = null;
    }
  }

  /**
   * Use the KnownBridges API to retrieve the last connected bridge
   * @return Ip address of the last connected bridge, or null
   */
  private String getLastUsedBridgeIp() {
    List<KnownBridge> bridges = KnownBridges.getAll();

    if (bridges.isEmpty()) {
      return null;
    }

    return Collections.max(bridges, new Comparator<KnownBridge>() {
      @Override
      public int compare(KnownBridge a, KnownBridge b) {
        return a.getLastConnected().compareTo(b.getLastConnected());
      }
    }).getIpAddress();
  }

  // TODO: Test if it's better to do group function or get each bulb from that group and do them individually
  public void drawGroup(int groupId, int brightness, int transitionTime, boolean on, int red, int green, int blue) {
    Group group = bridge.getBridgeState().getGroup(groupId + "");
    if (group == null) {
      System.out.println("Could not find group with id: " + groupId);
      return;
    }

    // Get a light for model idenifier and version
    DeviceConfiguration lightConf = bridge.getBridgeState().getDevice(DomainType.LIGHT_POINT, group.getLightIds().get(0)).getConfiguration();
    System.out.println("DEBUG -- light model identifier: " + lightConf.getModelIdentifier() + ", sw version: " + lightConf.getSwVersion());

    HueColor color = new HueColor(
        new HueColor.RGB(red, green, blue),
        lightConf.getModelIdentifier(),
        lightConf.getSwVersion());

    LightState lightState = new LightState();
    lightState.setOn(on);
    lightState.setBrightness(brightness);
    lightState.setTransitionTime(transitionTime);
    lightState.setXYWithColor(color);

    group.apply(lightState);
  }

  public void drawGroup2(int groupId, int brightness, int transitionTime, boolean on, int red, int green, int blue) {
    Group group = bridge.getBridgeState().getGroup(groupId + "");
    if (group == null) {
      System.out.println("Could not find group with id: " + groupId);
      return;
    }

    for (String lightId : group.getLightIds()) {
      Device light = bridge.getBridgeState().getDevice(DomainType.LIGHT_POINT, lightId);
      DeviceConfiguration lightConf = light.getConfiguration();
      HueColor color = new HueColor(
          new HueColor.RGB(red, green, blue),
          lightConf.getModelIdentifier(),
          lightConf.getSwVersion());
      LightState lightState = new LightState();
      lightState.setOn(on);
      lightState.setBrightness(brightness);
      lightState.setTransitionTime(transitionTime);
      lightState.setXYWithColor(color);

      light.updateState(lightState);
    }
  }

  static {
    // Load the huesdk native library before calling any SDK method
    System.load("/Users/tmaegerle/Documents/Philips Hue Lights Project/HueSDK4EDK/HueSDK/Apple/MacOS/Java/libhuesdk.dylib");

    // Configure the storage location and log level for the Hue SDK
    Persistence.setStorageLocation(MaxPhilipsHueObject.HUE_STORAGE_LOCATION, "Ableton");
    HueLog.setConsoleLogLevel(HueLog.LogLevel.INFO);
  }

  public HueConnector() {
    // Connect to a bridge or start the bridge discovery
    String bridgeIp = getLastUsedBridgeIp();
    if (bridgeIp == null) {
      startBridgeDiscovery();
      if (bridgeDiscoveryResults.size() > 0) {
        connectToBridge(bridgeDiscoveryResults.get(0).getIP());
      }
    } else {
      connectToBridge(bridgeIp);
    }
  }

}
