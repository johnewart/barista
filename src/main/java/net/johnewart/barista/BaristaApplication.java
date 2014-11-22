package net.johnewart.barista;

import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
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
import net.johnewart.barista.data.riak.*;
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
import java.net.UnknownHostException;
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
        bootstrap.addBundle(new AssetsBundle("/assets", "/assets"));
    }

    @Override
    public void run(BaristaConfiguration configuration,
                    Environment environment) throws ClassNotFoundException, UnknownHostException {

        //final DBIFactory factory = new DBIFactory();
        //final DBI jdbi = factory.build(environment, configuration.getDataSourceFactory(), "postgresql");
        final JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost", 6379);

        final FileStorageEngine fileStorageEngine = new OnDiskFileStorageEngine("/tmp/chef");

        final NodeDAO nodeDAO;
        final CookbookDAO cookbookDAO;
        final EnvironmentDAO environmentDAO;
        final SandboxDAO sandboxDAO;
        final RoleDAO roleDAO;
        final DatabagDAO databagDAO;
        final UserDAO userDAO;
        final ClientDAO clientDAO;
        boolean redis = false;
        boolean riak = true;


        if(redis) {
            clientDAO = new RedisClientDAO(jedisPool);
            cookbookDAO = new RedisCookbookDAO(jedisPool);
            nodeDAO = new RedisNodeDAO(jedisPool);
            environmentDAO = new RedisEnvironmentDAO(jedisPool);
            sandboxDAO = new RedisSandboxDAO(jedisPool);
            roleDAO = new RedisRoleDAO(jedisPool);
            databagDAO = new RedisDatabagDAO(jedisPool);
            userDAO = new RedisUserDAO(jedisPool);
        } else if (riak) {
            final RiakCluster cluster = setUpCluster();
            clientDAO = new RiakClientDAO(cluster);
            nodeDAO = new RiakNodeDAO(cluster);
            cookbookDAO = new RiakCookbookDAO(cluster);
            environmentDAO = new RiakEnvironmentDAO(cluster);
            sandboxDAO = new RiakSandboxDAO(cluster);
            roleDAO = new RiakRoleDAO(cluster);
            databagDAO = new RiakDatabagDAO(cluster);
            userDAO = new RiakUserDAO(cluster);
        } else {
            nodeDAO = new MemoryNodeDAO();
            cookbookDAO = new MemoryCookbookDAO();
            environmentDAO = new MemoryEnvironmentDAO();
            sandboxDAO = new MemorySandboxDAO();
            roleDAO = new MemoryRoleDAO();
            databagDAO = new MemoryDatabagDAO();
            userDAO = new MemoryUserDAO();
            clientDAO = new MemoryClientDAO();
        }

        // Init some things
        Client adminClient = new Client("admin");
        adminClient.generateKeys();
        clientDAO.store(adminClient);

        Client webuiClient = new Client("chef-webui");
        webuiClient.generateKeys();
        clientDAO.store(webuiClient);

        Client validator = new Client("chef-validator");
        validator.generateKeys();
        clientDAO.store(validator);

        // Initialize _default environment
        net.johnewart.barista.core.Environment defaultEnv = new net.johnewart.barista.core.Environment("_default");
        environmentDAO.store(defaultEnv);
        // Create initial admin user
        User adminUser = new User("admin");
        adminUser.setAdmin(true);
        userDAO.store(adminUser);


        FilterHolder requestSizeFilter = new FilterHolder(new RequestSizeFilter());
        environment.getApplicationContext().addFilter(requestSizeFilter, "/*", EnumSet.of(DispatcherType.REQUEST));

        /*
        FilterHolder opscodeAuthFilter = new FilterHolder(new OpscodeAuthFilter(userDAO));
        environment.getApplicationContext().addFilter(opscodeAuthFilter, "/environments/*", EnumSet.of(DispatcherType.REQUEST));
          */
        RewriteHandler rewrite = new RewriteHandler();
        RewriteRegexRule regex = new RewriteRegexRule();
        regex.setRegex("(//+)(.*)");
        regex.setReplacement("/$2");
        rewrite.addRule(regex);
        environment.getApplicationContext().setHandler(rewrite);
        //environment.jersey().register(rewrite);
        CoreContainer coreContainer = new CoreContainer("/tmp/chef-solr");
        SolrServer solrServer = new EmbeddedSolrServer(coreContainer, "barista");

        environment.jersey().register(new ChefAuthProvider<>(new ChefAuthenticator(userDAO)));
        environment.jersey().register(new ChefAPIExceptionMapper());
        environment.jersey().register(new JSONMappingExceptionHandler());
        environment.jersey().register(new NodeResource(nodeDAO));
        environment.jersey().register(new ClientResource(clientDAO));
        environment.jersey().register(new UserResource(userDAO));
        environment.jersey().register(new AuthResource(userDAO));
        environment.jersey().register(new EnvironmentResource(cookbookDAO, environmentDAO, roleDAO));
        environment.jersey().register(new CookbookResource(cookbookDAO, fileStorageEngine));
        environment.jersey().register(new SandboxResource(sandboxDAO, fileStorageEngine));
        environment.jersey().register(new RoleResource(roleDAO, environmentDAO));
        environment.jersey().register(new SearchResource(solrServer));
        environment.jersey().register(new DatabagResource(databagDAO));
        environment.jersey().register(new FileStoreResource(fileStorageEngine));
    }

    // This will create a client object that we can use to interact with Riak
    private static RiakCluster setUpCluster() throws UnknownHostException {
        // This example will use only one node listening on localhost:10017
        RiakNode node = new RiakNode.Builder()
                .withRemoteAddress("127.0.0.1")
                .withRemotePort(8087)

                .build();

        // This cluster object takes our one node as an argument
        RiakCluster cluster = new RiakCluster.Builder(node)
                .build();

        // The cluster must be started to work, otherwise you will see errors
        cluster.start();

        return cluster;
    }

}
