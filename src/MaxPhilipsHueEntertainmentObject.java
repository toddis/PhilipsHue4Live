import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.cycling74.max.DataTypes;
import com.cycling74.max.MaxObject;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

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
    ANIMATION_BOTTOM_RIGHT_Y,
    ANIMATION_TIME,
    ANIMATION_TYPE,
    ANIMATION_BRIGHTNESS,
    ANIMATION_AFFECT_SINGLE_RANDOM_LIGHT,
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
      DataTypes.INT,
  };

  private enum AnimationType {
    STROBE,
    COOLDOWN_STROBE
  }

  private boolean lightsNeedRerender;

  private static final ScheduledExecutorService RENDER_THREAD_POOL = Executors.newScheduledThreadPool(5);
  private static final Executor FX_RENDER_THREAD_POOL = Executors.newFixedThreadPool(5);

  private static final int RENDER_INIT_DELAY_MS = 5000;
  private static final int RENDER_PERIOD_MS = 38;

  public MaxPhilipsHueEntertainmentObject() {
    hue = new HueConnectorEntertainment();
    inletVals = new Number[INLET_TYPES.length];
    // Set default values
    for (int i = 0; i < INLET_TYPES.length; i++)
      inletVals[i] = 0;

    declareInlets(INLET_TYPES);
    declareOutlets(NO_OUTLETS);
    lightsNeedRerender = true;
    scheduleRenderCalls();
  }

//  public void inlet(int input) {
//    int inletIndex = getInlet();
//    System.out.println("B: " + input);
//
//    inletVals[inletIndex] = input;
//  }

  public void inlet(float input) {
    int inletIndex = getInlet();
    updateRenderState(inletIndex);
    inletVals[inletIndex] = input;
  }

  private void updateRenderState(int index) {
    if (
          index == Inlet.TOP_LEFT_X.ordinal() ||
          index == Inlet.TOP_LEFT_Y.ordinal() ||
              index == Inlet.BOTTOM_RIGHT_X.ordinal() ||
              index == Inlet.BOTTOM_RIGHT_Y.ordinal() ||
              index == Inlet.RED.ordinal() ||
              index == Inlet.GREEN.ordinal() ||
              index == Inlet.BLUE.ordinal() ||
              index == Inlet.BRIGHTNESS.ordinal()
        ) {
      lightsNeedRerender = true;
    }
  }

  public void bang() {
    CompletableFuture.runAsync(() -> {
      runAnimation();
    }, FX_RENDER_THREAD_POOL);
  }

  private void runAnimation() {
    boolean singleRandomLight = inletVals[Inlet.ANIMATION_AFFECT_SINGLE_RANDOM_LIGHT.ordinal()].intValue() == 1;
    final AnimationType animationType = AnimationType.values()[inletVals[Inlet.ANIMATION_TYPE.ordinal()].intValue()];
    if (singleRandomLight) {
      switch (animationType) {
        case STROBE:
          hue.strobeRandomLight(
              inletVals[Inlet.ANIMATION_TIME.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_RED.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_GREEN.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_BLUE.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_BRIGHTNESS.ordinal()].intValue()
          );
          break;
        case COOLDOWN_STROBE:
          hue.cooldownStrobeRandomLight(
              inletVals[Inlet.ANIMATION_TIME.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_RED.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_GREEN.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_BLUE.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_BRIGHTNESS.ordinal()].intValue()
          );
          break;
        default:
          break;
      }
    } else {
      switch (animationType) {
        case STROBE:
          hue.strobe(
              inletVals[Inlet.ANIMATION_TIME.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_TOP_LEFT_X.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_TOP_LEFT_Y.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_BOTTOM_RIGHT_X.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_BOTTOM_RIGHT_Y.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_RED.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_GREEN.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_BLUE.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_BRIGHTNESS.ordinal()].intValue()
          );
          break;
        case COOLDOWN_STROBE:
          hue.cooldownStrobe(
              inletVals[Inlet.ANIMATION_TIME.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_TOP_LEFT_X.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_TOP_LEFT_Y.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_BOTTOM_RIGHT_X.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_BOTTOM_RIGHT_Y.ordinal()].floatValue(),
              inletVals[Inlet.ANIMATION_RED.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_GREEN.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_BLUE.ordinal()].intValue(),
              inletVals[Inlet.ANIMATION_BRIGHTNESS.ordinal()].intValue()
          );
          break;
        default:
          break;
      }
    }
  }

  private void printAllInlets() {
    StringBuilder res = new StringBuilder();
    for (int i = 0; i < inletVals.length; i++) {
      res.append((Inlet.values())[i] + ": " + String.format("%.2f", inletVals[i].floatValue()) + "\n");
    }
    System.out.println(res);
  }

  private void scheduleRenderCalls() {
    RENDER_THREAD_POOL.scheduleAtFixedRate(() -> {
        if (inletVals[Inlet.RENDER.ordinal()].intValue() == 1 && lightsNeedRerender) {
          lightsNeedRerender = false;
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
    }, RENDER_INIT_DELAY_MS, RENDER_PERIOD_MS, MILLISECONDS);
  }

}
