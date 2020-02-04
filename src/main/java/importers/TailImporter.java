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

import dev.morphia.Datastore;
import models.tailNumber;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class TailImporter extends Importer {
    public TailImporter(HashMap<String, String> config, Datastore connection) {
        super(config, connection);
    }

    public boolean execute() throws IOException, ParseException {

        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();

        try(FileReader reader = new FileReader(config.get("t"))){
            //Read JSON file
            Object obj = jsonParser.parse(reader);

            JSONArray tails = (JSONArray) obj;

            connection.ensureIndexes();
            //Iterate over tail array
            for(Object tail : tails){
                JSONObject jsonTail = (JSONObject) tail;
                connection.save(new tailNumber((String) jsonTail.get("tailNumber")));
            }

        }
        catch(Exception e){
            e.printStackTrace();
            return false;
        }

        return true;
    }
}
