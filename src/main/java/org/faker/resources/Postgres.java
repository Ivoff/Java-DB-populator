package org.faker.resources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Postgres {

    private static boolean driverLoaded;
    private static Properties props = new Properties();

    public static Connection connect() {
        if(!driverLoaded) {
            try {
                /*
                 * Class.forName carrega a classe de nome passado no argumento em tempo de execucao. Todos seus metodos
                 * estaticos sao executados pela jvm apos a classe ser carregada. A classe sera carregada somente se ja
                 * nao tiver sido carregada, a jvm mantem o rastreio de todas as classes ja carregadas
                 */
                Class.forName("org.postgresql.Driver");

            } catch (ClassNotFoundException e) {
                System.err.println("Error: Driver not loaded, maybe not found");
                e.printStackTrace();
                return null;
            }

            driverLoaded = true;
        }
        try{
            String url = "jdbc:postgresql://localhost:5432/postgres";

            props.setProperty("user", "postgres");
            props.setProperty("password", "123");
            props.setProperty("ssl", "false");

            return DriverManager.getConnection(url, props);
        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }
}
