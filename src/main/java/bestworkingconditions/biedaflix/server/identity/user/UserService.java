package bestworkingconditions.biedaflix.server.identity.user;

import bestworkingconditions.biedaflix.server.identity.role.RoleRepository;
import bestworkingconditions.biedaflix.server.common.service.GenericServiceImpl;
import bestworkingconditions.biedaflix.server.identity.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.Valid;

@Service
public class UserService extends GenericServiceImpl<User, UserRepository> {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository repository, UserRepository userRepository, RoleRepository roleRepository, @Lazy PasswordEncoder passwordEncoder) {
        super(repository);
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User create(@Valid User resource) {

        resource.setPassword(passwordEncoder.encode(resource.getPassword()));
        resource.setAccepted(false);

        return repository.save(resource);
    }

    @Override
    public User fetchAndUpdate(String id, User resource) {

        User user = findById(id);

        
    }
}
