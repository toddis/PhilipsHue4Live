public class Main {

    public static void main(String[] args) throws Exception {
        HueConnectorEntertainment h = new HueConnectorEntertainment();
//        h.bridge.getBridgeConfiguration();
//        h.drawGroup2(0, 150, 0, true, 254, 254, 254);
//        Thread.sleep(1000);
//        h.randomizeLights();
        Thread.sleep(5000);
        h.colorAll();

        Thread.sleep(1000);
        // h.setColorForArea(-1, 1, 1, -1, 10,0,0,30);
        for (int i = 0; i < 20; i++) {
            h.strobeRandomLight(0.15);
            Thread.sleep(1000);
        }

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

        Thread.sleep(10000);
    }

}
