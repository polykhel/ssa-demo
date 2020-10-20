package com.polykhel.ssa.config.liquibase;

import liquibase.integration.spring.SpringLiquibase;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.liquibase.DataSourceClosingSpringLiquibase;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import java.util.concurrent.Executor;

/**
 * Utility class for handling SpringLiquibase.
 *
 * <p>
 * It follows implementation of
 * <a href="https://github.com/spring-projects/spring-boot/blob/master/spring-boot-project/spring-boot-autoconfigure/src/main/java/org/springframework/boot/autoconfigure/liquibase/LiquibaseAutoConfiguration.java">LiquibaseAutoConfiguration</a>.
 */
public class SpringLiquibaseUtil {

    private SpringLiquibaseUtil() {
    }

    public static SpringLiquibase createSpringLiquibase(DataSource liquibaseDatasource,
                                                        LiquibaseProperties liquibaseProperties,
                                                        DataSource dataSource,
                                                        DataSourceProperties dataSourceProperties) {
        SpringLiquibase liquibase;
        DataSource liquibaseDataSource = getDataSource(liquibaseDatasource, liquibaseProperties, dataSource);
        if (liquibaseDatasource != null) {
            liquibase = new SpringLiquibase();
            liquibase.setDataSource(liquibaseDatasource);
            return liquibase;
        }
        liquibase = new DataSourceClosingSpringLiquibase();
        liquibase.setDataSource(createNewDataSource(liquibaseProperties, dataSourceProperties));
        return liquibase;
    }

    public static SpringLiquibase createAsyncSpringLiquibase(Environment env, Executor executor,
                                                             DataSource liquibaseDatasource, LiquibaseProperties liquibaseProperties,
                                                             DataSource dataSource, DataSourceProperties dataSourceProperties) {
        AsyncSpringLiquibase liquibase;
        DataSource liquibaseDataSource = getDataSource(liquibaseDatasource, liquibaseProperties, dataSource);
        if (liquibaseDataSource != null) {
            liquibase = new AsyncSpringLiquibase(executor, env);
            liquibase.setCloseDataSourceOnceMigrated(false);
            liquibase.setDataSource(liquibaseDataSource);
            return liquibase;
        }
        liquibase = new AsyncSpringLiquibase(executor, env);
        liquibase.setDataSource(createNewDataSource(liquibaseProperties, dataSourceProperties));
        return liquibase;
    }

    private static DataSource getDataSource(DataSource liquibaseDataSource, LiquibaseProperties liquibaseProperties, DataSource dataSource) {
        if (liquibaseDataSource != null) {
            return liquibaseDataSource;
        }
        if (liquibaseProperties.getUrl() == null && liquibaseProperties.getUser() == null) {
            return dataSource;
        }
        return null;
    }

    private static DataSource createNewDataSource(LiquibaseProperties liquibaseProperties, DataSourceProperties dataSourceProperties) {
        String url = StringUtils.defaultString(liquibaseProperties.getUrl(), dataSourceProperties.determineUrl());
        String user = StringUtils.defaultString(liquibaseProperties.getUser(), dataSourceProperties.determineUsername());
        String password = StringUtils.defaultString(liquibaseProperties.getPassword(), dataSourceProperties.determinePassword());
        return DataSourceBuilder.create().url(url).username(user).password(password).build();
    }

}
