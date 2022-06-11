package com.mlkabdazz.pickfish.env;

import com.mlkabdazz.pickfish.tflite.Classifier;

import java.util.List;

public class InferenceUtils {

    /**
     * Inference for get final condition of fish
     *
     * @return Fresh, Medium, and Spoiled
     */
    public static String getInferences(List<Classifier.Recognition> recognitions) {
        String eyesCondition = "";
        String skinsCondition = "";
        String[] eyes = {"fresh_eyes", "medium_eyes", "spoil_eyes"};
        String[] skins = {"fresh_skins", "medium_skins", "spoil_skins"};

        for (Classifier.Recognition recognition : recognitions) {
            if (recognition.getTitle().contains("Eyes")) {
                eyesCondition = recognition.getTitle();
            } else if (recognition.getTitle().contains("Skins")) {
                skinsCondition = recognition.getTitle();
            }
        }

        if ((eyesCondition.equalsIgnoreCase(eyes[0]) && skinsCondition.equalsIgnoreCase(skins[0]))) {
            return "Fresh";
        } else if (
                (eyesCondition.equalsIgnoreCase(eyes[1]) && skinsCondition.equalsIgnoreCase(skins[0])) ||
                        (eyesCondition.equalsIgnoreCase(eyes[0]) && skinsCondition.equalsIgnoreCase(skins[1])) ||
                        (eyesCondition.equalsIgnoreCase(eyes[1]) && skinsCondition.equalsIgnoreCase(skins[1])) ||
                        (eyesCondition.equalsIgnoreCase(eyes[2]) && skinsCondition.equalsIgnoreCase(skins[0])) ||
                        (eyesCondition.equalsIgnoreCase(eyes[2]) && skinsCondition.equalsIgnoreCase(skins[1])) ||
                        (eyesCondition.equalsIgnoreCase(eyes[0]) && skinsCondition.equalsIgnoreCase(skins[2]))) {
            return "Medium";
        } else if (
                (eyesCondition.equalsIgnoreCase(eyes[2]) && skinsCondition.equalsIgnoreCase(skins[2])) ||
                        (eyesCondition.equalsIgnoreCase(eyes[1]) && skinsCondition.equalsIgnoreCase(skins[2]))) {
            return "Spoil";
        } else {
            if (eyesCondition.isEmpty()) {
                return "Fail Inference (Eyes Not Found)";
            } else if (skinsCondition.isEmpty()) {
                return "Fail Inference (Skins Not Found)";
            } else {
                return "Fail Inference (Rule Not Found)";
            }
        }
    }


}
