package trainning.api.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import trainning.api.model.UserModel;

@Repository
public interface UserRepository extends CrudRepository<UserModel, Long> {
    UserModel findByUsername(String username);
}
