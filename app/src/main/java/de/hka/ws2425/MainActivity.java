package de.hka.ws2425;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import org.gtfs.reader.GtfsReader;
import org.gtfs.reader.GtfsSimpleDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.List;

import de.hka.ws2425.ui.main.MainFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        String filePath = copyAssetToFile(this, "gtfs.zip", "gtfs.zip");
        if (filePath != null) {
            System.out.println("Dateipfad: " + filePath);
        } else {
            System.out.println("Fehler beim Kopieren der Datei!");
        }

        System.out.println("Hallo Welt!");
        String path = this.getApplication().getFilesDir() + "/gtfs.zip";
        File gtfsInputFile = new File(path);
        System.out.println(path);
        GtfsSimpleDao gtfsSimpleDao = new GtfsSimpleDao();

        GtfsReader gtfsReader = new GtfsReader();
        gtfsReader.setDataAccessObject(gtfsSimpleDao);
        try {
            gtfsReader.read(gtfsInputFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        gtfsSimpleDao.getAgencies().forEach(agency ->{
            Log.d(this.getClass().getSimpleName(), agency.getName());
    });

        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow();

        }
    }

    public String copyAssetToFile(Context context, String assetPath, String outputFileName) {
        File outFile = new File(context.getFilesDir(), outputFileName); // Ziel im internen Speicher
        if (outFile.exists()) {
            return outFile.getAbsolutePath(); // Datei existiert bereits
        }

        try (InputStream inputStream = context.getAssets().open(assetPath);
             FileOutputStream outputStream = new FileOutputStream(outFile)) {

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return outFile.getAbsolutePath(); // Absoluter Pfad der kopierten Datei
    }


}
