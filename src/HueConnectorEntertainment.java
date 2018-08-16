

import com.philips.lighting.hue.sdk.wrapper.HueLog;
import com.philips.lighting.hue.sdk.wrapper.Persistence;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnection;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeResponseCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedCallback;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateUpdatedEvent;
import com.philips.lighting.hue.sdk.wrapper.connection.ConnectionEvent;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscovery;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryCallback;
import com.philips.lighting.hue.sdk.wrapper.discovery.BridgeDiscoveryResult;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeBuilder;
import com.philips.lighting.hue.sdk.wrapper.domain.BridgeState;
import com.philips.lighting.hue.sdk.wrapper.domain.HueError;
import com.philips.lighting.hue.sdk.wrapper.domain.ReturnCode;
import com.philips.lighting.hue.sdk.wrapper.domain.SupportedFeature;
import com.philips.lighting.hue.sdk.wrapper.domain.clip.ClipResponse;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Group;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.GroupClass;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.GroupLightLocation;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.GroupStream;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.GroupType;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.ProxyMode;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Area;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Color;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Entertainment;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Callback;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Message;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Observer;
import com.philips.lighting.hue.sdk.wrapper.entertainment.StartCallback;
import com.philips.lighting.hue.sdk.wrapper.entertainment.Location;
import com.philips.lighting.hue.sdk.wrapper.entertainment.TweenType;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.Animation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.AnimationDelegate;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.ConstantAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.animation.TweenAnimation;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.AreaEffect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.Effect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.ExplosionEffect;
import com.philips.lighting.hue.sdk.wrapper.entertainment.effect.ManualEffect;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridge;
import com.philips.lighting.hue.sdk.wrapper.knownbridges.KnownBridges;
import com.philips.lighting.hue.sdk.wrapper.utilities.HueColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class HueConnectorEntertainment {
  static {
    // Load the huesdk native library before calling any SDK method
    System.load("/Users/tmaegerle/Documents/Philips Hue Lights Project/HueSDK4EDK/HueSDK/Apple/MacOS/Java/libhuesdk.dylib");

    // Configure the storage location and log level for the Hue SDK
    Persistence.setStorageLocation(MaxPhilipsHueObject.HUE_STORAGE_LOCATION, "Ableton");
    HueLog.setConsoleLogLevel(HueLog.LogLevel.INFO);
  }

  private static final String TAG = "HueQuickStartApp";

  private static final int MAX_HUE = 65535;

  private Bridge bridge;
  private Entertainment entertainment;
  private Group group;

  private BridgeDiscovery bridgeDiscovery;

  private List<BridgeDiscoveryResult> bridgeDiscoveryResults;

  // UI elements

  enum UIState {
    Idle,
    BridgeDiscoveryRunning,
    BridgeDiscoveryResults,
    Connecting,
    Pushlinking,
    Connected,
    EntertainmentReady
  }

  public HueConnectorEntertainment() {
    bridge = null;
    entertainment = null;

    // Connect to a bridge or start the bridge discovery
    // String bridgeIp = getLastUsedBridgeIp();
    String bridgeIp = "10.0.1.2";
    if (bridgeIp == null) {
      startBridgeDiscovery();
    } else {
      connectToBridge(bridgeIp);
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

  /**
   * Start the bridge discovery search
   * Read the documentation on meethue for an explanation of the bridge discovery options
   */
  private void startBridgeDiscovery() {
    disconnectFromBridge();

    bridgeDiscovery = new BridgeDiscovery();
    bridgeDiscovery.search(BridgeDiscovery.BridgeDiscoveryOption.UPNP, bridgeDiscoveryCallback);

    updateUI(UIState.BridgeDiscoveryRunning, "Scanning the network for hue bridges...");
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
   * The callback that receives the results of the bridge discovery
   */
  private BridgeDiscoveryCallback bridgeDiscoveryCallback = new BridgeDiscoveryCallback() {
    @Override
    public void onFinished(final List<BridgeDiscoveryResult> results, final ReturnCode returnCode) {
      // Set to null to prevent stopBridgeDiscovery from stopping it
      bridgeDiscovery = null;
      if (returnCode == ReturnCode.SUCCESS) {
        bridgeDiscoveryResults = results;

        updateUI(UIState.BridgeDiscoveryResults, "Found " + results.size() + " bridge(s) in the network.");
      } else if (returnCode == ReturnCode.STOPPED) {
        System.out.println(TAG + " - Bridge discovery stopped.");
      } else {
        updateUI(UIState.Idle, "Error doing bridge discovery: " + returnCode);
      }
    }
  };

  /**
   * Use the BridgeBuilder to create a bridge instance and connect to it
   */
  private void connectToBridge(String bridgeIp) {
    stopBridgeDiscovery();
    disconnectFromBridge();

    bridge = new BridgeBuilder("app name", "device name")
        .setIpAddress(bridgeIp)
        .setConnectionType(BridgeConnectionType.LOCAL)
        .setBridgeConnectionCallback(bridgeConnectionCallback)
        .addBridgeStateUpdatedCallback(bridgeStateUpdatedCallback)
        .build();

    bridge.connect();

    updateUI(UIState.Connecting, "Connecting to bridge...");
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
   * The callback that receives bridge connection events
   */
  private BridgeConnectionCallback bridgeConnectionCallback = new BridgeConnectionCallback() {
    @Override
    public void onConnectionEvent(BridgeConnection bridgeConnection, ConnectionEvent connectionEvent) {
      System.out.println(TAG + " - Connection event: " + connectionEvent);

      switch (connectionEvent) {
        case LINK_BUTTON_NOT_PRESSED:
          updateUI(UIState.Pushlinking, "Press the link button to authenticate.");
          break;

        case COULD_NOT_CONNECT:
          updateUI(UIState.Connecting, "Could not connect.");
          break;

        case CONNECTION_LOST:
          updateUI(UIState.Connecting, "Connection lost. Attempting to reconnect.");
          break;

        case CONNECTION_RESTORED:
          updateUI(UIState.Connected, "Connection restored.");
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
        System.out.println(TAG + " - Connection error: " + error.toString());
      }
    }
  };

  /**
   * The callback the receives bridge state update events
   */
  private BridgeStateUpdatedCallback bridgeStateUpdatedCallback = new BridgeStateUpdatedCallback() {
    @Override
    public void onBridgeStateUpdated(Bridge bridge, BridgeStateUpdatedEvent bridgeStateUpdatedEvent) {
      System.out.println(TAG + " - Bridge state updated event: " + bridgeStateUpdatedEvent);

      switch (bridgeStateUpdatedEvent) {
        case INITIALIZED:
          // The bridge state was fully initialized for the first time.
          // It is now safe to perform operations on the bridge state.
          updateUI(UIState.Connected, "Connected!");
          setupEntertainment();
          break;

        case LIGHTS_AND_GROUPS:
          // At least one light was updated.
          break;

        default:
          break;
      }
    }
  };

  /**
   * Randomize the color of all lights in the bridge
   * The SDK contains an internal processing queue that automatically throttles
   * the rate of requests sent to the bridge, therefore it is safe to
   * perform all light operations at once, even if there are dozens of lights.
   */
  private void randomizeLights() {
    BridgeState bridgeState = bridge.getBridgeState();
    List<LightPoint> lights = bridgeState.getLights();

    Random rand = new Random();

    for (final LightPoint light : lights) {
      final LightState lightState = new LightState();

      lightState.setHue(rand.nextInt(MAX_HUE));

      light.updateState(lightState, BridgeConnectionType.LOCAL, new BridgeResponseCallback() {
        @Override
        public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> list, List<HueError> errorList) {
          if (returnCode == ReturnCode.SUCCESS) {
            System.out.println(TAG + " - Changed hue of light " + light.getIdentifier() + " to " + lightState.getHue());
          } else {
            System.out.println(TAG + " - Error changing hue of light " + light.getIdentifier());
            for (HueError error : errorList) {
              System.out.println(TAG + " - " + error.toString());
            }
          }
        }
      });
    }
  }

  /**
   * Refresh the username in case it was created before entertainment was available
   */
  private void setupEntertainment() {
    bridge.refreshUsername(new BridgeResponseCallback() {
      @Override
      public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> responses, List<HueError> errors) {
        if (returnCode == ReturnCode.SUCCESS) {
          setupEntertainmentGroup();
        } else {
          // ...
        }
      }
    });
  }

  /**
   * Setup the group used for entertainment
   */
  private void setupEntertainmentGroup() {
    // look for an existing entertainment group

    List<Group> groups = bridge.getBridgeState().getGroups();
    for (Group group : groups) {
      if (group.getGroupType() == GroupType.ENTERTAINMENT) {
        this.group = group;
        createEntertainmentObject(group.getIdentifier());
        return;
      }
    }

    // Could not find an existing group, create a new one with all color lights

    List<LightPoint> validLights = getValidLights();

    if (validLights.isEmpty()) {
      System.out.println(TAG + " - No color lights found for entertainment");
      return;
    }

    createEntertainmentGroup(validLights);
  }

  /**
   * Create an entertainment group
   * @param validLights List of supported lights
   */
  private void createEntertainmentGroup(List<LightPoint> validLights) {
    ArrayList<String> lightIds = new ArrayList<String>();
    ArrayList<GroupLightLocation> lightLocations = new ArrayList<GroupLightLocation>();
    Random rand = new Random();

    for (LightPoint light : validLights) {
      lightIds.add(light.getIdentifier());

      GroupLightLocation location = new GroupLightLocation();
      location.setLightIdentifier(light.getIdentifier());
      location.setX(rand.nextInt(11) / 10.0 - 0.5);
      location.setY(rand.nextInt(11) / 10.0 - 0.5);
      location.setZ(rand.nextInt(11) / 10.0 - 0.5);

      lightLocations.add(location);
    }

    Group group = new Group();
    group.setName("NewEntertainmentGroup");
    group.setGroupType(GroupType.ENTERTAINMENT);
    group.setGroupClass(GroupClass.TV);

    group.setLightIds(lightIds);
    group.setLightLocations(lightLocations);

    GroupStream stream = new GroupStream();
    stream.setProxyMode(ProxyMode.AUTO);
    group.setStream(stream);
    this.group = group;

    bridge.createResource(group, new BridgeResponseCallback() {
      @Override
      public void handleCallback(Bridge bridge, ReturnCode returnCode, List<ClipResponse> responses, List<HueError> errors) {
        if (returnCode == ReturnCode.SUCCESS) {
          createEntertainmentObject(responses.get(0).getStringValue());
        } else {
          System.out.println(TAG + " - Could not create entertainment group.");
        }
      }
    });
  }

  /**
   * Create an entertainment object and register an observer to receive messages
   * @param groupId The entertainment group to be used
   */
  private void createEntertainmentObject(String groupId) {
    int defaultPort = 2100;

    entertainment = new Entertainment(bridge, defaultPort, groupId);

    entertainment.registerObserver(new Observer() {
      @Override
      public void onMessage(Message message) {
        //System.out.println(TAG + " - Entertainment message: " + message.getType() + " " + message.getUserMessage());
      }
    }, Message.Type.RENDER);

    updateUI(UIState.EntertainmentReady, "Connected, entertainment ready.");
    entertainment.start(new StartCallback() {
      @Override
      public void handleCallback(StartStatus status) {
        if (status != StartStatus.Success) {
          return;
        }
        //playExplosionEffect();
      }
    });
  }

  /**
   * Get a list of all lights that support entertainment
   * @return Valid lights
   */
  private List<LightPoint> getValidLights() {
    ArrayList<LightPoint> validLights = new ArrayList<LightPoint>();
    for (final LightPoint light : bridge.getBridgeState().getLights()) {
      if (light.getInfo().getSupportedFeatures().contains(SupportedFeature.STREAM_PROXYING)) {
        validLights.add(light);
      }
    }
    return validLights;
  }

  /**
   * Add an explosion effect to the entertainment engine
   */
  public void playExplosionEffect(double time, double r, double b, double g, double brightness) {
    Color color = getColorFrom255RgbBrightness(r, g, b, brightness);
    Location center = new Location(0.0, 0.0);
    final double duration = time * 1000;
    double radius = 1.7;
    double radiusExpansionTime = 100;
    double intensityExpansionTime = 50;

    ExplosionEffect effect = new ExplosionEffect();
    effect.prepareEffect(color, center, duration, radius, radiusExpansionTime, intensityExpansionTime);
    effect.setFixedOpacity(1.0);
    effect.enable();

    entertainment.lockMixer();
    entertainment.addEffect(effect);
    entertainment.unlockMixer();
  }

  public void colorAll() {
    AreaEffect effect = new AreaEffect();
    effect.addArea(new Area(-1, 1, 1, -1, "Test", false));
    double r = Math.random();
    double g = Math.random();
    double b = Math.random();
    Color c = normalizeRGB(r, g, b);

    System.out.println(String.format("Setting fixed color: r: %d, g: %d, b: %d", (int) c.getRed(), (int) c.getGreen(), (int) c.getBlue()));

    effect.setFixedColor(new Color(r, g, b));


    effect.enable();

    entertainment.lockMixer();
    entertainment.addEffect(effect);
    entertainment.unlockMixer();
  }

  public void setColorForArea(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY,
                              double r, double g, double b, double brightness) {
    AreaEffect effect = new AreaEffect();
    effect.addArea(new Area(topLeftX, topLeftY, bottomRightX, bottomRightY, "Test", false));
    Color c = getColorFrom255RgbBrightness(r, g, b, brightness);
    effect.setFixedColor(c);
    effect.setFixedOpacity(0.1);
    effect.enable();

    entertainment.lockMixer();
    entertainment.addEffect(effect);
    entertainment.unlockMixer();
  }

  public void draw(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY,
                   double transitionTime, double toR, double toG, double toB) {
    // TODO: This starts the light at the current state of a random bulb in the area

    Color fromRGB = null;
    for (GroupLightLocation l : group.getLightLocations()) {
      if (l.getX() >= topLeftX && l.getX() <= bottomRightX && l.getY() <= topLeftY && l.getY() >= bottomRightY) {
        System.out.println("TEST " + l.getLightIdentifier());
        LightPoint light = bridge.getBridgeState().getLight(l.getLightIdentifier());
        HueColor.RGB rgb = light.getLightState().getColor().getRGB();
        fromRGB = normalizeRGB(rgb.r, rgb.b, rgb.g);
        System.out.println(String.format("Fetched color: r: %d, g: %d, b: %d", (int) fromRGB.getRed(), (int) fromRGB.getGreen(), (int) fromRGB.getBlue()));
        break;
      }
    }
    AreaEffect effect = new AreaEffect();

    effect.addArea(new Area(-1, 1, 1, -1, "Test", false));
    effect.setColorAnimation(
        new TweenAnimation(fromRGB.getRed() / 255.0D, toR, transitionTime, TweenType.EaseInOutSine),
        new TweenAnimation(fromRGB.getBlue() / 255.0D, toG, transitionTime, TweenType.EaseInOutSine),
        new TweenAnimation(fromRGB.getGreen() / 255.0D, toB, transitionTime, TweenType.EaseInOutSine)
    );

    effect.enable();

    AreaEffect fixedColor = new AreaEffect();
    fixedColor.addArea(new Area(-1, 1, 1, -1, "Test2", false));
    fixedColor.setFixedColor(new Color(toR, toG, toB));
    fixedColor.enable();

    entertainment.lockMixer();
    entertainment.addEffect(effect);
    entertainment.addEffect(fixedColor);
    entertainment.unlockMixer();


    entertainment.addEffect(effect);

    entertainment.lockMixer();
    entertainment.addEffect(effect);
    entertainment.unlockMixer();

  }

  public void strobeRandomLight(double transitionTime) {
    String lightId = group.getLightIds().get((int) (Math.random() * group.getLightIds().size()));

    ManualEffect on = new ManualEffect();
    on.setLightToColor(lightId, new Color(1.0, 1.0, 1.0));
    on.enable();

    ManualEffect off = new ManualEffect();
    off.setLightToColor(lightId, new Color(0, 0, 0));
    off.enable();

    changeEffectAfterDelay(on, off, transitionTime);
  }

  public void strobe(double transitionTime) {
    AreaEffect on = new AreaEffect();
    on.addArea(new Area(-1, 1, 1, -1, "Test", false));
    on.setFixedColor(new Color(1.0, 1.0, 1.0));
    on.setFixedOpacity(1.0);
    on.enable();

    AreaEffect off = new AreaEffect();
    off.addArea(new Area(-1, 1, 1, -1, "Test2", false));
    off.setFixedColor(new Color(0, 0, 0));
    off.setFixedOpacity(1.0);
    off.enable();

    changeEffectAfterDelay(on, off, transitionTime);
  }

  /*
   delay is in seconds
   */
  private void changeEffectAfterDelay(Effect first, Effect second, double delay) {
    entertainment.lockMixer();
    entertainment.addEffect(first);
    entertainment.unlockMixer();

    new Thread(new Runnable() {
      public void run() {
        try {
          Thread.sleep((long) (delay * 1000));
          entertainment.lockMixer();
          entertainment.addEffect(second);
          entertainment.unlockMixer();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }).run();
  }

  // Takes 20, 30, 10 and turns it into 170, 255, 85
  public static Color normalizeRGB(double r, double g, double b) {
    double fraction = 255.0 / Math.max(Math.max(r, g), b);
    return new Color(r * fraction, g * fraction, b * fraction);
  }

  public static Color getColorFrom255RgbBrightness(double r, double g, double b, double brightness) {
    double maxColor = Math.max(Math.max(r, g), b);
    double fraction = maxColor == 0 ? 0 : 1.0 / maxColor;
    double brightnessCoefficient = brightness / 255.0;
    double r2 = r * fraction * brightnessCoefficient;
    double g2 = g * fraction * brightnessCoefficient;
    double b2 = b * fraction * brightnessCoefficient;
    //System.out.println("red: " + r2 + ", green: " + g2 + ", blue: " + b2);
    return new Color(r2, g2, b2);
  }

  public void turnAreaOff() {
    AreaEffect effect = new AreaEffect();
    effect.addArea(new Area(-1, 1, 1, -1, "Test", false));
    effect.setFixedColor(new Color(0, 0, 0));
    effect.enable();

    entertainment.lockMixer();
    entertainment.addEffect(effect);
    entertainment.unlockMixer();
  }

  public void draw(double topLeftX, double topLeftY, double bottomRightX, double bottomRightY, int transitionTime, int brightness, double red, double green, double blue) {

    AreaEffect effect = new AreaEffect();
    effect.addArea(new Area(topLeftX, topLeftY, bottomRightX, bottomRightY, "Draw", false));

//    Animation r = new TweenAnimationz(blue, transitionTime, 10, TweenType.EaseInOutSine);

//    effect.setFixedColor(new Color(Math.random(), Math.random(), Math.random()));
//    Animation redAnimation = new ConstantAnimation(red, 0);
//    Animation greenAnimation = new ConstantAnimation(green, 0);
//    Animation blueAnimation = new ConstantAnimation(blue, 0);
//    effect.setColorAnimation(redAnimation, greenAnimation, blueAnimation);
    // effect.setFixedOpacity(0.25);
    //effect.setColorAnimation(r, g, b);

    effect.setColorAnimation(new TweenAnimation(1, 0,1000,TweenType.Linear),new TweenAnimation(0, 1,1000,TweenType.Linear),new ConstantAnimation(1));

    effect.enable();

    entertainment.lockMixer();
    entertainment.addEffect(effect);
    entertainment.unlockMixer();
  }

  private void updateUI(final UIState state, final String status) {
    System.out.println(TAG + " - Status: " + status);
  }

}
