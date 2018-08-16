import java.util.Timer;
import java.util.TimerTask;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

public class MaxPhilipsHueEntertainmentObject extends MaxObject {
  public static final String HUE_STORAGE_LOCATION = "/Users/tmaegerle/Music/Ableton/User Library/Presets/Instruments/Max Instrument/NewHueSdkMaxStorage";
  private HueConnectorEntertainment hue;

  // inlets
  Number[] inletVals;
  private enum Inlet {
    DUMMY,
    TOP_LEFT_X,
    TOP_LEFT_Y,
    BOTTOM_RIGHT_X,
    BOTTOM_RIGHT_Y,
    BRIGHTNESS,
    RED,
    GREEN,
    BLUE,
    RENDER,
    ANIMATION_TOP_LEFT_X,
    ANIMATION_TOP_LEFT_Y,
    ANIMATION_BOTTOM_RIGHT_X,
    ANTIMATION_BOTTOM_RIGHT_Y,
    ANIMATION_TIME,
    ANIMATION_TYPE,
    ANIMATION_BRIGHTNESS,
    ANIMATION_RED,
    ANIMATION_GREEN,
    ANIMATION_BLUE
  }

  private static final int[] INLET_TYPES = new int[] {
      DataTypes.INT,
      DataTypes.FLOAT,
      DataTypes.FLOAT,
      DataTypes.FLOAT,
      DataTypes.FLOAT,
      DataTypes.INT,
      DataTypes.INT,
      DataTypes.INT,
      DataTypes.INT,
      DataTypes.INT,
      DataTypes.FLOAT,
      DataTypes.FLOAT,
      DataTypes.FLOAT,
      DataTypes.FLOAT,
      DataTypes.FLOAT,
      DataTypes.INT,
      DataTypes.INT,
      DataTypes.INT,
      DataTypes.INT,
      DataTypes.INT,
  };

  public MaxPhilipsHueEntertainmentObject() {
    hue = new HueConnectorEntertainment();
    inletVals = new Number[INLET_TYPES.length];
    // Set default values
    for (int i = 0; i < INLET_TYPES.length; i++)
      inletVals[i] = 0;

    declareInlets(INLET_TYPES);
    declareOutlets(NO_OUTLETS);
    Timer timer = new Timer();
    timer.schedule(new Render(), 10000, 40);
  }

//  public void inlet(int input) {
//    int inletIndex = getInlet();
//    System.out.println("B: " + input);
//
//    inletVals[inletIndex] = input;
//  }

  public void inlet(float input) {
    int inletIndex = getInlet();
    inletVals[inletIndex] = input;
  }

  public void bang() {
//    System.out.printf("time: %.2f, r: %d, g: %d, b: %d, bri: %d",
//        inletVals[Inlet.ANIMATION_TIME.ordinal()],
//        inletVals[Inlet.ANIMATION_RED.ordinal()],
//        inletVals[Inlet.ANIMATION_GREEN.ordinal()],
//        inletVals[Inlet.ANIMATION_BLUE.ordinal()],
//        inletVals[Inlet.ANIMATION_BRIGHTNESS.ordinal()]
//        );
    hue.playExplosionEffect(
        inletVals[Inlet.ANIMATION_TIME.ordinal()].floatValue(),
        inletVals[Inlet.ANIMATION_RED.ordinal()].intValue(),
        inletVals[Inlet.ANIMATION_GREEN.ordinal()].intValue(),
        inletVals[Inlet.ANIMATION_BLUE.ordinal()].intValue(),
        inletVals[Inlet.ANIMATION_BRIGHTNESS.ordinal()].intValue()
    );
  }

  private class Render extends TimerTask {
    public void run() {
      if (inletVals[Inlet.RENDER.ordinal()].intValue() == 1) {
        hue.setColorForArea(
            inletVals[Inlet.TOP_LEFT_X.ordinal()].floatValue(),
            inletVals[Inlet.TOP_LEFT_Y.ordinal()].floatValue(),
            inletVals[Inlet.BOTTOM_RIGHT_X.ordinal()].floatValue(),
            inletVals[Inlet.BOTTOM_RIGHT_Y.ordinal()].floatValue(),
            inletVals[Inlet.RED.ordinal()].intValue(),
            inletVals[Inlet.GREEN.ordinal()].intValue(),
            inletVals[Inlet.BLUE.ordinal()].intValue(),
            inletVals[Inlet.BRIGHTNESS.ordinal()].intValue()
        );
      }
    }
  }
}
