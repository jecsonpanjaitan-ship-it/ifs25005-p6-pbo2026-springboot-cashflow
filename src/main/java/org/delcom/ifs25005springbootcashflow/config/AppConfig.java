package org.delcom.ifs25005springbootcashflow.config;

import org.delcom.ifs25005springbootcashflow.adapter.ai.OpenAiFinanceService;
import org.delcom.ifs25005springbootcashflow.adapter.presenter.FinancePresenter;
import org.delcom.ifs25005springbootcashflow.adapter.repository.JdbcTransactionRepository;
import org.delcom.ifs25005springbootcashflow.adapter.repository.JdbcUserRepository;
import org.delcom.ifs25005springbootcashflow.adapter.repository.SchemaInitializer;
import org.delcom.ifs25005springbootcashflow.adapter.storage.FileStorage;
import org.delcom.ifs25005springbootcashflow.domain.repository.IFinanceAiService;
import org.delcom.ifs25005springbootcashflow.domain.repository.ITransactionRepository;
import org.delcom.ifs25005springbootcashflow.domain.repository.IUserRepository;
import org.delcom.ifs25005springbootcashflow.usecase.AuthUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceAiUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.FinanceUseCase;
import org.delcom.ifs25005springbootcashflow.usecase.UserUseCase;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Composition root: seluruh wiring bean ada di sini (bukan new tersebar di view). */
@Configuration
public class AppConfig {

    @Bean
    public IUserRepository userRepository(DataSource dataSource) {
        SchemaInitializer.initialize(dataSource);
        return new JdbcUserRepository(dataSource);
    }

    /** Parameter userRepository memastikan tabel users (FK) sudah dibuat lebih dulu. */
    @Bean
    public ITransactionRepository transactionRepository(DataSource dataSource, IUserRepository userRepository) {
        return new JdbcTransactionRepository(dataSource);
    }

    @Bean
    public IFinanceAiService financeAiService(@Value("${app.openai.endpoint}") String endpoint,
                                              @Value("${app.openai.key}") String key,
                                              @Value("${app.openai.model}") String model) {
        return new OpenAiFinanceService(endpoint, key, model);
    }

    @Bean
    public AuthUseCase authUseCase(IUserRepository userRepository) {
        return new AuthUseCase(userRepository);
    }

    @Bean
    public UserUseCase userUseCase(IUserRepository userRepository) {
        return new UserUseCase(userRepository);
    }

    @Bean
    public FinanceUseCase financeUseCase(ITransactionRepository transactionRepository) {
        return new FinanceUseCase(transactionRepository);
    }

    @Bean
    public FinanceAiUseCase financeAiUseCase(IFinanceAiService aiService, FinanceUseCase financeUseCase) {
        return new FinanceAiUseCase(aiService, financeUseCase);
    }

    @Bean
    public FinancePresenter financePresenter() {
        return new FinancePresenter();
    }

    @Bean
    public FileStorage fileStorage(@Value("${app.upload-dir}") String uploadDir) {
        return new FileStorage(uploadDir);
    }

    @Bean
    public ApplicationRunner seedRootAdmin(AuthUseCase authUseCase,
                                           @Value("${app.root-username}") String rootUsername,
                                           @Value("${app.root-password}") String rootPassword) {
        return args -> authUseCase.ensureRootAdmin(rootUsername, rootPassword);
    }

    /** Melayani /uploads/** dari folder UPLOAD_DIR (foto profil & bukti transaksi). */
    @Bean
    public WebMvcConfigurer uploadsResourceConfigurer(FileStorage fileStorage) {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(fileStorage.root().toUri().toString());
            }
        };
    }
}
