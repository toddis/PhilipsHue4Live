public class Main {

    public static void main(String[] args) throws Exception {
        HueConnector h = new HueConnector();
        h.bridge.getBridgeConfiguration();
        h.drawGroup2(0, 150, 0, false, 254, 254, 254);
    }

}
