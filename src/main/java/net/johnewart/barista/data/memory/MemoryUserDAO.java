package net.johnewart.barista.data.memory;

import com.google.common.collect.ImmutableList;
import net.johnewart.barista.core.User;
import net.johnewart.barista.data.UserDAO;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryUserDAO implements UserDAO {
    private final ConcurrentHashMap<String, User> userMap;

    public MemoryUserDAO() {
        userMap = new ConcurrentHashMap<>();
        User adminUser = new User("admin");
        userMap.put("admin", adminUser);
    }

    @Override
    public List<User> findAll() {
        return ImmutableList.copyOf(userMap.values());
    }

    @Override
    public void store(User user) {
        userMap.put(user.getName(), user);
    }

    @Override
    public User removeByName(String userName) {
        return userMap.remove(userName);
    }

    @Override
    public void removeAll() {
        userMap.clear();
    }

    @Override
    public User getByName(String userName) {
        User user = userMap.get(userName);
        if(user != null) {
            return new User(user);
        } else {
            return null;
        }
    }
}
