
import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

public class MaxPhilipsHueObject {
  public static final String HUE_STORAGE_LOCATION = "/Users/tmaegerle/Music/Ableton/User Library/Presets/Instruments/Max Instrument/NewHueSdkMaxStorage";
}

//public class MaxPhilipsHueObject extends MaxObject {
//  public static final String HUE_STORAGE_LOCATION = "/Users/tmaegerle/Music/Ableton/User Library/Presets/Instruments/Max Instrument/NewHueSdkMaxStorage";
//  private HueConnector hue;
//
//  // inlets
//  int[] inletVals;
//  public enum Inlet {
//    DUMMY,
//    TRANSITION_TIME,
//    GROUP_ID,
//    BRIGHTNESS,
//    RED,
//    GREEN,
//    BLUE,
//    ON
//  }
//
//  public MaxPhilipsHueObject() {
//    hue = new HueConnector();
//    inletVals = new int[8];
//    declareInlets(new int[] {DataTypes.INT, DataTypes.INT, DataTypes.INT, DataTypes.INT, DataTypes.INT, DataTypes.INT, DataTypes.INT, DataTypes.INT, });
//    declareOutlets(NO_OUTLETS);
//  }
//
//  public void inlet(int input) {
//    int inletIndex = getInlet();
//
//    inletVals[inletIndex] = input;
//  }
//
//  public void bang() {
//    //int groupId, int brightness, int transitionTime, boolean on, int red, int green, int blue
//    hue.drawGroup(
//        inletVals[Inlet.GROUP_ID.ordinal()],
//        inletVals[Inlet.BRIGHTNESS.ordinal()],
//        inletVals[Inlet.TRANSITION_TIME.ordinal()],
//        inletVals[Inlet.ON.ordinal()] == 1,
//        inletVals[Inlet.RED.ordinal()],
//        inletVals[Inlet.GREEN.ordinal()],
//        inletVals[Inlet.BLUE.ordinal()]
//    );
//  }
//}
