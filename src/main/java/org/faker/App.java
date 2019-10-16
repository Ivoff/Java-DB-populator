package org.faker;

import com.github.javafaker.Faker;
import org.faker.resources.Postgres;
import org.postgresql.util.PSQLException;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 *
 */
public class App {
    private static final int QNT = 100;

    private static Map<String, Integer> getLimit(String tableName, String columnName){
        Connection con = Postgres.connect();
        try {
            if(con != null) {
                String query = "select min("+columnName+") as minimo, max("+columnName+") as maximo from " + tableName;
                ResultSet result = con.createStatement().executeQuery(query);
                if(result.next()) {
                    Map<String, Integer> boundaries = new HashMap<String, Integer>();
                    boundaries.put("minimo", result.getInt("minimo"));
                    boundaries.put("maximo", result.getInt("maximo"));
                    con.close();
                    return boundaries;
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return null;
    }

    public static void main( String[] args ) {
        try{
            Connection con = Postgres.connect();

            if(con != null) {
                DatabaseMetaData metaData = con.getMetaData();
                ResultSet metaDataResult = metaData.getTables(null, "public", null, new String[]{"TABLE"});

                while(metaDataResult.next()){
                    System.out.println("\n");
                    System.out.println("Current Table: " + metaDataResult.getString("TABLE_NAME"));
                    ResultSet tableFKData = metaData.getImportedKeys(null,"public", metaDataResult.getString("TABLE_NAME"));

                    int i = 0;
                    while(tableFKData.next()){
                        System.out.println("\t" + tableFKData.getString("FKCOLUMN_NAME") + " -> " + tableFKData.getString("PKCOLUMN_NAME") + " [" + tableFKData.getString("PKTABLE_NAME") + "]");
                        i += 1;
                    }

                    if()
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
