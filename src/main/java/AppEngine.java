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

import executors.ImportExecutor;
import org.apache.commons.cli.*;

import java.util.HashMap;

public class AppEngine {


    public static void main(String[] args) {

        AppEngine ae = new AppEngine();
        HashMap<String, String> parsedArgs = parseArgs(args);
        ae.execute(parsedArgs);

    }


    private static HashMap<String, String> parseArgs(String[] args){

        HashMap<String, String> parsedAgrs = new HashMap<>();

        // command line inputs for shit
        final Options options = new Options();
        options.addOption(new Option("t", "importTails", true, "imports tails from tails.json"));
        options.addOption(new Option("a", "importADSB", true, "imports data from ADSB param format of \"YYYY-MM-DD\""));
        options.addOption(new Option("c", "importCEDAS", true, "Imports CEDAS Upload info"));
        options.addOption(new Option("d", "dataBaseName", true, "Name of Mongo Database to Import to"));
        options.addOption(new Option("C", "runCorrelator", false, "run corellator"));

        // create the parser
        CommandLineParser parser = new DefaultParser();
        try {
            // parse the command line arguments and add to hashmap.
            for(Option option : parser.parse( options, args ).getOptions()) {
                parsedAgrs.put(option.getLongOpt(), option.getValue());
            }
        }
        catch( ParseException exp ) {
            // oops, something went wrong
            System.err.println( "Parsing failed.  Reason: " + exp.getMessage() );
        }

        return parsedAgrs;
    }

    private boolean execute(HashMap<String, String> parsedArgs){

        ImportExecutor importExecutor = new ImportExecutor(parsedArgs);

        return importExecutor.execute();
    }


}
