package net.johnewart.barista;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.johnewart.barista.auth.ChefAuthProvider;
import net.johnewart.barista.auth.ChefAuthenticator;
import net.johnewart.barista.data.*;
import net.johnewart.barista.data.memory.*;
import net.johnewart.barista.data.storage.FileStorageEngine;
import net.johnewart.barista.data.storage.OnDiskFileStorageEngine;
import net.johnewart.barista.exceptions.ChefAPIExceptionMapper;
import net.johnewart.barista.exceptions.JSONMappingExceptionHandler;
import net.johnewart.barista.filters.OpscodeAuthFilter;
import net.johnewart.barista.filters.RequestSizeFilter;
import net.johnewart.barista.resources.*;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.servlet.FilterHolder;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

public class BaristaApplication extends Application<BaristaConfiguration> {
    public static void main(String[] args) throws Exception {
        new BaristaApplication().run(args);
    }

    @Override
    public String getName() {
        return "barista";
    }

    @Override
    public void initialize(Bootstrap<BaristaConfiguration> bootstrap) {
       /* bootstrap.addBundle(new MigrationsBundle<BaristaConfiguration>() {
            @Override
            public DataSourceFactory getDataSourceFactory(BaristaConfiguration configuration) {
                return configuration.getDataSourceFactory();
            }
        });*/
        bootstrap.addBundle(new AssetsBundle("/assets", "/assets"));

    }

    @Override
    public void run(BaristaConfiguration configuration,
                    Environment environment) throws ClassNotFoundException {

        //final DBIFactory factory = new DBIFactory();
        //final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");
        final NodeDAO nodeDAO = new MemoryNodeDAO();
        final CookbookDAO cookbookDAO = new MemoryCookbookDAO();
        final EnvironmentDAO environmentDAO = new MemoryEnvironmentDAO();
        final SandboxDAO sandboxDAO = new MemorySandboxDAO();
        final RoleDAO roleDAO = new MemoryRoleDAO();
        final DatabagDAO databagDAO = new MemoryDatabagDAO();
        final UserDAO userDAO = new MemoryUserDAO();
        final ClientDAO clientDAO = new MemoryClientDAO();
        final FileStorageEngine fileStorageEngine = new OnDiskFileStorageEngine("/tmp/chef");

        FilterHolder requestSizeFilter = new FilterHolder(new RequestSizeFilter());
        environment.getApplicationContext().addFilter(requestSizeFilter, "/*", EnumSet.of(DispatcherType.REQUEST));
        FilterHolder opscodeAuthFilter = new FilterHolder(new OpscodeAuthFilter());
        environment.getApplicationContext().addFilter(opscodeAuthFilter, "/environments/*", EnumSet.of(DispatcherType.REQUEST));

        RewriteHandler rewrite = new RewriteHandler();
        RewriteRegexRule regex = new RewriteRegexRule();
        regex.setRegex("(//+)(.*)");
        regex.setReplacement("/$2");
        rewrite.addRule(regex);
        environment.getApplicationContext().setHandler(rewrite);
        //environment.jersey().register(rewrite);

        environment.jersey().register(new ChefAuthProvider<>(new ChefAuthenticator()));
        environment.jersey().register(new ChefAPIExceptionMapper());
        environment.jersey().register(new JSONMappingExceptionHandler());
        environment.jersey().register(new NodeResource(nodeDAO));
        environment.jersey().register(new ClientResource(clientDAO));
        environment.jersey().register(new UserResource(userDAO));
        environment.jersey().register(new AuthResource(userDAO));
        environment.jersey().register(new EnvironmentResource(cookbookDAO, environmentDAO, roleDAO));
        environment.jersey().register(new CookbookResource(cookbookDAO));
        environment.jersey().register(new SandboxResource(sandboxDAO, fileStorageEngine));
        environment.jersey().register(new RoleResource(roleDAO));
        environment.jersey().register(new SearchResource());
        environment.jersey().register(new DatabagResource(databagDAO));
        environment.jersey().register(new FileStoreResource(fileStorageEngine));
    }

}
