package com.example.user.weather;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;


public class WeatherInfoFragment extends Fragment {
    private static String weatherURL;

    private WeatherData weatherDataInfo;

    private ImageView ivHighClouds;
    private ImageView ivMediumClouds;
    private ImageView ivLowClouds;
    private ImageView ivFog;

    private TextView tvHighCloudsValue;
    private TextView tvMediumCloudsValue;
    private TextView tvLowCloudsValue;
    private TextView tvFogValue;

    private TextView tvDewPoint;
    private TextView tvHumidity;
    private TextView tvTemperature;

    private EditText etEnterLocation;
    private EditText etEnterLatitude;
    private EditText etEnterLongitude;

    private Button find;

    private LinearLayout llContent;
    private LinearLayout llEnterCoordinates;

    private Geolocation lastCoordinates = new Geolocation();

    private Button toggleEnterLocation;
    private boolean isEnterAddress = true; //if true, we read address string, if false, we read coordinates

    public int position; //0 - departure fragment, 1 - destination fragment

    public static Fragment getInstance(int position) {
        Bundle bundle = new Bundle();
        bundle.putInt("pos", position);
        WeatherInfoFragment tabFragment = new WeatherInfoFragment();
        tabFragment.setArguments(bundle);
        return tabFragment;
    }

    public WeatherInfoFragment() {
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        position = getArguments().getInt("pos");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_weather_info, container, false);

        ivHighClouds = rootView.findViewById(R.id.imageHighClouds);
        ivMediumClouds = rootView.findViewById(R.id.ivMediumClouds);
        ivLowClouds = rootView.findViewById(R.id.ivLowClouds);
        ivFog = rootView.findViewById(R.id.ivFog);

        tvHighCloudsValue = rootView.findViewById(R.id.tvHighCloudsValue);
        tvMediumCloudsValue = rootView.findViewById(R.id.tvMediumCloudsValue);
        tvLowCloudsValue = rootView.findViewById(R.id.tvLowCloudsValue);
        tvFogValue = rootView.findViewById(R.id.tvFogValue);

        tvDewPoint = rootView.findViewById(R.id.tvDewPoint);
        tvHumidity = rootView.findViewById(R.id.tvHumidity);
        tvTemperature = rootView.findViewById(R.id.tvTemperature);

        etEnterLocation = rootView.findViewById(R.id.etEnterLocation);
        etEnterLatitude = rootView.findViewById(R.id.etEnterLatitude);
        etEnterLongitude = rootView.findViewById(R.id.etEnterLongitude);

        llContent = rootView.findViewById(R.id.llContent);
        llEnterCoordinates = rootView.findViewById(R.id.llEnterCoordinates);

