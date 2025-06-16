package ro.ase.ism.dissertation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTransitOperations;

@Configuration
public class VaultTransitConfig {

    @Bean
    public VaultTransitOperations vaultTransitOperations(VaultOperations vaultOperations) {
        // uses the "transit" mount by default
        return vaultOperations.opsForTransit();
    }
}
