package uk.gov.pay.ledger.app;

import com.google.inject.Guice;
import com.google.inject.Injector;
import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import uk.gov.pay.ledger.event.EventResource;

public class LedgerApp extends Application<LedgerConfig> {

    public static void main(String[] args) throws Exception {
        new LedgerApp().run(args);
    }

    @Override
    public void initialize(Bootstrap<LedgerConfig> bootstrap){
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false))
        );

        bootstrap.addBundle(new MigrationsBundle<LedgerConfig>() {
            @Override
            public DataSourceFactory getDataSourceFactory(LedgerConfig configuration) {
                return configuration.getDataSourceFactory();
            }
        });
    }

    @Override
    public void run(LedgerConfig config, Environment environment) {
        final Injector injector = Guice.createInjector(new LedgerModule(config, environment, createJdbi(config.getDataSourceFactory())));

        environment.jersey().register(injector.getInstance(EventResource.class));
    }

    private Jdbi createJdbi(DataSourceFactory dataSourceFactory) {
        final Jdbi jdbi = Jdbi.create(
                dataSourceFactory.getUrl(),
                dataSourceFactory.getUser(),
                dataSourceFactory.getPassword()
        );
        jdbi.installPlugin(new SqlObjectPlugin());

        return jdbi;
    }
}
