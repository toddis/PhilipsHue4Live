import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;

import com.philips.lighting.hue.sdk.wrapper.connection.BridgeConnectionType;
import com.philips.lighting.hue.sdk.wrapper.connection.BridgeStateCacheType;
import com.philips.lighting.hue.sdk.wrapper.domain.Bridge;
import com.philips.lighting.hue.sdk.wrapper.domain.DomainType;
import com.philips.lighting.hue.sdk.wrapper.domain.SupportedFeature;
import com.philips.lighting.hue.sdk.wrapper.domain.device.Device;
import com.philips.lighting.hue.sdk.wrapper.domain.device.DeviceState;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightConfiguration;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightPoint;
import com.philips.lighting.hue.sdk.wrapper.domain.device.light.LightState;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.Group;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.GroupLightLocation;
import com.philips.lighting.hue.sdk.wrapper.domain.resource.GroupType;
import com.philips.lighting.hue.sdk.wrapper.utilities.HueColor;

public class Main {

    // Streaming credentials (put request to <ip-address>/api). Ip for local is 169.254.8.173
    // {
    //     "devicetype":"ableton#mymacbook"
    // }

        // [
    // 	{
    // 		"success": {
    // 			"username": "WZtGoHPHJvRmKFAJ6qOMfAg6huflerOxgVXG2rlB",
    // 			"clientkey": "011081C4176F68194BB5511B14D69D24"
    // 		}
    // 	}
    // ]

    // NEW (9/11/2018)
    // [
    //	{
    //		"success": {
    //			"username": "yBgR1WFTz-lEv1HS4PjmrOCF37ZiVxNPi0b2ZgUI"
    //		}
    //	}
    //]
    //
    // Update location info: https://developers.meethue.com/documentation/hue-entertainment-api
    // TLDR; go to http://10.0.1.2/debug/clip.html (or 169.254.8.173 on local)
    // do PUT on /api/yBgR1WFTz-lEv1HS4PjmrOCF37ZiVxNPi0b2ZgUI/groups/3
    // with body like:
    // {
    //	"locations": {
    //		"15": [
    //			0.4,
    //			0.5,
    //			0
    //		]
    //	}
    //}

    private static final int MAX_HUE = 65535;

    public static void main(String[] args) throws Exception {
        // HueConnectorEntertainment h = new HueConnectorEntertainment();
        HueConnector h = new HueConnector();
        Thread.sleep(10000);
//        System.out.println("Before:");
//        printAllDeviceIds(h.bridge);
//        setupLights(h.bridge);
//        System.out.println();
//        System.out.println("After:");
//        printAllDeviceIds(h.bridge);
        // new SetupLightsWithGui();

        setLightLocations(h.bridge);
        // setupLights(h.bridge);
    }



//    public static void main(String[] args) throws Exception {
//        HueConnectorEntertainment h = new HueConnectorEntertainment();
////        h.bridge.getBridgeConfiguration();
////        h.drawGroup2(0, 150, 0, true, 254, 254, 254);
////        Thread.sleep(1000);
////        h.randomizeLights();
//        Thread.sleep(10000);
//        // h.colorAll();
//
//        Thread.sleep(1000);
//        // h.setColorForArea(-1, 1, 1, -1, 10,0,0,30);
//        for (int i = 0; i < 20; i++) {
//            System.out.println("strobe");
//            // h.strobe(1, -1, 1, 1, -1, 255, 255, 255, 255);
//            h.setColorForArea(-1, 1, 1, -1, 255 * Math.random(), 255, 255, 100);
////            h.strobeRandomLight( 0.15, 255, 255, 255, 255);
//            Thread.sleep(3000);
//        }

//        for (int i = 0; i < 100; i++) {
////            h.draw(-1, 1, 1, -1, 1000, 130, Math.random(), Math.random(), Math.random());
////            h.colorAll2();
////            Thread.sleep(5000);
//            if (i % 2 != 0) {
////                h.turnAreaOff();
//                h.draw(-1, 1, 1, -1, 5000, 0.5, 0.125, 0.05);
//            } else {
//                //h.draw(-1, 1, 1, -1, 5000, 0.5, 0.125, 0.05, 1.0, 0.25, 0.1);
//                h.colorAll();
//            }
//            Thread.sleep(10000);
//        }
//
//        Thread.sleep(10000);
//    }

    private static final String[] SERIALS = {
        "A2F165", // F1
        "5A7E3A",  // B1
        "1E7EFB",  // B2
        "9DC479",  // B3
        "16A756",  // M2
        "46182C", // M3
        "827F70", // F4
        "019B2C"    // M1
    };
    private static void setupLights(Bridge bridge) throws Exception {
        bridge.findNewDevices(Arrays.asList(SERIALS));
        Thread.sleep(60000);
        List<String> lightIds = Arrays.asList(
            (String[]) bridge.getBridgeState().getDevices(DomainType.LIGHT_POINT).stream().map(
                device -> device.getIdentifier()
            ).toArray()
        );

        Group g = getEntertainmentGroup(bridge);
        g.setLightIds(lightIds);
    }

