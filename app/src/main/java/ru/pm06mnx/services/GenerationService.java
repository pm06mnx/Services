package ru.pm06mnx.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import java.util.concurrent.Exchanger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.Nullable;
import io.reactivex.Flowable;

/**
 * Генерирующий бесконечную последовательность сервис
 */
public class GenerationService extends Service {

    public static final String TAG = "GenerationService";
    private IBinder binder;
    private Exchanger<String> exchanger;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    public static Intent newIntent(Context context) {
        return new Intent(context, GenerationService.class);
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "Создание сервиса");
        super.onCreate();
        exchanger = new Exchanger<>();
        binder = new LocalBinder(exchanger);
        startNewGenerator();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "Кто то подключился");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.i(TAG, "Кто то отключился");
        return super.onUnbind(intent);
    }

    @Override
    public void onRebind(Intent intent) {
        Log.i(TAG, "Кто то переподключился");
        super.onRebind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Остановка сервиса");
        executorService.shutdown();
        super.onDestroy();
    }

    private void startNewGenerator() {
        Log.i(TAG, "Старт генератора");
        executorService.submit(() -> {
            int i = 0;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Log.i(TAG, "Сгенерировано значение " + ++i);
                    exchanger.exchange(String.valueOf(i));
                } catch (Exception e) {
                    Log.e(TAG, "Ошибка генерации значения "+i, e);
                }
            }
        });
    }

    /**
     * Биндер для сервиса генерации
     */
    public static class LocalBinder extends Binder {

        private final Exchanger<String> exchanger;

        private LocalBinder(Exchanger<String> exchanger) {
            this.exchanger = exchanger;
        }

        /**
         * @return бесконечный поток сгенерированных сервисом значений
         */
        public Flowable<String> getObservable() {
            return Flowable.generate(() -> 1, (state, emitter) -> {
                String value = exchanger.exchange(null);
                emitter.onNext(value);
                Log.i("Generator", String.format("Получено из сервиса и отправлено в излучатель %s (такт %s)", value, state));
                return ++state;
            });
        }
    }
}
