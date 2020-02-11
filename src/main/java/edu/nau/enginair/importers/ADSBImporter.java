/*
 * The MIT License
 *
 * Copyright © 2010-2020 EnginAir Team
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the “Software”), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package edu.nau.enginair.importers;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import dev.morphia.Datastore;
import edu.nau.enginair.models.*;
import org.apache.commons.io.FileUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ADSBImporter extends Importer {
    private ArrayList<String> tailCache = null;
    public ADSBImporter(HashMap<String, String> config, Datastore connection) {
        super(config, connection);
    }

    public boolean execute() throws IOException, ExecutionException, InterruptedException {

        ADSBDownloader adsb = new ADSBDownloader();
        ADSBData[] adsbData = adsb.execute();
        if(adsbData != null){
            System.out.println("Data isnt null");
            System.out.println("Length: " + adsbData.length);
            connection.ensureIndexes();
            for(ADSBData ad : adsbData){
                System.out.println("Added to DB " + ad.tailNumber);
                connection.save(ad);
            }
            return true;
        }
        else{
            return false;
        }
    }

    class ADSBDownloader {

        public boolean saveFile(URL imgURL, String imgSavePath) {

            boolean isSucceed = true;

            CloseableHttpClient httpClient = HttpClients.createDefault();

            HttpGet httpGet = new HttpGet(imgURL.toString());
            httpGet.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/50.0.2661.11 Safari/537.36");
            httpGet.addHeader("Referer", "https://www.adsbexchange.com");

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

        ADSBData[] execute() throws IOException, ExecutionException, InterruptedException {
            List<ADSBData> dataList = new ArrayList<>();
            download(true);
            return parse();
        }

        boolean download(boolean isTest) throws IOException {
            if (new File("./ADSBDownload/" + config.get("importADSB") + ".zip").exists()) {
                extract();
                return true;
            }
            if(isTest){
                try{

                    System.out.println("Downloading le file lololol");
                    saveFile(new URL("https://history.adsbexchange.com/downloads/samples/" + config.get("importADSB") + ".zip"), "./ADSBDownload/" + config.get("importADSB") + ".zip");
                    extract();
                    return true;
                }
                catch(Exception e){
                    e.printStackTrace();
                    return false;
                }
            }
            else{
                try{
                    FileUtils.copyURLToFile(new URL("https://history.adsbexchange.com/Aircraftlist.json/" + config.get("importADSB") + ".zip"), new File("./ADSBDownload/" + config.get("importADSB") + ".zip"), 10000, 10000);
                    extract();
                    return true;
                }
                catch(Exception e){
                    return false;
                }
            }
        }

        boolean extract() {
            try{
                String zipFilePath = "./ADSBDownload/" + config.get("importADSB") + ".zip";
                String destDir = "./ADSBDownload/" + config.get("importADSB");
                //unzip(zipFilePath, destDir);
                return true;
            }
            catch(Exception e){
                e.printStackTrace();
                return false;
            }
        }

        private void unzip(String zipFilePath, String destDir) {
            File dir = new File(destDir);
            // create output directory if it doesn't exist
            if(!dir.exists()) dir.mkdirs();
            FileInputStream fis;
            //buffer for read and write data to file
            byte[] buffer = new byte[1024];
            try {
                fis = new FileInputStream(zipFilePath);
                ZipInputStream zis = new ZipInputStream(fis);
                ZipEntry ze = zis.getNextEntry();
                while(ze != null){
                    String fileName = ze.getName();
                    File newFile = new File(destDir + File.separator + fileName);
                    System.out.println("Unzipping to "+newFile.getAbsolutePath());
                    //create directories for sub directories in zip
                    new File(newFile.getParent()).mkdirs();
                    FileOutputStream fos = new FileOutputStream(newFile);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                    //close this ZipEntry
                    zis.closeEntry();
                    ze = zis.getNextEntry();
                }
                //close last ZipEntry
                System.out.println("zis close entry");
                zis.closeEntry();
                System.out.println("zis close");
                zis.close();
                System.out.println("Fis close");
                fis.close();

                System.out.println("Finished Unzipping?");
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        // calls parse segment
        public ADSBData[] parse() throws FileNotFoundException, ExecutionException, InterruptedException {

            System.out.println("Starting parse");
            File dir = new File("./ADSBDownload/" + config.get("importADSB"));
            File[] directoryListing = dir.listFiles();


            //System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "4");

            ForkJoinPool customThreadPool = new ForkJoinPool(16);
            buildTailCache();
            List<AircraftList> l = customThreadPool.submit(() -> Arrays.asList(directoryListing)
                    .parallelStream()
                    .map(this::parseSegment)
                    .flatMap(Arrays::stream)
                    .collect(Collectors.toList())).get();

            System.out.println("Parse finished");
            return convertAircraftToADSB(l);
        }


        ADSBData[] convertAircraftToADSB(List<AircraftList> ac){
            List<ADSBData> adsb = new ArrayList<>();

            for(AircraftList aircraft : ac){
                if(aircraft.PosTime != null) {
                    adsb.add(new ADSBData(aircraft.Reg, new LatLong(aircraft.Lat, aircraft.Long), aircraft.Alt, aircraft.Spd, aircraft.PosTime));
                    System.out.println("converting aricraft " + aircraft.Reg);
                }
            }

            return adsb.toArray(new ADSBData[0]);
        }
        // takes file from parse(), reads it, makes objects
        AircraftList[] parseSegment(File f) {
            try{
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new FileReader(f));
                ADSBJSON adsb = gson.fromJson(reader, ADSBJSON.class);
                return Arrays.stream(adsb.acList)
                        .filter(this::filter)
                        .toArray(AircraftList[]::new);

            }
            catch(Exception e){
                e.printStackTrace();
                return null;
            }
        }

        private void buildTailCache() {
            if(tailCache == null) {
                TailNumber[] meme = connection.createQuery(TailNumber.class).find().toList().toArray(new TailNumber[0]);
                tailCache = new ArrayList<>(meme.length);
                for(TailNumber t : meme) {
                    tailCache.add(t.tailNumber.toUpperCase());
                }
            }
        }

        // filters out tail numbers that arent in TailNumbers table.
        boolean filter(AircraftList aircraft) {
            try{
                return aircraft.Reg != null && tailCache.contains(aircraft.Reg.toUpperCase());
            }
            catch (Exception e){
                e.printStackTrace();
                return false;
            }
        }
    }
}