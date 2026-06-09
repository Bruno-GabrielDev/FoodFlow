package br.edu.ifsp.foodflow.app.util;

import org.flywaydb.core.Flyway;

import java.io.InputStream;
import java.util.Properties;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Utilitário para resetar o banco de dados durante os testes.
 * Lê as credenciais do application.properties (com fallback para os padrões),
 * para evitar falhas de autenticação, executa o Flyway clean+migrate e, em
 * seguida, remove as comandas-semente, deixando um baseline determinístico para
 * os testes de UI: schema + dados de referência (usuários, mesas, cardápio,
 * adicionais), porém SEM comandas ativas.
 */
public class DbTestHelper {

    private static final String DEFAULT_URL = "jdbc:postgresql://localhost:5433/foodflow";
    private static final String DEFAULT_USER = "postgres";
    private static final String DEFAULT_PASS = "postgres";

    /**
     * Reseta o banco de dados: apaga tudo, recria as tabelas e limpa as comandas-semente.
     * Requer que o banco de dados esteja acessível em localhost:5433.
     */
    public static void resetDatabase() {
        String url = DEFAULT_URL;
        String user = DEFAULT_USER;
        String pass = DEFAULT_PASS;

        try (InputStream is = DbTestHelper.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (is != null) {
                Properties props = new Properties();
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
                    .cleanDisabled(false) // Permite o comando clean
                    .locations("classpath:db/migration")
                    .load();

            flyway.clean();
            flyway.migrate();

            clearOrders(url, user, pass);
            System.out.println("--- Banco de Dados Resetado com Sucesso ---");
        } catch (Exception e) {
            System.err.println("Aviso: Falha ao resetar banco (Postgres offline ou senha errada): " + e.getMessage());
        }
    }

    /**
     * Remove todas as comandas (pedidos) semeadas pelas migrações e libera as mesas,
     * para que cada classe de teste de UI comece sem comandas ativas.
     */
    private static void clearOrders(String url, String user, String pass) throws Exception {
        try (Connection conn = DriverManager.getConnection(url, user, pass);
             Statement st = conn.createStatement()) {
            // Ordem respeita as foreign keys: addons -> itens -> comandas.
            st.executeUpdate("DELETE FROM order_item_addons");
            st.executeUpdate("DELETE FROM order_items");
            st.executeUpdate("DELETE FROM orders");
            st.executeUpdate("UPDATE restaurant_tables SET status = 'AVAILABLE'");
        }
    }
}