    private static void printAllDeviceIds(Bridge bridge) {
        for (Device d : bridge.getBridgeState().getDevices(DomainType.LIGHT_POINT)) {
            System.out.println(((LightPoint) d).getIdentifier());
        }
    }

//    private static void createStageLights(Bridge bridge) {
//        List<LightPoint> validLights = getValidLights(bridge);
//
//
//    }
//
//    private static List<LightPoint> getValidLights(Bridge bridge) {
//        ArrayList<LightPoint> validLights = new ArrayList<LightPoint>();
//        for (final LightPoint light : bridge.getBridgeState().getLights()) {
//            if (light.getInfo().getSupportedFeatures().contains(SupportedFeature.STREAM_PROXYING)) {
//                validLights.add(light);
//            }
//        }
//        return validLights;
//    }

//    private static class SetupLightsWithGui {
//        /**
//         * Create the GUI and show it.  For thread safety,
//         * this method should be invoked from the
//         * event-dispatching thread.
//         */
//        private void createAndShowGUI() {
//            //Create and set up the window.
//            JFrame frame = new JFrame("HelloWorldSwing");
//            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//
//            //Add the ubiquitous "Hello World" label.
//            JLabel label = new JLabel("Hello World");
//            frame.getContentPane().add(label);
//
//            //Display the window.
//            frame.pack();
//            frame.setVisible(true);
//        }
//
//        public SetupLightsWithGui() {
//            //Schedule a job for the event-dispatching thread:
//            //creating and showing this application's GUI.
//            javax.swing.SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    createAndShowGUI();
//                }
//            });
//        }
//    }

    public static void setLightLocations(Bridge bridge) {
        Scanner input = new Scanner(System.in);

        List<Device> devices = bridge.getBridgeState().getDevices(DomainType.LIGHT_POINT);
        devices.sort((a, b) -> a.getIdentifier().compareTo(b.getIdentifier()));
        List<String> ids = new ArrayList<String>();
        for (Device d : devices) {
            ids.add(d.getIdentifier());
        }

        String response = "";
        while (!response.equals("quit")) {
            System.out.print("'pl' to print locations, input device ID to flash, append x and y positions to set location (available IDs: " +
            Arrays.toString(ids.toArray()) + "): ");
            response = input.nextLine();
            if (!response.equals("quit")) {
                String[] arr = response.split(" ");
                if (arr.length == 1) {
                    if (arr[0].equals("pl")) {
                        printEntertainmentGroupLightLocations(bridge);
                    } else {
                        setLightToRandomColor(bridge, arr[0]);
                    }
                } else {
                    setLightLocation(bridge, arr[0], Double.parseDouble(arr[1]), Double.parseDouble(arr[2]));
                }
            }
        }

    }

    private static void setLightToRandomColor(Bridge b, String id) {
        LightPoint light = (LightPoint)b.getBridgeState().getDevice(DomainType.LIGHT_POINT, id);
        LightConfiguration lightConfiguration = light.getLightConfiguration();
        HueColor color = new HueColor(
            new HueColor.RGB(((int) (Math.random() * 255)), ((int) (Math.random() * 255)), ((int) (Math.random() * 255))),
            lightConfiguration.getModelIdentifier(),
            lightConfiguration.getSwVersion());

        LightState lightState = new LightState();
        lightState.setBrightness(255);
        lightState.setXY(color.getXY().x, color.getXY().y);
        light.updateState(lightState);
        //LightState newState = b.getBridgeState().getLight(id).getLightState().setHue((int) (Math.random() * MAX_HUE));
        b.getBridgeState().getLightPoint(id).updateState(lightState);

    }

    private static void setLightLocation(Bridge bridge, String id, double x, double y) {
        GroupLightLocation location = new GroupLightLocation();
        location.setLightIdentifier(id);
        location.setX(x);
        location.setY(y);

        Group entertainmentGroup = getEntertainmentGroup(bridge);
//        entertainmentGroup.removeLightLocation(id);
        entertainmentGroup.addLightLocation(location);
//        entertainmentGroup.removeLight(bridge.getBridgeState().getLightPoint(id));
//        bridge.getBridgeState().getGroup(entertainmentGroup.getIdentifier())
    }

    private static void printEntertainmentGroupLightLocations(Bridge bridge) {
        Group g = getEntertainmentGroup(bridge);
        for (GroupLightLocation loc : g.getLightLocations()) {
            System.out.println(loc.getLightIdentifier() + ": " + loc.getX() + ", " + loc.getY());
        }
    }

    private static Group getEntertainmentGroup(Bridge bridge) {
        Group entertainmentGroup = null;
        for (Group group : bridge.getBridgeState().getGroups()) {
            if (group.getGroupType() == GroupType.ENTERTAINMENT) {
                return group;
            }
        }
        return null;
    }



}
