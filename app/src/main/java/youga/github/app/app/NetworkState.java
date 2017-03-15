package youga.github.app.app;

import static android.net.ConnectivityManager.TYPE_WIFI;

/**
 * Created by YougaKing on 2017/3/15.
 */

public class NetworkState {

    private boolean connected;

    private int type;

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return (type == TYPE_WIFI ? "WIFI" : "MOBILE") + (connected ? "已连接" : "断开");
    }
}
