package org.faker;

import com.github.javafaker.Faker;
import org.faker.resources.Postgres;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Date;

/**
 * Hello world!
 *
 */
public class App
{
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

    private static List<Integer> getTableId(String tableName){
        List<Integer> tableIds = new ArrayList<Integer>();
        Connection con = Postgres.connect();
        if(con != null) {
            String query = "select id from  " + tableName;
            try {
                ResultSet result = con.createStatement().executeQuery(query);
                while (result.next()){
                    tableIds.add(result.getInt("id"));
                }
                return tableIds;
            }catch (SQLException e){
                e.printStackTrace();
            }
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

                for(int i = 0; i < QNT + 900; i += 1) {
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

                List<Integer> perfilIdList = getTableId("perfil");
                for(int i = 0; i < QNT; i += 1){
                    String query = "insert into equipe(criador_perfil_id, nome, descricao) values(?, ?, ?)";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setInt(1, perfilIdList.get(faker.number().numberBetween(1, perfilIdList.size())));
                    statement.setString(2, faker.lorem().sentence(3));
                    statement.setString(3, faker.lorem().paragraph(7));
                    statement.executeUpdate();
                    statement.close();
                }
                con.commit();

                Map<String, Integer> perfilRange = getLimit("perfil", "id");

                Map<String, Integer> equipeRange = getLimit("equipe", "id");

                List<Integer> equipeIdList = getTableId("equipe");
                for(int i = 0; i < 100; i += 1){
                    String query = "insert into membro(perfil_id, equipe_id) values(?, ?)";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setInt(1, perfilIdList.get(faker.number().numberBetween(1, perfilIdList.size())));
                    statement.setInt(2, equipeIdList.get(faker.number().numberBetween(1, equipeIdList.size())));
                    statement.executeUpdate();
                    statement.close();
                }
                con.commit();

                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date low = Date.from(Instant.now().plus(60, ChronoUnit.SECONDS));
                Date sup = formatter.parse("06/09/2025");
                for(int i = 0; i < 100; i += 1){
                    String query = "insert into maratona(nome, inscricao_comeco, inscricao_termino, " +
                            "horario_comeco, horario_termino, numero_maximo_time, numero_maximo_participantes_time) " +
                            "values (?, ?, ?, ?, ?, ?, ?)";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setString(1, faker.lorem().sentence(4));

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
                    con.commit();
                }

                Map<String, Integer> maratonaRange = getLimit("maratona", "id");
                List<Integer> maratonaIdList = getTableId("maratona");
                for(int i = 0; i < 100; i += 1){
                    String query = "insert into equipemaratona(maratona_id, equipe_id, status_equipe, pontuacao_final) " +
                            "values(?, ?, ?, ?)";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setInt(1, maratonaIdList.get(faker.number().numberBetween(1, maratonaIdList.size())));
                    statement.setInt(2, equipeIdList.get(faker.number().numberBetween(1, equipeIdList.size())));
                    statement.setInt(3, faker.number().numberBetween(0, 2));
                    statement.setDouble(4, faker.number().randomDouble(10, 0, 100));
                    statement.executeUpdate();
                    con.commit();
                }

                for(int i = 0; i < QNT; i += 1){
                    String query = "insert into questoes(descricao, entrada, saida, dificuldade, titulo) values (?, ?, ?, ?, ?)";
                    PreparedStatement statement = con.prepareStatement(query);
                    statement.setString(1, faker.lorem().paragraph());
                    statement.setString(2, faker.lorem().paragraph());
                    statement.setString(3, faker.lorem().paragraph());
                    statement.setInt(4, faker.number().numberBetween(1, 11));
                    statement.setString(5, faker.lorem().sentence());
                    statement.executeUpdate();
                    con.commit();
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