        //Switching between address and coordinates entering form
        toggleEnterLocation = rootView.findViewById(R.id.btnSwitch);
        toggleEnterLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isEnterAddress){
                    etEnterLocation.setVisibility(View.GONE);
                    llEnterCoordinates.setVisibility(View.VISIBLE);
                    isEnterAddress = false;
                } else{
                    etEnterLocation.setVisibility(View.VISIBLE);
                    llEnterCoordinates.setVisibility(View.GONE);
                    isEnterAddress = true;
                }
            }
        });


        find = (Button) rootView.findViewById(R.id.btnFind);
        find.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Gets coordinates on address/coordinates we entered and also checks validity of entry
                Geolocation coordinates = getCoordinatesForURL();

                if(coordinates != null) {

                    weatherURL = "https://api.met.no/weatherapi/locationforecast/1.9/?lat=" + coordinates.getLatitude() + ";lon=" + coordinates.getLongitude();

                    //Geting last departure/destination weather data (if exists)
                    SharedPreferences mPrefs = getActivity().getPreferences(MODE_PRIVATE);

                    String dataName = (position == 0) ? "departureData" : "destinationData";

                    Gson gson = new Gson();
                    String json = mPrefs.getString(dataName, "");
                    weatherDataInfo = gson.fromJson(json, WeatherData.class);

                    Date now = new Date();

                    //If there is weather data in cache but not the one we entered now
                    if(weatherDataInfo != null && weatherDataInfo.getCoordinates().compareTo(coordinates) == 0){
                        lastCoordinates = coordinates;
                        new XMLParserClass().execute();
                        //If coordinates are the same, if there is data in cache and it's less than a hour old
                    } else if (weatherDataInfo != null && weatherDataInfo.getDateAndTime() != null
                            && now.getTime() - weatherDataInfo.getDateAndTime().getTime() < 60 * 60 * 1000) {
                        updateFragment();
                        //If there is no previuos entries
                    } else {
                        new XMLParserClass().execute();
                    }
                }

            }
        });

        return rootView;
    }

    //Updating fragment data
    public void updateFragment(){

        if(weatherDataInfo != null){
            tvHumidity.setText(getResources().getString(R.string.humidity) + weatherDataInfo.getHumidity() + "%");
            tvDewPoint.setText(getResources().getString(R.string.dew_point) + weatherDataInfo.getDewPoint() + "°");
            tvTemperature.setText(getResources().getString(R.string.temperature) + weatherDataInfo.getTemperature() + "°");

            tvHighCloudsValue.setText(weatherDataInfo.getHighClouds() + "%");
            tvMediumCloudsValue.setText(weatherDataInfo.getMediumClouds() + "%");
            tvLowCloudsValue.setText(weatherDataInfo.getLowClouds() + "%");
            tvFogValue.setText(weatherDataInfo.getFog() + "%");

            setImageView(weatherDataInfo.getFog(), ivFog);
            setImageView(weatherDataInfo.getLowClouds(), ivLowClouds);
            setImageView(weatherDataInfo.getMediumClouds(), ivMediumClouds);
            setImageView(weatherDataInfo.getHighClouds(), ivHighClouds);

            llContent.setVisibility(View.VISIBLE);
        }
    }

    //Sets weather images depending on value percent
    private void setImageView(double value, ImageView image){
        if(value < 5){
            image.setImageDrawable(getResources().getDrawable(R.drawable.sun));
            image.setAlpha(1.0f);
        }else if(value < 20){
            image.setImageDrawable(getResources().getDrawable(R.drawable.cloud1));
            image.setAlpha(0.2f);
        } else if(value < 50){
            image.setImageDrawable(getResources().getDrawable(R.drawable.cloud2));
            image.setAlpha(0.4f);
        } else if(value < 70){
            image.setImageDrawable(getResources().getDrawable(R.drawable.cloud3));
            image.setAlpha(0.6f);
        } else {
            image.setImageDrawable(getResources().getDrawable(R.drawable.cloud4));
            image.setAlpha(0.8f);
        }
    }

    // Async Task class that parses xml data
    private class XMLParserClass extends AsyncTask<String, Void, String> {

        WeatherData currentData = null;

        @Override
        protected String doInBackground(String... params) {

            try {
                currentData = parse(weatherURL);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (XmlPullParserException e) {
                e.printStackTrace();
            }

            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

            if(currentData != null){
                weatherDataInfo = currentData;

                weatherDataInfo.setDateAndTime(new Date());
                weatherDataInfo.setCoordinates(lastCoordinates);

                SharedPreferences mPrefs = getActivity().getPreferences(MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = mPrefs.edit();
                Gson gson = new Gson();
                String json = gson.toJson(weatherDataInfo);

                String dataName = (position == 0) ? "departureData" : "destinationData";

                prefsEditor.putString(dataName, json);
                prefsEditor.commit();

                updateFragment();
            } else{
                llContent.setVisibility(View.GONE);
                Toast.makeText(getContext(), getResources().getString(R.string.loading_failed), Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }

    // Method for parsing xml data
    public WeatherData parse(String url) throws IOException, XmlPullParserException {
        WeatherData weatherData = new WeatherData();

        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser myParser = factory.newPullParser();

        URL input = new URL(url);
        InputStream stream = input.openStream();

        myParser.setInput(stream, null);

        int event = myParser.getEventType();
        String text = null;

        boolean isFinished = false;

        while (event != XmlPullParser.END_DOCUMENT && !isFinished) {
            String name = myParser.getName();

            switch (event) {
                case XmlPullParser.START_TAG:
                    break;
                case XmlPullParser.TEXT:
                    text = myParser.getText();
                    break;

                case XmlPullParser.END_TAG:

                    if (name.equalsIgnoreCase("temperature") && myParser.getAttributeValue(null, "value") != null) {
                        weatherData.setTemperature(Double.parseDouble(myParser.getAttributeValue(null, "value")));
                    } else if (name.equalsIgnoreCase("humidity") && myParser.getAttributeValue(null, "value") != null) {
                        weatherData.setHumidity(Double.parseDouble(myParser.getAttributeValue(null, "value")));
                    } else if (name.equalsIgnoreCase("fog") && myParser.getAttributeValue(null, "percent") != null) {
                        weatherData.setFog(Double.parseDouble(myParser.getAttributeValue(null, "percent")));
                    } else if(name.equalsIgnoreCase("lowClouds") && myParser.getAttributeValue(null, "percent") != null){
                        weatherData.setLowClouds(Double.parseDouble(myParser.getAttributeValue(null, "percent")));
                    } else if(name.equalsIgnoreCase("mediumClouds") && myParser.getAttributeValue(null, "percent") != null){
                        weatherData.setMediumClouds(Double.parseDouble(myParser.getAttributeValue(null, "percent")));
                    } else if(name.equalsIgnoreCase("highClouds") && myParser.getAttributeValue(null, "percent") != null){
                        weatherData.setHighClouds(Double.parseDouble(myParser.getAttributeValue(null, "percent")));
                    } else if(name.equalsIgnoreCase("dewpointTemperature") && myParser.getAttributeValue(null, "value") != null){
                        weatherData.setDewPoint(Double.parseDouble(myParser.getAttributeValue(null, "value")));
                        isFinished = true;
                    }
//                    else if(name.equalsIgnoreCase("location")){
//                        isFinished = true;
//                    }
                    break;
            }

            event = myParser.next();
        }
        return weatherData;
    }

    public Geolocation getCoordinatesFromAddress(String address){
        Geolocation gl = new Geolocation();

        Geocoder geocoder = new Geocoder(getActivity());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocationName(address, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(addresses.size() > 0) {
            gl.setLatitude(addresses.get(0).getLatitude());
            gl.setLongitude(addresses.get(0).getLongitude());
        } else{
            return null;
        }

        return gl;
    }

    private Geolocation getCoordinatesForURL(){

        Geolocation coordinates = new Geolocation();

        String enteredLocation = "";
        double latitude = 200;
        double longitude = 200;

        if(isEnterAddress) {
            enteredLocation = etEnterLocation.getText().toString();

            if(!enteredLocation.equals("")){
                coordinates = getCoordinatesFromAddress(enteredLocation);

                if(coordinates == null){
                    Toast.makeText(getContext(), getResources().getString(R.string.incorrect_address), Toast.LENGTH_SHORT).show();
                    return null;
                } else{
                    return coordinates;
                }
            } else{
                Toast.makeText(getContext(), getResources().getString(R.string.no_location), Toast.LENGTH_SHORT).show();
                return null;
            }
        } else{
            if(!etEnterLatitude.getText().toString().equals("") && !etEnterLongitude.getText().toString().equals("")) {
                latitude = Double.parseDouble(etEnterLatitude.getText().toString());
                longitude = Double.parseDouble(etEnterLongitude.getText().toString());

                if(latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180){
                    Toast.makeText(getContext(), getResources().getString(R.string.incorrect_coordinates), Toast.LENGTH_SHORT).show();
                    return null;
                } else{
                    coordinates.setLatitude(latitude);
                    coordinates.setLongitude(longitude);
                    return coordinates;
                }
            } else{
                Toast.makeText(getContext(), getResources().getString(R.string.no_coordinates), Toast.LENGTH_SHORT).show();
                return null;
            }
        }

    }


}
