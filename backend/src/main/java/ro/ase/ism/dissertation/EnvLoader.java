package ro.ase.ism.dissertation;

import io.github.cdimascio.dotenv.Dotenv;

public class EnvLoader {
    public static void loadEnv() {
        Dotenv dotenv = Dotenv.configure()
                .directory("src/main/resources")
                .filename(".env")
                .ignoreIfMissing()
                .systemProperties()
                .load();

        String vaultToken = dotenv.get("VAULT_TOKEN");
        System.setProperty("VAULT_TOKEN", vaultToken);
    }
}
