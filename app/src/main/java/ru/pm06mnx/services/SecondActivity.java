package ru.pm06mnx.services;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.concurrent.TimeUnit;

public class SecondActivity extends AppCompatActivity {

    public static final int SEQ_SIZE = 10;

    private TextView textView;
    private View button;
    private ServiceConnection serviceConnection;
    private Subscription subscription;

    public static Intent newIntent(Context context) {
        return new Intent(context, SecondActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        textView = findViewById(R.id.text_view);
        button = findViewById(R.id.get_it_more_button);

        button.setOnClickListener(v -> {
            getNextSequenceElements();
        });

        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onSubscribe(Subscription s) {
                Log.i("Observer", "Подписчик подключен");
                subscription = s;
                getNextSequenceElements();
            }

            @Override
            public void onNext(String s) {
                Log.i("Observer", "Выводим на экран " + s);
                textView.append(" ");
                textView.append(s);
            }

            @Override
            public void onError(Throwable e) {
                Log.i("Observer", "Ошибка", e);
            }

            @Override
            public void onComplete() {
                Log.i("Observer", "Завершение потока");
            }
        };
        serviceConnection =  new SecondActivityServiceConnection(subscriber);
    }

    private void getNextSequenceElements() {
        if (subscription != null) {
            subscription.request(SEQ_SIZE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindService(GenerationService.newIntent(this), serviceConnection, BIND_WAIVE_PRIORITY | BIND_ALLOW_OOM_MANAGEMENT);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindService(serviceConnection);
    }


    private static class SecondActivityServiceConnection implements ServiceConnection {

        private final Subscriber<String> subscriber;

        private SecondActivityServiceConnection(Subscriber<String> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i("SecondActivityServiceConnection", "Подключились к сервису");
            Flowable<String> observable = ((GenerationService.LocalBinder) service).getObservable();
            observable
                    .observeOn(Schedulers.trampoline(), false, 1)
                    .zipWith(Observable.interval(3, TimeUnit.SECONDS).toFlowable(BackpressureStrategy.ERROR), (item, interval) -> {
                        Log.i("Flowable", "Обработано значение "+item);
                        return item;
                    })
                    .subscribe(subscriber);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i("SecondActivityServiceConnection", "Отключились от сервиса");
        }
    }
}
