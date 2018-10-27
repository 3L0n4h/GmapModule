package com.student.googlemodule_n;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//Parser class for searching location
public class DataParser {
    private HashMap<String, String> getSinglePlace(JSONObject googlePlaceJSON){
        HashMap<String, String> googlePlaceMap = new HashMap<>();  //one hashMap = one place
        String NameOfPlace = "-NA-";
        String vicinity = "-NA-";
        String latitude = "";
        String longitude = "";
        String reference = "";

        try {
            if(!googlePlaceJSON.isNull("name")){
                NameOfPlace = googlePlaceJSON.getString("name");
            }
            if(!googlePlaceJSON.isNull("vicinity")){
                NameOfPlace = googlePlaceJSON.getString("vicinity");
            }

            latitude=googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lat");
            longitude=googlePlaceJSON.getJSONObject("geometry").getJSONObject("location").getString("lng");
            reference = googlePlaceJSON.getString("reference");

            googlePlaceMap.put("place_name", NameOfPlace);
            googlePlaceMap.put("vicinity", vicinity);
            googlePlaceMap.put("lat", latitude);
            googlePlaceMap.put("lng", longitude);
            googlePlaceMap.put("reference", reference);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return googlePlaceMap;
    }

    private List<HashMap<String,String>> getAllNearby(JSONArray jsonArray){

        int counter= jsonArray.length();
        List<HashMap<String,String>> NearbyPlacesList = new ArrayList<>();

        HashMap<String,String> NearbyPlaceMap =null;

        for (int i =0; i<counter;i++ ){

            try {
                NearbyPlaceMap = getSinglePlace((JSONObject) jsonArray.get(i));
                NearbyPlacesList.add(NearbyPlaceMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return NearbyPlacesList;
    }


    public List<HashMap<String,String>> parse(String jSONdata){

        JSONArray jsonArray=null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jSONdata);
            jsonArray= jsonObject.getJSONArray("results");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return getAllNearby(jsonArray);

    }



    //for routes
    public String[] parseDirections(String jsonData)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;

        try {
            jsonObject = new JSONObject(jsonData);
            jsonArray = jsonObject.getJSONArray("routes").getJSONObject(0).getJSONArray("legs").getJSONObject(0).getJSONArray("steps");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return getPaths(jsonArray);
    }

    public String[] getPaths(JSONArray googleStepsJson ) //get all paths
    {
        int count = googleStepsJson.length();
        if (count==0){
            Log.i("getPaths ", " no paths retreived.");

        }
            String[] polylines = new String[count];

            for(int i = 0;i<count;i++)
            {
                try {
                    polylines[i] = getPath(googleStepsJson.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            return polylines;
    }


    public String getPath(JSONObject googlePathJson)
    {
        String polyline = "";
        try {
            polyline = googlePathJson.getJSONObject("polyline").getString("points");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyline;
    }



    private HashMap<String,String> getDuration(JSONArray googleDirectionsJson)
    {
        HashMap<String,String> googleDirectionsMap = new HashMap<>();
        String duration = "";
        String distance ="";


        try {

            duration = googleDirectionsJson.getJSONObject(0).getJSONObject("duration").getString("text");
            distance = googleDirectionsJson.getJSONObject(0).getJSONObject("distance").getString("text");

            googleDirectionsMap.put("duration" , duration);
            googleDirectionsMap.put("distance", distance);

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return googleDirectionsMap;
    }
    //for routes
}
