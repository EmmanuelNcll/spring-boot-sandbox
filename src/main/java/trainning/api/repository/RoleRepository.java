package trainning.api.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import trainning.api.model.RoleModel;

@Repository
public interface RoleRepository extends CrudRepository<RoleModel, Long> {
    RoleModel findByName(String name);
}
