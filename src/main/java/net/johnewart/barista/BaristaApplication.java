package net.johnewart.barista;

import com.basho.riak.client.core.RiakCluster;
import com.basho.riak.client.core.RiakNode;
import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.views.ViewBundle;
import net.johnewart.barista.auth.ChefAuthProvider;
import net.johnewart.barista.auth.ChefAuthenticator;
import net.johnewart.barista.config.RedisConfiguration;
import net.johnewart.barista.config.RiakConfiguration;
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
import net.johnewart.barista.filters.RequestSizeFilter;
import net.johnewart.barista.resources.*;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.core.CoreContainer;
import org.eclipse.jetty.rewrite.handler.RewriteHandler;
import org.eclipse.jetty.rewrite.handler.RewriteRegexRule;
import org.eclipse.jetty.servlet.FilterHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.DispatcherType;
import java.net.UnknownHostException;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;

public class BaristaApplication extends Application<BaristaConfiguration> {
    private final static String RIAK = "riak";
    private final static String REDIS = "redis";
    private final static String MEMORY = "memory";
    private final static Logger LOG = LoggerFactory.getLogger(BaristaApplication.class);

    public static void main(String[] args) throws Exception {
        new BaristaApplication().run(args);
    }

    @Override
    public String getName() {
        return "barista";
    }

    @Override
    public void initialize(Bootstrap<BaristaConfiguration> bootstrap) {
        bootstrap.addBundle(new AssetsBundle());
        bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(BaristaConfiguration configuration,
                    Environment environment) throws ClassNotFoundException, UnknownHostException {

        final FileStorageEngine fileStorageEngine = new OnDiskFileStorageEngine("/tmp/chef");

        final NodeDAO nodeDAO;
        final CookbookDAO cookbookDAO;
        final EnvironmentDAO environmentDAO;
        final SandboxDAO sandboxDAO;
        final RoleDAO roleDAO;
        final DatabagDAO databagDAO;
        final UserDAO userDAO;
        final ClientDAO clientDAO;


        switch(configuration.getStorageEngine()) {
            case REDIS:
                LOG.info("Using Redis for persistence");
                final JedisPool jedisPool = setupRedis(configuration.getRedisConfiguration());
                clientDAO = new RedisClientDAO(jedisPool);
                cookbookDAO = new RedisCookbookDAO(jedisPool);
                nodeDAO = new RedisNodeDAO(jedisPool);
                environmentDAO = new RedisEnvironmentDAO(jedisPool);
                sandboxDAO = new RedisSandboxDAO(jedisPool);
                roleDAO = new RedisRoleDAO(jedisPool);
                databagDAO = new RedisDatabagDAO(jedisPool);
                userDAO = new RedisUserDAO(jedisPool);
                break;
            case RIAK:
                LOG.info("Using Riak for persistence");
                final RiakCluster cluster = setUpCluster(configuration.getRiakConfiguration());
                clientDAO = new RiakClientDAO(cluster);
                nodeDAO = new RiakNodeDAO(cluster);
                cookbookDAO = new RiakCookbookDAO(cluster);
                environmentDAO = new RiakEnvironmentDAO(cluster);
                sandboxDAO = new RiakSandboxDAO(cluster);
                roleDAO = new RiakRoleDAO(cluster);
                databagDAO = new RiakDatabagDAO(cluster);
                userDAO = new RiakUserDAO(cluster);
                break;
            case MEMORY:
            default:
                LOG.info("Using in-memory persistence");
                nodeDAO = new MemoryNodeDAO();
                cookbookDAO = new MemoryCookbookDAO();
                environmentDAO = new MemoryEnvironmentDAO();
                sandboxDAO = new MemorySandboxDAO();
                roleDAO = new MemoryRoleDAO();
                databagDAO = new MemoryDatabagDAO();
                userDAO = new MemoryUserDAO();
                clientDAO = new MemoryClientDAO();
        }


        setupClients(clientDAO);
        setupEnvironments(environmentDAO);
        setupUsers(userDAO);

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
        environment.jersey().register(new AdminResource(userDAO, cookbookDAO, clientDAO));
    }

    // TODO: Support multiple hosts
    private static JedisPool setupRedis(RedisConfiguration config) {
        return new JedisPool(new JedisPoolConfig(), config.getHost(), config.getPort());
    }


    // TODO: Support heterogeneous ports
    private static RiakCluster setUpCluster(RiakConfiguration config) throws UnknownHostException {
        List<RiakNode> nodeList = new LinkedList<>();
        Integer riakPort = config.getPort();

        for(String hostname : config.getHosts()) {
            LOG.info("Adding " + hostname + ":" + riakPort + " to Riak cluster config.");
            RiakNode node = new RiakNode.Builder()
                    .withRemoteAddress(hostname)
                    .withRemotePort(riakPort)
                    .build();
            nodeList.add(node);
        }

        RiakCluster cluster = new RiakCluster.Builder(nodeList)
                .build();

        cluster.start();

        return cluster;
    }

    private void setupClients(ClientDAO clientDAO) {
        String[] clients = {"admin", "chef-webui", "chef-validator"};

        for(String clientName : clients) {
            if (clientDAO.getByName(clientName) == null) {
                Client client = new Client(clientName);
                client.generateKeys();
                clientDAO.store(client);
            }
        }
    }


    private void setupEnvironments(EnvironmentDAO environmentDAO) {
        // Initialize _default environment
        if(environmentDAO.getByName("_default") == null) {
            net.johnewart.barista.core.Environment defaultEnv = new net.johnewart.barista.core.Environment("_default");
            environmentDAO.store(defaultEnv);
        }
    }

    private void setupUsers(UserDAO userDAO) {
        // Create initial admin user
        if (userDAO.getByName("admin") == null) {
            User adminUser = new User("admin");
            adminUser.setAdmin(true);
            adminUser.generateKeys();
            userDAO.store(adminUser);
        }
    }

}
