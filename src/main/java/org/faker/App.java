package org.faker;

import com.github.javafaker.Faker;
import org.faker.resources.Postgres;

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
public class App
{
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

    public static void main( String[] args )
    {
        Connection con = Postgres.connect();
        Faker faker = new Faker();
        try {
            Class.forName("com.github.javafaker.Faker");
            if(con != null) {
                con.setAutoCommit(false);

                for(int i = 0; i < 100; i += 1) {
                    String query = "insert into perfil(nome_completo, rga, siapi, cpf, codigo_uri, status_participante, " +
                            "status_voluntario, status_tecnico) values(?, ?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setString(1, faker.name().fullName());
                    statement.setString(2, faker.number().digits(12));
                    statement.setString(3, faker.number().digits(12));
                    statement.setString(4, faker.number().digits(11));
                    statement.setString(5, faker.number().digits(15));
                    statement.setBoolean(6, false);
                    statement.setBoolean(7, false);
                    statement.setBoolean(8, false);
                    if (faker.number().numberBetween(0, 3) == 0)
                        statement.setBoolean(6, true);
                    else if (faker.number().numberBetween(0, 3) == 1)
                        statement.setBoolean(7, true);
                    else
                        statement.setBoolean(8, true);
                    statement.executeUpdate();
                    statement.close();
                }
                con.commit();

                for(int i = 0; i < 100; i += 1){
                    String query = "insert into equipe(criador_perfil_id, nome, descricao) values(?, ?, ?)";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setInt(1, faker.number().numberBetween(1, 100));
                    statement.setString(2, faker.team().name());
                    statement.setString(3, faker.lorem().paragraph(7));
                    statement.executeUpdate();
                    statement.close();
                }
                con.commit();

                Map<String, Integer> perfilRange = getLimit("perfil", "id");
                
                Map<String, Integer> equipeRange = getLimit("equipe", "id");
                
                for(int i = 0; i < 100; i += 1){
                    String query = "insert into membro(perfil_id, equipe_id) values(?, ?)";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setInt(1, faker.number().numberBetween(perfilRange.get("minimo"), perfilRange.get("maximo")));
                    statement.setInt(2, faker.number().numberBetween(equipeRange.get("minimo"), equipeRange.get("maximo")));
                    statement.executeUpdate();
                    statement.close();
                }
                con.commit();

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date low = formatter.parse("13/10/2019");
                Date sup = formatter.parse("06/09/2021");
                for(int i = 0; i < 100; i += 1){
                    String query = "insert into maratona(nome, inscricao_comeco, inscricao_termino, " +
                            "horario_comeco, horario_termino, numero_maximo_time, numero_maximo_participantes_time) " +
                            "values (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setString(1, faker.lordOfTheRings().location());

                    Instant subBeginDate = faker.date().between(low, sup).toInstant();
                    Instant subEndDate = subBeginDate.plus(faker.number().numberBetween(15, 60), ChronoUnit.DAYS);
                    statement.setTimestamp(2, Timestamp.from(subBeginDate));
                    statement.setTimestamp(3, Timestamp.from(subEndDate));
                    statement.setTimestamp(4, Timestamp.from(subEndDate.plus(6, ChronoUnit.HOURS)));
                    statement.setTimestamp(5, Timestamp.from(subEndDate.plus(12, ChronoUnit.HOURS)));
                    statement.setInt(6, faker.number().numberBetween(5, 150));
                    statement.setInt(7, faker.number().numberBetween(3, 5));
                    statement.executeUpdate();
                    statement.close();
                }
                con.commit();

                Map<String, Integer> maratonaRange = getLimit("maratona", "id");
                for(int i = 0; i < 100; i += 1){
                    String query = "insert into equipemaratona(maratona_id, equipe_id, status_equipe, pontuacao_final) " +
                            "values(?, ?, ?, ?)";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setInt(1, faker.number().numberBetween(maratonaRange.get("minimo"), maratonaRange.get("maximo")));
                    statement.setInt(2, faker.number().numberBetween(equipeRange.get("minimo"), equipeRange.get("maximo")));
                    statement.setInt(3, faker.number().numberBetween(0, 2));
                    statement.setDouble(4, faker.number().randomDouble(10, 0, 100));
                    statement.executeUpdate();
                    con.commit();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
