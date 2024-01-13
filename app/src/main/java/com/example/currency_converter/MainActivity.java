package com.example.currency_converter;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private Spinner fromCurrencySpinner, toCurrencySpinner;
    private Button convertButton;
    private String fromCurrency, toCurrency;
    private TextView resultTextView;

    private TextView resultLabelTextView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fromCurrencySpinner = findViewById(R.id.fromCurrencySpinner);
        toCurrencySpinner = findViewById(R.id.toCurrencySpinner);
        convertButton = findViewById(R.id.convertButton);
        resultTextView = findViewById(R.id.resultTextView);
        resultLabelTextView = findViewById(R.id.resultLabelTextView);


        fromCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                fromCurrency = parent.getItemAtPosition(position).toString().split(" - ")[0];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        toCurrencySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                toCurrency = parent.getItemAtPosition(position).toString().split(" - ")[0];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        convertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!fromCurrency.equals(toCurrency)) {
                    EditText amountInput = findViewById(R.id.amountInput);
                    String amountStr = amountInput.getText().toString().trim();

                    if (!amountStr.isEmpty()) {
                        double amount = Double.parseDouble(amountStr);
                        fetchExchangeRate(fromCurrency, toCurrency, amount);
                        resultLabelTextView.setVisibility(View.VISIBLE);

                    } else {
                        Toast.makeText(MainActivity.this, "Enter a valid amount", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Choose different currencies", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void fetchExchangeRate(String fromCurrency, String toCurrency, double amount) {
        Retrofit retrofit = RetrofitClient.getClient();
        ExchangeRateService service = retrofit.create(ExchangeRateService.class);

        Call<ExchangeRateResponse> call = service.getExchangeRates("1357a90803948cc2f888d46c", fromCurrency);
        call.enqueue(new Callback<ExchangeRateResponse>() {
            @Override
            public void onResponse(Call<ExchangeRateResponse> call, Response<ExchangeRateResponse> response) {
                if (response.isSuccessful()) {
                    ExchangeRateResponse exchangeRateResponse = response.body();
                    if (exchangeRateResponse != null) {
                        Log.d("API_RESPONSE", "Successful Response: " + exchangeRateResponse.toString());
                        Map<String, Double> rates = exchangeRateResponse.getRates();
                        if (rates != null) {
                            Double rate = rates.get(toCurrency);
                            if (rate != null) {
                                EditText amountInput = findViewById(R.id.amountInput);
                                double amount = Double.parseDouble(amountInput.getText().toString());
                                double convertedAmount = amount * rate;
                                resultTextView.setText(String.format(Locale.US, "%.2f %s", convertedAmount, toCurrency));
                            } else {
                                Log.e("API_RESPONSE", "Conversion rate not available for " + toCurrency);
                                showError("Conversion rate not available for " + toCurrency);
                            }
                        } else {
                            Log.e("API_RESPONSE", "Rates not available in the response");
                            showError("Rates not available in the response");
                        }
                    } else {
                        Log.e("API_RESPONSE", "Invalid response body");
                        showError("Invalid response body");
                    }
                } else {
                    // Log the error response details
                    Log.e("API_RESPONSE", "Error Response Code: " + response.code());
                    try {
                        Log.e("API_RESPONSE", "Error Response Body: " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    showError("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ExchangeRateResponse> call, Throwable t) {
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void showError(String message) {
        Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show();
        resultTextView.setText("");
    }
}
