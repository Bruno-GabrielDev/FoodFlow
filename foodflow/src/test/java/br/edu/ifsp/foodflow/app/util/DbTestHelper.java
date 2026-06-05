package br.edu.ifsp.foodflow.app.util;

import org.flywaydb.core.Flyway;

/**
 * Utilitário para resetar o banco de dados durante os testes.
 * Utiliza o Flyway para limpar e reaplicar as migrações.
 */
public class DbTestHelper {

    private static final String DB_URL = "jdbc:postgresql://localhost:5433/foodflow";
    private static final String DB_USER = "postgres";
    private static final String DB_PASS = "postgres";

    /**
     * Reseta o banco de dados: apaga tudo e recria as tabelas.
     * Requer que o banco de dados esteja acessível em localhost:5433.
     */
    public static void resetDatabase() {
        try {
            System.out.println("--- Resetando Banco de Dados via Flyway ---");
            Flyway flyway = Flyway.configure()
                    .dataSource(DB_URL, DB_USER, DB_PASS)
                    .cleanDisabled(false) // Permite o comando clean
                    .locations("classpath:db/migration")
                    .load();

            flyway.clean();
            flyway.migrate();
            System.out.println("--- Banco de Dados Resetado com Sucesso ---");
        } catch (Exception e) {
            System.err.println("ERRO ao resetar banco de dados: " + e.getMessage());
            // Não relançamos a exceção para não impedir a execução se o banco não permitir clean
        }
    }
}
