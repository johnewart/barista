package net.johnewart.barista;

import com.sun.jersey.api.core.ResourceConfig;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.jdbi.DBIFactory;
import io.dropwizard.migrations.MigrationsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.johnewart.barista.data.CookbookDAO;
import net.johnewart.barista.data.EnvironmentDAO;
import net.johnewart.barista.data.NodeDAO;
import net.johnewart.barista.data.SandboxDAO;
import net.johnewart.barista.data.memory.MemoryCookbookDAO;
import net.johnewart.barista.data.memory.MemoryEnvironmentDAO;
import net.johnewart.barista.data.memory.MemoryNodeDAO;
import net.johnewart.barista.data.memory.MemorySandboxDAO;
import net.johnewart.barista.exceptions.ChefAPIExceptionMapper;
import net.johnewart.barista.exceptions.JSONMappingExceptionHandler;
import net.johnewart.barista.filters.OpscodeAuthFilter;
import net.johnewart.barista.filters.RequestSizeFilter;
import net.johnewart.barista.resources.*;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.skife.jdbi.v2.DBI;

import javax.servlet.DispatcherType;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

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

        FilterHolder requestSizeFilter = new FilterHolder(new RequestSizeFilter());
        environment.getApplicationContext().addFilter(requestSizeFilter, "/*", EnumSet.of(DispatcherType.REQUEST));
        FilterHolder opscodeAuthFilter = new FilterHolder(new OpscodeAuthFilter());
        environment.getApplicationContext().addFilter(opscodeAuthFilter, "/environments/*", EnumSet.of(DispatcherType.REQUEST));


        environment.jersey().register(new ChefAPIExceptionMapper());
        environment.jersey().register(new JSONMappingExceptionHandler());
        environment.jersey().register(new NodeResource(nodeDAO));
        environment.jersey().register(new ClientResource());
        environment.jersey().register(new UserResource());
        environment.jersey().register(new EnvironmentResource(cookbookDAO, environmentDAO));
        environment.jersey().register(new CookbookResource(cookbookDAO));
        environment.jersey().register(new SandboxResource(sandboxDAO));
    }

}
