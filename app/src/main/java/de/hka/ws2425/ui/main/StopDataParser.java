package de.hka.ws2425.ui.main;
import org.gtfs.reader.GtfsDaoBase;
import org.gtfs.reader.GtfsReader;
import org.gtfs.reader.GtfsSimpleDao;

import java.io.BufferedReader;
import java.io.DataOutput;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

public class StopDataParser {

    public static List<Stops> parseStopsFromFile(String filePath) {
        List<Stops> stopList = new ArrayList<>();
        //for (Stops stop: dao.getStops) {
        //    stopList.add(stop);
        //}
       // try (BufferedReader br = new BufferedReader(new FileReader(new File(filePath)))) {
         //   String line;
           // boolean headerSkipped = false;

            // Datei Zeile für Zeile lesen
            //while ((line = br.readLine()) != null) {
              //  if (!headerSkipped) { // Überspringe die Header-Zeile
                //    headerSkipped = true;
                  //  continue;
               // }

            //    String[] values = line.split(","); // CSV-Trennung
            //    String stopId = values[0];
            //    String stopName = values[1];
        //   double stopLat = Double.parseDouble(values[2]);



             //   double stopLon = Double.parseDouble(values[3]);

           //     stopList.add(new Stops(stopId, stopName, stopLat, stopLon));
          //  }

      //  } catch (Exception e) {
        //    e.printStackTrace();
       // }
        stopList.add(new Stops("1", "kirche", 49.000379, 8.812543));
        System.out.println("Liste im Parser:" + stopList);
        return stopList;

    }
}