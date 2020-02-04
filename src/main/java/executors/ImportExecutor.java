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

package executors;

import com.mongodb.MongoClient;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import importers.ADSBImporter;
import importers.CEDASImporter;
import importers.Importer;
import importers.TailImporter;

import java.util.HashMap;
import java.util.Map;

public class ImportExecutor {

    private HashMap<String, String> config;

    public ImportExecutor(HashMap<String, String> config) {
        this.config = config;
    }

    public boolean execute() {

        try{
            Datastore dataStore = makeConnection(config.get("d"));

            for(Map.Entry<String, String> arg : config.entrySet()){

                Importer importer = getImporter(arg.getKey(), dataStore);
                if(importer != null){
                    importer.execute();
                }

            }
            return true;
        }

        catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    private Importer getImporter(String arg, Datastore datastore){

        if(arg.equals("c")){
            return new CEDASImporter(config, datastore);
        }
        if(arg.equals("a")){
            return new ADSBImporter(config, datastore);
        }
        if(arg.equals("t")){
            return new TailImporter(config, datastore);
        }

        return null;
    }

    private Datastore makeConnection(String dbName) {

        final Morphia morphia = new Morphia();
        morphia.mapPackage("models");
        return morphia.createDatastore(new MongoClient(), dbName);
    }
}
