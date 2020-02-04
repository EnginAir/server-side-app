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

package importers;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import dev.morphia.Datastore;
import models.CEDASUpload;
import models.LatLong;
import models.TailNumber;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CEDASImporter extends Importer {
    public CEDASImporter(HashMap<String, String> config, Datastore connection) {
        super(config, connection);
    }

    public boolean execute() throws IOException {

        if(config.get("importCEDAS").toLowerCase().endsWith(".xlsx")){
            return executeEXCEL();
        }
        else if(config.get("importCEDAS").toLowerCase().endsWith(".json")){
            return executeJSON(true, null);
        }
        else {
            return false;
        }
    }

    private boolean executeEXCEL() throws IOException {
        List<CEDASUpload> cedatas = new ArrayList<>();

        //System.out.println(config.get("importCEDAS"));
        FileInputStream fileInput = new FileInputStream(new File(config.get("importCEDAS")));
        XSSFWorkbook wb = new XSSFWorkbook(fileInput);
        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFRow row;
        XSSFCell cell;

        int rows = sheet.getPhysicalNumberOfRows();
        int cols = 0;
        int tmp = 0;

        for(int i = 0; i < 10 || i < rows; i++){
            row = sheet.getRow(i);
            if(row != null){
                tmp = sheet.getRow(i).getPhysicalNumberOfCells();
            }
            if(tmp>cols){
                cols = tmp;
            }
        }

        for(int r = 1; r < rows; r++){
            row = sheet.getRow(r);
            if(row != null ){
                if(row.getCell(0) != null && row.getCell(0).getCellType() != Cell.CELL_TYPE_BLANK){
                    cedatas.add(new CEDASUpload(
                            row.getCell(0).toString(),
                            new LatLong(Float.parseFloat(row.getCell(1).toString()), Float.parseFloat(row.getCell(2).toString())),
                            row.getCell(3).toString(),
                            new LatLong(Float.parseFloat(row.getCell(4).toString()), Float.parseFloat(row.getCell(5).toString())),
                            row.getCell(6).toString(),
                            new LatLong(Float.parseFloat(row.getCell(7).toString()), Float.parseFloat(row.getCell(8).toString())),
                            row.getCell(9).toString(),
                            row.getCell(10).toString(),
                            row.getCell(11).toString()));
                }
            }
        }
        return executeJSON(false, cedatas);
    }


    private boolean executeJSON(boolean isAlreadyJSON, List<CEDASUpload> cedasUploads) throws FileNotFoundException {

        try{
            CEDASUpload[] cedasData;

            if(isAlreadyJSON){
                Gson gson = new Gson();
                JsonReader reader = new JsonReader(new FileReader(config.get("importCEDAS")));
                cedasData = gson.fromJson(reader, CEDASUpload[].class);
            }
            else{
                cedasData = new CEDASUpload[cedasUploads.size()];
                for(int i = 0; i < cedasUploads.size(); i++){
                    cedasData[i] = cedasUploads.get(i);
                }
            }

            connection.ensureIndexes();
            for(CEDASUpload cu : cedasData){
                connection.save(cu);
            }

        }
        catch (Exception e){
            e.printStackTrace();
            return false;
        }


        return true;
    }
}


