package com.nwhacks.strideml.MLCore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.deeplearning4j.nn.modelimport.keras.KerasModelImport;
import org.deeplearning4j.nn.modelimport.keras.exceptions.InvalidKerasConfigurationException;
import org.deeplearning4j.nn.modelimport.keras.exceptions.UnsupportedKerasConfigurationException;
import org.deeplearning4j.nn.multilayer.*;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.io.ClassPathResource;
import org.nd4j.linalg.learning.config.*; // for different updaters like Adam, Nesterovs, etc.
import org.nd4j.linalg.activations.Activation; // defines different activation functions like RELU, SOFTMAX, etc.
import org.nd4j.linalg.lossfunctions.LossFunctions; // mean squared error, multiclass cross entropy, etc.
import org.nd4j.linalg.util.ArrayUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Main {


    public static int[] predict (String json) {
        String fullModel = null;
        try {
            fullModel = new ClassPathResource("Model-88.h5").getFile().getPath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        MultiLayerNetwork model = null;
        try {
            model = KerasModelImport.importKerasSequentialModelAndWeights(fullModel);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKerasConfigurationException e) {
            e.printStackTrace();
        } catch (UnsupportedKerasConfigurationException e) {
            e.printStackTrace();
        }

        JsonObject input = new JsonParser().parse(json).getAsJsonObject();
        double[][][] data = Second.fromJson(json);
        INDArray array = Nd4j.create(data[5]);

        System.out.println(model.predict(array));

        return model.predict(array);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        File tempFile = new File( "Model-88.hf" );
        boolean exists = tempFile.exists();
        System.out.println(exists);
        String json = StringUtil.readFileAsString("andy1.json");
        System.out.println(predict(json)[0]);
    }



}