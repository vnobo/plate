package com.plate.auth.commons.base;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;

/**
 * AbstractDatabase serves as an abstract base class for database-related services within the application.
 * It centralizes common database connection and setup logic by providing initialized instances
 * of JDBC clients and templates, along with a ConversionService, ready for extension by concrete service implementations.
 * This class extends AbstractService which ensures additional initialization steps are performed after bean properties are set.
 *
 * <p>This abstract class autowires the following dependencies:</p>
 * <ul>
 *   <li>{@link JdbcClient}: For low-level JDBC operations.</li>
 *   <li>{@link JdbcTemplate}: Simplifies JDBC operations with a template design pattern.</li>
 *   <li>{@link NamedParameterJdbcTemplate}: Extends JdbcTemplate to use named parameters instead of traditional placeholders.</li>
 *   <li>{@link ConversionService}: Handles conversion between different types when binding parameters or reading results.</li>
 * </ul>
 *
 * <p>Subclasses should implement specific database interaction logic tailored to their needs,
 * leveraging the provided database access tools.</p>
 *
 * <p>Usage Note:</p>
 * <pre>
 * {@code
 * public class MyDatabaseService extends AbstractDatabase {
 *     // Implement your service methods here
 *     // Access JDBC tools using `this.jdbcTemplate`, `this.namedParameterJdbcTemplate`, etc.
 * }
 * }</pre>
 */
public abstract class AbstractDatabase extends AbstractService {

    /**
     * The JDBC client instance used for executing SQL statements and managing
     * database connections within the service. This field is populated by Spring's
     * dependency injection through the {@link #setJdbcClient(JdbcClient)} method.
     * It serves as the primary interface for interacting with the underlying database.
     */
    protected JdbcClient jdbcClient;
    /**
     * JDBC template instance utilized for executing SQL commands and queries against the database.
     * This field is automatically wired by the Spring framework through the {@link #setJdbcTemplate(JdbcTemplate)} method,
     * which injects a configured {@link JdbcTemplate} capable of efficient data access operations.
     */
    protected JdbcTemplate jdbcTemplate;
    /**
     * Instance of NamedParameterJdbcTemplate used for executing SQL queries with named parameters.
     * This field is automatically wired by the Spring framework, facilitating safer and more convenient
     * parameter binding in SQL statements compared to traditional positional parameters.
     */
    protected NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    /**
     * Conversion service used for type conversion operations within the application.
     * This service facilitates binding of parameters and reading of results from the database,
     * ensuring seamless communication between different data types.
     */
    protected ConversionService conversionService;

    /**
     * Sets the JDBC client instance for the service.
     * This method is automatically called by the Spring framework to inject the JDBC client dependency.
     *
     * @param jdbcClient The JDBC client used for executing SQL statements and managing database connections.
     */
    @Autowired
    public void setJdbcClient(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    /**
     * Sets the ConversionService instance to be used by the service.
     * This method is automatically invoked by the Spring framework during bean initialization to inject the ConversionService dependency.
     *
     * @param conversionService The ConversionService responsible for type conversion operations within the application,
     *                          facilitating binding of parameters and reading of results from the database.
     */
    @Autowired
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    /**
     * Sets the JdbcTemplate instance for the service.
     * This method is automatically invoked by the Spring framework's dependency injection to provide the JdbcTemplate bean,
     * enabling the service to execute SQL commands and queries efficiently.
     *
     * @param jdbcTemplate The JdbcTemplate that simplifies JDBC operations and provides high-level data access functions.
     */
    @Autowired
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Sets the NamedParameterJdbcTemplate instance for the service.
     * This method is automatically called by the Spring framework to inject the NamedParameterJdbcTemplate dependency,
     * which allows for executing SQL queries with named parameters.
     *
     * @param namedParameterJdbcTemplate The NamedParameterJdbcTemplate used for executing parameterized SQL statements
     *                                   with named parameters, providing a more convenient and safer way to bind query variables.
     */
    @Autowired
    public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
    }

    /**
     * Callback method that is invoked by the Spring container once all properties
     * of this Bean have been set. This method is part of the lifecycle interface
     * for beans that need to react once all their properties have been initialized.
     * It calls the superclass's {@code afterPropertiesSet} method to ensure
     * any additional setup defined there is also executed.
     */
    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
    }
}