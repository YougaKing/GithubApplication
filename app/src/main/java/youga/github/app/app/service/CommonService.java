package youga.github.app.app.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import com.hwangjr.rxbus.RxBus;
import com.orhanobut.logger.Logger;

import io.reactivex.Observable;
import youga.github.app.app.NetworkState;

import static android.net.ConnectivityManager.TYPE_WIFI;
import static android.net.ConnectivityManager.TYPE_MOBILE;

public class CommonService extends Service {

    CommonReceiver mReceiver = new CommonReceiver();

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    public static class CommonReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            switch (intent.getAction()) {
                case ConnectivityManager.CONNECTIVITY_ACTION:
                    dispatchConnectivity(context);
                    break;
            }
        }


        private void dispatchConnectivity(Context context) {
            NetworkState state = new NetworkState();
            ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Network[] networks = manager.getAllNetworks();
                Observable.fromArray(networks)
                        .subscribe(network -> {
                            NetworkInfo networkInfo = manager.getNetworkInfo(network);
                            state.setConnected(state.isConnected() || networkInfo.isConnected());
                            if (networkInfo.getType() == TYPE_WIFI && state.isConnected()) {
                                state.setType(TYPE_WIFI);
                            } else if (state.isConnected() && state.getType() != TYPE_WIFI) {
                                state.setType(TYPE_MOBILE);
                            }
                        });
            } else {
                NetworkInfo wifiNetwork = manager.getNetworkInfo(TYPE_WIFI);
                NetworkInfo mobileNetwork = manager.getNetworkInfo(TYPE_MOBILE);
                if (wifiNetwork.isConnected() || mobileNetwork.isConnected()) {
                    state.setConnected(true);
                    state.setType(wifiNetwork.isConnected() ? TYPE_WIFI : TYPE_MOBILE);
                }
            }
            Logger.d(state);
            RxBus.get().post(ConnectivityManager.CONNECTIVITY_ACTION, state);
        }
    }


}
