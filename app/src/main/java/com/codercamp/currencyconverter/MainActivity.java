package com.codercamp.currencyconverter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.codercamp.currencyconverter.Retrofit.RetrofitBuilder;
import com.codercamp.currencyconverter.Retrofit.RetrofitInterface;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.gson.JsonObject;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    CircularProgressButton button;
    EditText currencyToBeConverted;
    Spinner convertToDropdown;
    Spinner convertFromDropdown;
    TextView currencyConvertedResult;

    private AdView adView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setContentView( R.layout.activity_main);
        //Initialization
        currencyToBeConverted = findViewById(R.id.currency_to_be_converted);
        convertToDropdown = findViewById(R.id.convert_to);
        convertFromDropdown = findViewById(R.id.convert_from);
        currencyConvertedResult = findViewById(R.id.currency_convertedResult);
        button = findViewById(R.id.button);
        adView = (AdView) findViewById(R.id.adView);

        //Adding Functionality
        //String[] dropDownList = {"USD", "INR", "EUR", "NZD", "BDT"};
        String[] dropDownList = getResources().getStringArray(R.array.array_currency_codes);
        String[] dropDownList2 = getResources().getStringArray(R.array.array_currency);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, dropDownList);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, dropDownList2);
        convertToDropdown.setAdapter(adapter2);
        convertFromDropdown.setAdapter(adapter);


        button.setOnClickListener(v -> {

            try {

                if (currencyToBeConverted.getText().toString().isEmpty()) {
                    currencyToBeConverted.setError("Please Enter Amount");
                    currencyToBeConverted.requestFocus();
                    button.stopAnimation();
                } else {
                    button.startAnimation();
                    RetrofitInterface retrofitInterface = RetrofitBuilder.getRetrofitInstance().create(RetrofitInterface.class);
                    Call<JsonObject> call = retrofitInterface.getExchangeCurrency(convertFromDropdown.getSelectedItem().toString());
                    call.enqueue(new Callback<JsonObject>() {
                        @SuppressLint("SetTextI18n")
                        @Override
                        public void onResponse(@NonNull Call<JsonObject> call, @NonNull Response<JsonObject> response) {
                            JsonObject res = response.body();
                            assert res != null;
                            JsonObject rates = res.getAsJsonObject("rates");
                            double currency = Double.parseDouble(currencyToBeConverted.getText().toString());
                            double multiplier = Double.parseDouble(rates.get(convertToDropdown.getSelectedItem().toString()).toString());
                            double result = currency * multiplier;
                            currencyConvertedResult.setVisibility(View.VISIBLE);
                            currencyConvertedResult.setText(currencyToBeConverted.getText().toString() + " " + convertFromDropdown.getSelectedItem().toString() + " = " + result + " " + convertToDropdown.getSelectedItem().toString());
                            //button.stopAnimation();
                            button.revertAnimation();
                        }

                        @Override
                        public void onFailure(@NonNull Call<JsonObject> call, @NonNull Throwable t) {
                           // button.stopAnimation();
                            if (!isConnected()) {
                                Toast.makeText(MainActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                                button.revertAnimation();
                                button.stopAnimation();
                            } else {
                                Toast.makeText(MainActivity.this, "Error" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                button.stopAnimation();
                                button.revertAnimation();
                            }
                            //button.stopAnimation();
                            //button.revertAnimation();
                        }
                    });
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
        loadAds();

    }

    public boolean isConnected() {
        boolean connected;
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            //Log.e("Connectivity Exception", e.getMessage());
        }
        return false;
    }

    @Override
    protected void onResume() {
        if (adView != null) {
            adView.resume();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (adView != null) {
            adView.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
        super.onDestroy();
    }

    private void loadAds() {
        // Initialize the Mobile Ads SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(@NonNull InitializationStatus initializationStatus) {
                Log.d("successful", "successful");
            }
        });
        AdRequest adRequest = new AdRequest.Builder().build();
        if (adView != null) {
            adView.loadAd(adRequest);
        }

    }
}