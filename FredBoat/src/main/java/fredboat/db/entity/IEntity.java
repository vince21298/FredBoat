package fredboat.db.entity;

/**
 * Created by napster on 02.05.17.
 * <p>
 * Just fucking around with generics
 * Implement this in all entities to retrieve them easily over a shared function
 * in EntityReader while having some type safety
 */
public interface IEntity {

    void setId(String id);
}
