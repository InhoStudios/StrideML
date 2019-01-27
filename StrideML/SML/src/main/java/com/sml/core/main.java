package com.sml.core;



import org.deeplearning4j.datasets.iterator.*;
import org.deeplearning4j.datasets.iterator.impl.*;
import org.deeplearning4j.nn.api.*;
import org.deeplearning4j.nn.multilayer.*;
import org.deeplearning4j.nn.graph.*;
import org.deeplearning4j.nn.conf.*;
import org.deeplearning4j.nn.conf.inputs.*;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.weights.*;
import org.deeplearning4j.optimize.listeners.*;
import org.deeplearning4j.datasets.datavec.RecordReaderMultiDataSetIterator;
import org.nd4j.linalg.dataset.DataSet;

import org.nd4j.evaluation.classification.Evaluation;
import org.nd4j.evaluation.classification.ROCMultiClass;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.cpu.nativecpu.NDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.*; // for different updaters like Adam, Nesterovs, etc.
import org.nd4j.linalg.activations.Activation; // defines different activation functions like RELU, SOFTMAX, etc.
import org.nd4j.linalg.lossfunctions.LossFunctions; // mean squared error, multiclass cross entropy, etc.
import org.nd4j.linalg.util.ArrayUtil;
//import org.deeplearning4j.datasets.iterator.impl.EmnistDataSetIterator;

import java.io.IOException;
import java.util.ArrayList;

import static jdk.nashorn.internal.objects.Global.print;

public class main {


    public static void main (String[] args ) {
        System.out.println("Working Directory = " +
                System.getProperty("user.dir"));

        double[][][] andy = null;
        try {
            andy = Second.fromJson(StringUtil.readFileAsString("andy1.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        double[][][] james = null;
        try {
            james = Second.fromJson(StringUtil.readFileAsString("james1.json"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        int[] trainShape = new int[3];
        trainShape[0] = andy.length + james.length;
        trainShape[1] = 4;
        trainShape[2] = 4;

        double[][][] train = new double[andy.length + james.length][4][4];
        for(int i = 0; i < andy.length; i++){
            train[i] = andy[i];
        }
        for(int i = 0; i < james.length; i++){
            train[i + andy.length] = james[i];
        }


        INDArray trainData = Nd4j.create(ArrayUtil.flattenDoubleArray(train), trainShape, 'c');

        int[] labels = new int[andy.length + james.length];
        for(int i = 0; i < andy.length; i++){
            labels[i] = 1;
        }
        for(int i = 0; i < james.length; i++){
            labels[i + andy.length] = 1;
        }

        INDArray trainLabels = Nd4j.create(labels, 'c');

        System.out.println(trainData.shape()[0]);
        System.out.println(labels.length);

        org.nd4j.linalg.dataset.DataSet train_data = new org.nd4j.linalg.dataset.DataSet(trainData, trainLabels);

        train_data.shuffle();

        int batchSize = 16; // how many examples to simultaneously train in the network
        int outputNum = 2; // total output classes
        int rngSeed = 123; // integer for reproducability of a random number generator
        int numRows = 28; // number of "pixel rows" in an mnist digit
        int numColumns = 28;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(rngSeed)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .l2(1e-4)
                .list()
                .layer(new DenseLayer.Builder()
                        .nIn(numRows * numColumns) // Number of input datapoints.
                        .nOut(1000) // Number of output datapoints.
                        .activation(Activation.RELU) // Activation function.
                        .weightInit(WeightInit.XAVIER) // Weight initialization.
                        .build())
                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .nIn(1000)
                        .nOut(outputNum)
                        .activation(Activation.SOFTMAX)
                        .weightInit(WeightInit.XAVIER)
                        .build())
                .pretrain(false).backprop(true)
                .build();

        // create the MLN
        MultiLayerNetwork network = new MultiLayerNetwork(conf);
        network.init();

        // pass a training listener that reports score every 10 iterations
        int eachIterations = 5;
        network.addListeners(new ScoreIterationListener(eachIterations));

        // fit a dataset for a single epoch
        // network.fit(emnistTrain)

        // fit for multiple epochs
        int numEpochs = 2;
        network.fit(new MultipleEpochsIterator(numEpochs, train_data));

//        // evaluate basic performance
//        Evaluation eval = network.evaluate(emnistTest);
//        eval.accuracy();
//        eval.precision();
//        eval.recall();
//
//        // evaluate ROC and calculate the Area Under Curve
//        ROCMultiClass roc = network.evaluateROCMultiClass(emnistTest);
//        roc.calculateAverageAUC();
//
//        int classIndex = 0;
//        roc.calculateAUC(classIndex);
//
//        // optionally, you can print all stats from the evaluations
//        print(eval.stats());
//        print(roc.stats());
    }



}
