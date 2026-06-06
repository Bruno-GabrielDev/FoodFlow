package br.edu.ifsp.foodflow.app.util;

import org.flywaydb.core.Flyway;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utilitário para resetar o banco de dados durante os testes.
 * Tenta ler as credenciais do application.properties para evitar falhas de autenticação.
 */
public class DbTestHelper {

    public static void resetDatabase() {
        Properties props = new Properties();
        String url = "jdbc:postgresql://localhost:5433/foodflow";
        String user = "postgres";
        String pass = "postgres";

        try (InputStream is = DbTestHelper.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
                url = props.getProperty("spring.datasource.url", url)
                        .replace("${POSTGRES_HOST:localhost}", "localhost")
                        .replace("${POSTGRES_PORT:5433}", "5433")
                        .replace("${POSTGRES_DB:foodflow}", "foodflow");
                user = props.getProperty("spring.datasource.username", user)
                        .replace("${POSTGRES_USER:postgres}", "postgres");
                pass = props.getProperty("spring.datasource.password", pass)
                        .replace("${POSTGRES_PASSWORD:postgres}", "postgres");
            }
        } catch (Exception ignored) {}

        try {
            System.out.println("--- Resetando Banco de Dados (" + url + ") ---");
            Flyway flyway = Flyway.configure()
                    .dataSource(url, user, pass)
                    .cleanDisabled(false)
                    .locations("classpath:db/migration")
                    .load();

            flyway.clean();
            flyway.migrate();
            System.out.println("--- Banco de Dados Resetado com Sucesso ---");
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao resetar banco (Postgres offline ou senha errada).");
        }
    }
}
