package com.example.tinytalks;

import android.content.Context;
import android.media.AudioRecord;
import android.util.Log;

import org.tensorflow.lite.support.audio.TensorAudio;
import org.tensorflow.lite.support.label.Category;
import org.tensorflow.lite.task.audio.classifier.AudioClassifier;
import org.tensorflow.lite.task.audio.classifier.Classifications;
import org.tensorflow.lite.task.core.BaseOptions;

import java.io.IOException;
import java.util.List;

public class CryDetector {

    private static final String TAG = "CryDetector";

    public interface ResultListener {
        void onResult(boolean cryDetected);
    }

    private AudioClassifier classifier;
    private AudioRecord audioRecord;
    private volatile boolean isRunning = false;
    private final ResultListener listener;

    public CryDetector(Context context, ResultListener listener) {
        this.listener = listener;

        AudioClassifier.AudioClassifierOptions options =
                AudioClassifier.AudioClassifierOptions.builder()
                        .setBaseOptions(BaseOptions.builder().setNumThreads(2).build())
                        .setMaxResults(5)
                        .setScoreThreshold(0.1f)
                        .build();

        try {
            classifier = AudioClassifier.createFromFileAndOptions(context, "yamnet.tflite", options);
        } catch (IOException e) {
            Log.e(TAG, "Failed to load yamnet.tflite from assets", e);
        }
    }

    public void start() {
        if (classifier == null) {
            Log.e(TAG, "Classifier not initialized — cannot start");
            return;
        }

        TensorAudio tensorAudio = classifier.createInputTensorAudio();
        audioRecord = classifier.createAudioRecord();
        audioRecord.startRecording();
        isRunning = true;

        new Thread(() -> {
            while (isRunning) {
                tensorAudio.load(audioRecord);
                List<Classifications> results = classifier.classify(tensorAudio);

                boolean cryFound = false;
                if (!results.isEmpty()) {
                    List<Category> categories = results.get(0).getCategories();

                    for (Category category : categories) {
                        Log.d(TAG, "Candidate: " + category.getLabel() + " (" + category.getScore() + ")");
                        String label = category.getLabel().toLowerCase();
                        if ((label.contains("cry") || label.contains("infant") || label.contains("wail")
                                || label.contains("whimper"))
                                && category.getScore() > 0.15f) {
                            cryFound = true;
                        }
                    }
                }

                listener.onResult(cryFound);

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }).start();
    }

    public void stop() {
        isRunning = false;
        if (audioRecord != null) {
            audioRecord.stop();
            audioRecord.release();
            audioRecord = null;
        }
    }
}