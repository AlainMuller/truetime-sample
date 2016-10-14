package fr.alainmuller.truetimesample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.instacart.library.truetime.TrueTime;
import com.instacart.library.truetime.extensionrx.TrueTimeRx;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    @Bind(R.id.tt_btn_refresh)
    Button refreshBtn;
    @Bind(R.id.tt_time_utc)
    TextView timeUTC;
    @Bind(R.id.tt_time_gmt)
    TextView timeGMT;
    @Bind(R.id.tt_time_pst)
    TextView timePST;
    @Bind(R.id.tt_time_device)
    TextView timeDeviceTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        refreshBtn.setEnabled(false);

        List<String> ntpHosts = Arrays.asList("time.apple.com",
                "0.north-america.pool.ntp.org",
                "1.north-america.pool.ntp.org",
                "2.north-america.pool.ntp.org",
                "3.north-america.pool.ntp.org",
                "0.us.pool.ntp.org",
                "1.us.pool.ntp.org");
        //TrueTimeRx.clearCachedInfo(this);

        TrueTimeRx.build()
                .withConnectionTimeout(31_428)
                .withRetryCount(100)
                .initialize(ntpHosts)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Date>() {
                    @Override
                    public void call(Date date) {
                        onBtnRefresh();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "something went wrong when trying to initialize TrueTime", throwable);
                    }
                }, new Action0() {
                    @Override
                    public void call() {
                        refreshBtn.setEnabled(true);
                    }
                });
    }

    @OnClick(R.id.tt_btn_refresh)
    public void onBtnRefresh() {
        if (!TrueTimeRx.isInitialized()) {
            Toast.makeText(this, "Sorry TrueTime not yet initialized.", Toast.LENGTH_SHORT).show();
            return;
        }

        Date trueTime = TrueTime.now();
        Date deviceTime = new Date();

        Log.d("kg",
                String.format(" [trueTime: %d] [devicetime: %d] [drift_sec: %f]",
                        trueTime.getTime(),
                        deviceTime.getTime(),
                        (trueTime.getTime() - deviceTime.getTime()) / 1000F));

        timeUTC.setText(getString(R.string.tt_time_utc,
                _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("UTC"))));
        timeGMT.setText(getString(R.string.tt_time_gmt,
                _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT+02:00"))));
        timePST.setText(getString(R.string.tt_time_pst,
                _formatDate(trueTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone("GMT-07:00"))));
        timeDeviceTime.setText(getString(R.string.tt_time_device,
                _formatDate(deviceTime, "yyyy-MM-dd HH:mm:ss", TimeZone.getDefault())));
    }

    private String _formatDate(Date date, String pattern, TimeZone timeZone) {
        DateFormat format = new SimpleDateFormat(pattern, Locale.ENGLISH);
        format.setTimeZone(timeZone);
        return format.format(date);
    }
}