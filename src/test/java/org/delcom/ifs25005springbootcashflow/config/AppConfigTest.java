package org.delcom.ifs25005springbootcashflow.config;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.UUID;
import javax.sql.DataSource;
import org.delcom.ifs25005springbootcashflow.adapter.ai.OpenAiFinanceService;
import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.entity.Role;
import org.delcom.ifs25005springbootcashflow.domain.entity.User;
import org.delcom.ifs25005springbootcashflow.domain.repository.IFinanceAiService;
import org.delcom.ifs25005springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

class AppConfigTest {

    @TempDir
    Path tmp;

    private final AppConfig config = new AppConfig();

    private static DataSource dataSource() {
        return new DriverManagerDataSource("jdbc:h2:mem:" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1", "sa", "");
    }

    @Test
    void wiresAllBeans() throws Exception {
        DataSource ds = dataSource();
        IUserRepository users = config.userRepository(ds);
        ITransactionRepository transactions = config.transactionRepository(ds, users);
        IFinanceAiService ai = config.financeAiService("http://localhost:9/", "key", "model");
        assertInstanceOf(OpenAiFinanceService.class, ai);

        var auth = config.authUseCase(users);
        var finance = config.financeUseCase(transactions);
        assertNotNull(config.userUseCase(users));
        assertNotNull(config.financeAiUseCase(ai, finance));
        assertNotNull(config.financePresenter());

        // seed root admin (idempoten)
        ApplicationRunner runner = config.seedRootAdmin(auth, "admin", "admin");
        runner.run(null);
        runner.run(null);
        User admin = users.findByUsername("admin").orElseThrow();
        assertEquals(Role.ADMIN, admin.getRole());
        assertEquals(1, users.findAll().size());
    }

    @Test
    void exposesUploadsFolder() {
        FileStorage storage = config.fileStorage(tmp.resolve("uploads").toString());
        WebMvcConfigurer configurer = config.uploadsResourceConfigurer(storage);

        ResourceHandlerRegistry registry = mock(ResourceHandlerRegistry.class);
        ResourceHandlerRegistration registration = mock(ResourceHandlerRegistration.class);
        when(registry.addResourceHandler("/uploads/**")).thenReturn(registration);
        when(registration.addResourceLocations(anyString())).thenReturn(registration);

        configurer.addResourceHandlers(registry);

        verify(registration).addResourceLocations(storage.root().toUri().toString());
        assertTrue(storage.root().toUri().toString().endsWith("/"));
    }
}
