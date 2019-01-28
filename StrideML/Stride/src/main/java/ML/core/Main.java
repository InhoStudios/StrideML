package ML.core;

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

import static jdk.nashorn.internal.objects.Global.print;

public class Main {


    public static int[] predict (String json) {
        String simpleMlp = "Model-88.hf";
//        try {
//            simpleMlp = new ClassPathResource("Model-88.hf").getFile().getPath();
//        } catch (IOException e) {
//            int[] output = new int[1];
//            output[0] = -1;
//            return output;
//        }
        MultiLayerNetwork model;
        try {
            model = KerasModelImport.importKerasSequentialModelAndWeights(simpleMlp);
        } catch (IOException e) {
            int[] output = new int[1];
            output[0] = -1;
            return output;
        } catch (InvalidKerasConfigurationException e) {
            int[] output = new int[1];
            output[0] = -2;
            return output;
        } catch (UnsupportedKerasConfigurationException e) {
            int[] output = new int[1];
            output[0] = -3;
            return output;
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