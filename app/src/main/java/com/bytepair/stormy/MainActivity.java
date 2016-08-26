package com.bytepair.stormy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private CurrentWeather mCurrentWeather;

    // Butterknife to handle the views
    @BindView(R.id.timeTextView)    TextView mTimeTextView;
    @BindView(R.id.tempTextView)    TextView mTempTextView;
    @BindView(R.id.humidityValue)   TextView mHumidityValue;
    @BindView(R.id.precipValue)     TextView mPrecipValue;
    @BindView(R.id.summaryTextView) TextView mSummaryTextView;
    @BindView(R.id.iconImageView)   ImageView mIconImageView;
    @BindView(R.id.refreshImageView)ImageView mRefreshImageView;
    @BindView(R.id.progressBar)     ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // bind ButterKnife to this
        ButterKnife.bind(this);

        // hide progress bar when page loads
        mProgressBar.setVisibility(View.INVISIBLE);

        // our latitude and longitude for getting forecast
        final double latitude = 26.370;
        final double longitude = -80.102;

        // handle the refresh button
        mRefreshImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // get updated forecast when refresh button is pressed
                getForecast(latitude, longitude);
            }
        });

        // get the forecast on the first load
        getForecast(latitude, longitude);
    }

    private void getForecast(double latitude, double longitude) {

        // creating the api call
        String apiKey = "14f3a70fc62951568fd19afb0b110589";
        String forecastURL = "https://api.forecast.io/forecast/" + apiKey + "/" + latitude + "," + longitude;


        // check for network availability
        if (isNetworkAvailable()) {

            // show the progress bar and hide refresh button while working
            toggleRefresh();

            // make new http client object
            OkHttpClient client = new OkHttpClient();

            // build a request that client will send to server
            Request request = new Request.Builder()
                    .url(forecastURL)
                    .build();

            // create a call object to handle the request
            Call call = client.newCall(request);
            // puts call in a queue and executes on a worker thread
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Return to ui thread to change images
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    alertUserAboutError();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {

                    // Return to ui thread to change images
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    try {
                        // Log the body of the response as a string
                        String jsonData = response.body().string();
                        Log.v(TAG, jsonData);
                        if (response.isSuccessful()) {
                            // set our member current weather object to be equal to the one
                            // we create and return with the getCurrentDetails method
                            mCurrentWeather = getCurrentDetails(jsonData);

                            // after data is returned from background thread, display on main thread
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });

                        } else {
                            alertUserAboutError();
                        }
                    } catch (IOException e) {
                        // Log exception
                        Log.e(TAG, "Exception caught: ", e);
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON exception caught: ", e);
                    }
                }
            });

        } else {
            // if network is unavailable
            Toast.makeText(this, R.string.network_unavailable, Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.INVISIBLE) {
            mProgressBar.setVisibility(View.VISIBLE);
            mRefreshImageView.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
            mRefreshImageView.setVisibility(View.VISIBLE);
        }
    }

    private void updateDisplay() {
        mTempTextView.setText(mCurrentWeather.getTemperature() + "");
        mTimeTextView.setText("At " + mCurrentWeather.getFormattedTime() + " it will be");
        mHumidityValue.setText(mCurrentWeather.getHumidity() + "");
        mPrecipValue.setText(mCurrentWeather.getPrecipChance() + "%");
        mSummaryTextView.setText(mCurrentWeather.getSummary());
        // use getDrawable to get drawable from the icon id
        Drawable drawable = getResources().getDrawable(mCurrentWeather.getIconId());
        mIconImageView.setImageDrawable(drawable);
    }

    // add throws to handle an exception if it happens
    private CurrentWeather getCurrentDetails(String jsonData) throws JSONException{
        // create a new json object from the returned data
        JSONObject forecast = new JSONObject(jsonData);
        // write the timezone to the log
        String timezone = forecast.getString("timezone");
        Log.i(TAG, "From JSON: " + timezone);

        // get the currently json object from the larger forecast json object
        JSONObject current = forecast.getJSONObject("currently");

        // set all the variables in our new CurrentWeather object
        CurrentWeather currentWeather = new CurrentWeather();
        currentWeather.setIcon(current.getString("icon"));
        currentWeather.setTime(current.getLong("time"));
        currentWeather.setTemperature(current.getDouble("temperature"));
        currentWeather.setHumidity(current.getDouble("humidity"));
        currentWeather.setPrecipChance(current.getDouble("precipProbability"));
        currentWeather.setSummary(current.getString("summary"));
        currentWeather.setTimezone(timezone);

        Log.d(TAG, currentWeather.getFormattedTime());
        Log.d(TAG, "Icon Id: " + currentWeather.getIconId());

        return currentWeather;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        // if network info exists and is connected
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }
        return isAvailable;
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }
}
