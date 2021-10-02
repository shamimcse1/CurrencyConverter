package codercamp.com.currencyconverter;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;

import br.com.simplepass.loadingbutton.customViews.CircularProgressButton;
import codercamp.com.currencyconverter.Retrofit.RetrofitBuilder;
import codercamp.com.currencyconverter.Retrofit.RetrofitInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
    CircularProgressButton button;
    EditText currencyToBeConverted;
    Spinner convertToDropdown;
    Spinner convertFromDropdown;
    TextView currencyConvertedResult;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initialization
        currencyToBeConverted = (EditText) findViewById(R.id.currency_to_be_converted);
        convertToDropdown = (Spinner) findViewById(R.id.convert_to);
        convertFromDropdown = (Spinner) findViewById(R.id.convert_from);
        currencyConvertedResult = findViewById(R.id.currency_convertedResult);
        button = findViewById(R.id.button);


        //Adding Functionality
        //String[] dropDownList = {"USD", "INR", "EUR", "NZD", "BDT"};
        String[] dropDownList = getResources().getStringArray(R.array.array_currency_codes);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.support_simple_spinner_dropdown_item, dropDownList);
        convertToDropdown.setAdapter(adapter);
        convertFromDropdown.setAdapter(adapter);


        button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (currencyToBeConverted.getText().toString().isEmpty()) {
                    currencyToBeConverted.setError("Please Enter Value");
                    currencyToBeConverted.requestFocus();
                    button.stopAnimation();
                } else {
                    button.startAnimation();
                    RetrofitInterface retrofitInterface = RetrofitBuilder.getRetrofitInstance().create(RetrofitInterface.class);
                    Call<JsonObject> call = retrofitInterface.getExchangeCurrency(convertFromDropdown.getSelectedItem().toString());
                    call.enqueue(new Callback<JsonObject>() {
                        @Override
                        public void onResponse(Call<JsonObject> call, Response<JsonObject> response) {
                            JsonObject res = response.body();
                            assert res != null;
                            JsonObject rates = res.getAsJsonObject("rates");
                            double currency = Double.valueOf(currencyToBeConverted.getText().toString());
                            double multiplier = Double.valueOf(rates.get(convertToDropdown.getSelectedItem().toString()).toString());
                            double result = currency * multiplier;
                            currencyConvertedResult.setVisibility(View.VISIBLE);
                            currencyConvertedResult.setText(String.valueOf(currencyToBeConverted.getText().toString() + " " + convertFromDropdown.getSelectedItem().toString() + " = " + result + " " + convertToDropdown.getSelectedItem().toString()));
                            button.stopAnimation();
                            button.revertAnimation();
                        }

                        @Override
                        public void onFailure(Call<JsonObject> call, Throwable t) {
                            button.stopAnimation();
                            if (!isConnected()) {
                                Toast.makeText(MainActivity.this, "Please Check Your Internet Connection", Toast.LENGTH_SHORT).show();
                                button.stopAnimation();
                                button.revertAnimation();
                            } else {
                                Toast.makeText(MainActivity.this, "Error" + t.getMessage(), Toast.LENGTH_SHORT).show();
                                button.stopAnimation();
                                button.revertAnimation();

                            }
                        }
                    });
                }


            }
        });


    }

    public boolean isConnected() {
        boolean connected = false;
        try {
            ConnectivityManager cm = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nInfo = cm.getActiveNetworkInfo();
            connected = nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
            return connected;
        } catch (Exception e) {
            //Log.e("Connectivity Exception", e.getMessage());
        }
        return connected;
    }
}