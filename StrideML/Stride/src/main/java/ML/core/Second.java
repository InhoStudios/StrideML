package ML.core;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;

public class Second {

    ArrayList<Acceleration> accelerations = new ArrayList<Acceleration>();

    public Second() {
    }

    public static double[][][] fromJson(String json){
        JsonObject jsonObject = (new JsonParser()).parse(json).getAsJsonObject();
        JsonArray data = jsonObject.get("data").getAsJsonArray();

        double[][][] output =  new double[data.size()][4][4];

        int i = 0;
        int secondIndex = 0;
        double[][] s = new double[4][4];
        for(JsonElement a : data){
            if(i < 4){
                Acceleration acceleration = Acceleration.fromJson(a.toString());
//                System.out.println(accleration.ts);
                s[i] = acceleration.data;
                i++;
            }else{
                output[secondIndex] = s;
                s = new double[4][4];
                secondIndex++;
                i = 0;
            }
        }
        return output;
    }
}