package com.nwhacks.strideml.MLCore;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Acceleration {

    public double[] data;

    public Acceleration(long ts, double x, double y, double z) {
        data = new double[4];
        data[0] = ts;
        data[1] = x;
        data[2] = y;
        data[3] = z;
    }

    public static Acceleration fromJson(String json){
        JsonObject jsonObject = (new JsonParser()).parse(json).getAsJsonObject();
        return new Acceleration(jsonObject.get("ts").getAsLong(),jsonObject.get("z").getAsDouble(),jsonObject.get("y").getAsDouble(),jsonObject.get("z").getAsDouble());
    }
}