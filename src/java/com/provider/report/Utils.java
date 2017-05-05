package com.provider.report;

import au.com.bytecode.opencsv.CSVReader;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: POORNA
 * Date: 3/18/13
 * Time: 11:12 PM
 * To change this template use File | Settings | File Templates.
 */
public class Utils {

    public static String xmlFromCSV(String csv) throws Exception {

        CSVReader reader = new CSVReader(new StringReader(csv));
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("<Report>");
            List<String[]> list = reader.readAll();

            if(list == null || list.size() <= 0 ){
                return null;
            }
            String[] headers = list.get(0);
            int headerSize = headers.length;

            for(int i = 1; i<list.size(); i++){
                String[] row = list.get(i);
                buffer.append("<row>");
                for(int j = 0; j<row.length; j++){
                    buffer.append("<" + headers[j].replace(" ","_") + ">");
                    buffer.append(row[j]);
                    buffer.append("</" + headers[j].replace(" ","_") + ">");
                }
                buffer.append("</row>");
            }
            buffer.append("</Report>");
            return buffer.toString();
        } catch (IOException e) {
            throw new Exception("Unable to parse CSV");
        }
    }

    public static String jsonFromCSV(String csv) throws Exception {

        CSVReader reader = new CSVReader(new StringReader(csv));
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append("{ \"Report\" ");
            List<String[]> list = reader.readAll();

            if(list == null || list.size() <= 0 ){
                return null;
            }

            String[] headers = list.get(0);
            int headerSize = headers.length;

            if(list.size() > 1){
                buffer.append(": { \"row\" : [");
            }
            for(int i = 1; i<list.size(); i++){
                String[] row = list.get(i);
                buffer.append("{");
                for(int j = 0; j<row.length; j++){
                    buffer.append("\"" + headers[j]  + "\":");
                    buffer.append("\"" + row[j] + "\",");
                }
                buffer.deleteCharAt(buffer.length()-1);
                buffer.append("},");
            }

            if(list.size() > 1){
                buffer.deleteCharAt(buffer.length()-1);
                buffer.append("]");
            }
            buffer.append("} }");
            return buffer.toString();
        } catch (IOException e) {
            throw new Exception("Unable to parse CSV");
        }
    }

}
