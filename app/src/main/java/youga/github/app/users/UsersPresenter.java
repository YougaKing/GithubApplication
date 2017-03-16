package youga.github.app.users;


import android.net.ConnectivityManager;
import android.util.Log;

import com.hwangjr.rxbus.RxBus;
import com.hwangjr.rxbus.annotation.Subscribe;
import com.hwangjr.rxbus.annotation.Tag;
import com.hwangjr.rxbus.thread.EventThread;
import com.orhanobut.logger.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import youga.github.app.api.ApiClient;
import youga.github.app.api.ApiStores;
import youga.github.app.app.NetworkState;
import youga.github.app.bean.Repository;
import youga.github.app.bean.User;

/**
 * Created by YougaKing on 2017/3/3.
 */

public class UsersPresenter implements UsersContract.Presenter {

    CompositeDisposable mDisposable;
    ApiStores mApiStores;
    UsersContract.View mView;
    static Map<String, User> USER_MAP = new HashMap<>();

    public UsersPresenter(@NonNull UsersContract.View view) {
        mView = view;
        mApiStores = ApiClient.retrofit().create(ApiStores.class);
        mDisposable = new CompositeDisposable();
        mView.setPresenter(this);
    }

    @Subscribe(
            thread = EventThread.MAIN_THREAD,
            tags = {@Tag(ConnectivityManager.CONNECTIVITY_ACTION)}
    )
    public void caughtConnectivity(NetworkState state) {
        if (state.isConnected()) {
            Observable.fromIterable(USER_MAP.keySet())
                    .subscribe(s -> getRepository(USER_MAP.get(s)));
        }
    }

    @Override
    public void subscribe() {
        RxBus.get().register(this);
    }

    @Override
    public void unSubscribe() {
        USER_MAP.clear();
        mDisposable.clear();
        RxBus.get().unregister(this);
    }

    @Override
    public void searchUsers(String terms) {
        unSubscribe();
        mView.setLoadingIndicator(true);
        mDisposable.add(mApiStores.searchUsers(terms)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userForm -> {
                            if (userForm.getItems() == null || userForm.getItems().isEmpty()) {
                                mView.showNoUsers();
                            } else {
                                mView.showUsers(userForm.getItems());
                            }
                        },
                        throwable -> mView.showLoadingUsersError(throwable.getLocalizedMessage()),
                        () -> mView.setLoadingIndicator(false)));
    }

    @Override
    public void getRepository(@NonNull User user) {
        if (USER_MAP.containsKey(user.getLogin())) return;
        USER_MAP.put(user.getLogin(), user);
        mDisposable.add(mApiStores.getRepositories(user.getLogin())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(repositories -> {
                            user.setRepositories(repositories);
                            Map<String, Integer> map = new HashMap<>();
                            Observable.fromIterable(repositories)
                                    .filter(repository -> repository.getLanguage() != null)
                                    .map(Repository::getLanguage)
                                    .subscribe(s -> {
                                        if (map.get(s) != null) {
                                            map.put(s, map.get(s) + 1);
                                        } else {
                                            map.put(s, 1);
                                        }
                                    });
                            Observable.fromIterable(map.keySet())
                                    .scan((s, s2) -> map.get(s) > map.get(s2) ? s : s2)
                                    .lastElement()
                                    .subscribe(user::setReference_language);
                            if (user.getRepositories().isEmpty() || user.getReference_language().isEmpty())
                                user.setReference_language("No Repository");
                            mView.notifyItem(user);
                            USER_MAP.remove(user.getLogin());
                        },
                        throwable -> mView.showLoadingUsersError(throwable.getLocalizedMessage()))
        );
    }
}
