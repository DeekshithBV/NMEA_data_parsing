package com.example.nmea_data_parsing;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.nmea_data_parsing.databinding.ActivityMainBinding;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding mBinding;
    private final Handler handler = new Handler();
    private List<String> blocks;
    private int currentBlockIndex = 0;
    private final String TAG = "Main_activity";
    Pattern startPattern = Pattern.compile("\\$G.RMC");
    Pattern endPattern = Pattern.compile("\\$G.GLL");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        // Read and parse the .anf file
        blocks = loadAndParseNMEAData();
        Log.d(TAG, "onCreate: ");
        handler.post(updateRunnable);

    }

    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            // Display the current block in the TextView
            if (!blocks.isEmpty() && currentBlockIndex != blocks.size()) {
                Log.d(TAG, "CurrBlockInd: "+currentBlockIndex);
                mBinding.nmeaData.setText("");
                // Move to the next block, wrapping around if necessary
                currentBlockIndex = (currentBlockIndex) % blocks.size();
                mBinding.nmeaData.setText(blocks.get(currentBlockIndex));
                Log.d(TAG, "blocks text : "+blocks.get(currentBlockIndex));
                currentBlockIndex++;
                // Schedule the next update after 1 second (1000 ms)
                handler.postDelayed(this, 1000);
            }
        }
    };

    private List<String> loadAndParseNMEAData() {
        List<String> extractedBlocks = new ArrayList<>();
        Matcher startMatcher;
        Matcher endMatcher;
        try {
            // Read the NMEA data from the assets folder
            BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("NMEA_data.anf")));
            String line;
            StringBuilder currentBlock = new StringBuilder();
            boolean isRecording = false;

            while ((line = reader.readLine()) != null) {
                startMatcher = startPattern.matcher(line);
                endMatcher = endPattern.matcher(line);


                // Check for $G.RMC start
                //if (line.contains("\\$G.RMC")) {
                if (startMatcher.find()) {

                    isRecording = true;
                    currentBlock = new StringBuilder(); // Reset block

                    //int startIndex = line.indexOf("\\$G.RMC");
                    //Log.d(TAG, "start_index of $GNRMC : "+startIndex);
                    //currentBlock.append(line.substring(startIndex)).append("\n");

                    currentBlock.append(line.substring(startMatcher.start())).append("\n");

                    continue;
                }

                // Append line to the current block
                if (isRecording) {
                    currentBlock.append(line).append("\n");
                }

                // Check for $GNGLL end
                //if (line.startsWith("\\$G.GLL") && isRecording) {
                if (endMatcher.find() && isRecording) {
                    extractedBlocks.add(currentBlock.toString());
                    isRecording = false; // Reset recording
                    Log.d(TAG, "is current block cleared : "+ currentBlock);
                }
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return extractedBlocks;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(updateRunnable);
    }
}