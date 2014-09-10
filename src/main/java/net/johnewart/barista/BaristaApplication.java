package net.johnewart.barista;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import net.johnewart.barista.auth.ChefAuthProvider;
import net.johnewart.barista.auth.ChefAuthenticator;
import net.johnewart.barista.core.Client;
import net.johnewart.barista.core.User;
import net.johnewart.barista.data.*;
import net.johnewart.barista.data.memory.*;
import net.johnewart.barista.data.redis.*;
import net.johnewart.barista.data.storage.FileStorageEngine;
import net.johnewart.barista.data.storage.OnDiskFileStorageEngine;
import net.johnewart.barista.exceptions.ChefAPIExceptionMapper;
import net.johnewart.barista.exceptions.JSONMappingExceptionHandler;
import net.johnewart.barista.filters.OpscodeAuthFilter;
import net.johnewart.barista.filters.RequestSizeFilter;
import net.johnewart.barista.resources.*;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.servlet.FilterHolder;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

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
        final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

        //final NodeDAO nodeDAO = new MemoryNodeDAO();
        //final CookbookDAO cookbookDAO = new MemoryCookbookDAO();
        //final EnvironmentDAO environmentDAO = new MemoryEnvironmentDAO();
        //final SandboxDAO sandboxDAO = new MemorySandboxDAO();
        //final RoleDAO roleDAO = new MemoryRoleDAO();
        //final DatabagDAO databagDAO = new MemoryDatabagDAO();
        //final UserDAO userDAO = new MemoryUserDAO();
        //final ClientDAO clientDAO = new MemoryClientDAO();

        final FileStorageEngine fileStorageEngine = new OnDiskFileStorageEngine("/tmp/chef");

        final ClientDAO clientDAO = new RedisClientDAO(jedisPool);
        final CookbookDAO cookbookDAO = new RedisCookbookDAO(jedisPool);
        final NodeDAO nodeDAO = new RedisNodeDAO(jedisPool);
        final EnvironmentDAO environmentDAO = new RedisEnvironmentDAO(jedisPool);
        final SandboxDAO sandboxDAO = new RedisSandboxDAO(jedisPool);
        final RoleDAO roleDAO = new RedisRoleDAO(jedisPool);
        final DatabagDAO databagDAO = new RedisDatabagDAO(jedisPool);
        final UserDAO userDAO = new RedisUserDAO(jedisPool);

        // Init some things
        Client adminClient = new Client("admin");
        clientDAO.store(adminClient);
        Client webuiClient = new Client("chef-webui");
        clientDAO.store(webuiClient);
        Client validator = new Client("chef-validator");
        clientDAO.store(validator);
        // Initialize _default environment
        net.johnewart.barista.core.Environment defaultEnv = new net.johnewart.barista.core.Environment("_default");
        environmentDAO.store(defaultEnv);
        // Create initial admin user
        User adminUser = new User("admin");
        userDAO.store(adminUser);


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
        CoreContainer coreContainer = new CoreContainer("/tmp/chef-solr");
        SolrServer solrServer = new EmbeddedSolrServer(coreContainer, "barista");

        environment.jersey().register(new ChefAuthProvider<>(new ChefAuthenticator()));
        environment.jersey().register(new ChefAPIExceptionMapper());
        environment.jersey().register(new JSONMappingExceptionHandler());
        environment.jersey().register(new NodeResource(nodeDAO));
        environment.jersey().register(new ClientResource(clientDAO));
        environment.jersey().register(new UserResource(userDAO));
        environment.jersey().register(new AuthResource(userDAO));
        environment.jersey().register(new EnvironmentResource(cookbookDAO, environmentDAO, roleDAO));
        environment.jersey().register(new CookbookResource(cookbookDAO, fileStorageEngine));
        environment.jersey().register(new SandboxResource(sandboxDAO, fileStorageEngine));
        environment.jersey().register(new RoleResource(roleDAO));
        environment.jersey().register(new SearchResource(solrServer));
        environment.jersey().register(new DatabagResource(databagDAO));
        environment.jersey().register(new FileStoreResource(fileStorageEngine));
    }

}
