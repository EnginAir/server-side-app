package edu.nau.enginair.importers;

import dev.morphia.Datastore;
import edu.nau.enginair.models.Airport;
import io.github.seeesvee.CSVParseBuilder;
import io.github.seeesvee.CSVParser;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

public class AirportImporter extends Importer {

    public AirportImporter(HashMap<String, String> config, Datastore connection) {
        super(config, connection);
    }

    @Override
    public boolean execute() throws IOException, ParseException, ExecutionException, InterruptedException {
        downloadCSV();
        parseCSV();
        return true;
    }


    private Boolean parseCSV(){
        try {
            CSVParseBuilder<Airport> parsebuilder = new CSVParseBuilder<>();

            parsebuilder.setClass(Airport.class);

            CSVParser<Airport> csvParser = parsebuilder.create();

            ArrayList<Airport> datas = csvParser.parse(new File("./Airports/airports.csv"));

            for(Airport airport : datas){
                airport.setLocation();
                airport.killQuotes();
                connection.save(airport);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean downloadCSV() throws IOException {
        System.out.println("Downloading le airport file lololol");
        saveFile(new URL("https://raw.githubusercontent.com/jpatokal/openflights/master/data/airports.dat"), "./Airports/airports.csv");
        byte[] bytes = Files.readAllBytes(new File("./Airports/airports.csv").toPath());
        RandomAccessFile f = new RandomAccessFile(new File("./Airports/airports.csv"), "rw");
        f.seek(0); // to the beginning
        f.write("ID,Name,City,Country,IATA,ICAO,Latitude,Longitude,Altitude,Timezone,DST,Tz,Type,Source\n".getBytes());
        f.write(bytes);
        f.close();
        return true;
    }

    public boolean saveFile(URL imgURL, String imgSavePath) {

        boolean isSucceed = true;

        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet(imgURL.toString());
        httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.11 Safari/537.36");
        httpGet.addHeader("Referer", "https://www.openflights.org");

        try {
            CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
            HttpEntity imageEntity = httpResponse.getEntity();

            if (imageEntity != null) {
                FileUtils.copyInputStreamToFile(imageEntity.getContent(), new File(imgSavePath));
            }

        } catch (IOException e) {
            isSucceed = false;
        }

        httpGet.releaseConnection();

        return isSucceed;
    }

}
