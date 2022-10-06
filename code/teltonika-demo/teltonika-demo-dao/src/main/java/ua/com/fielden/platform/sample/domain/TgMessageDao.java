package ua.com.fielden.platform.sample.domain;

import java.lang.reflect.Method;
import java.util.Arrays;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.reflection.Reflector;
import ua.com.fielden.platform.reflection.TitlesDescsGetter;
import ua.com.fielden.platform.sample.domain.observables.TgMessageChangeSubject;
/** 
 * DAO implementation for companion object {@link ITgMessage}.
 * 
 * @author Developers
 *
 */
@EntityType(TgMessage.class)
public class TgMessageDao extends CommonEntityDao<TgMessage> implements ITgMessage {
    private final TgMessageChangeSubject changeSubject;

    @Inject
    public TgMessageDao(final IFilter filter, final TgMessageChangeSubject changeSubject) {
        super(filter);
        this.changeSubject = changeSubject;
    }
    
    @Override
    protected IFetchProvider<TgMessage> createFetchProvider() {
        return super.createFetchProvider().with("machine.desc", "gpsTime", "travelledDistance", "vectorAngle", "vectorSpeed", "x", "y");
    }

    @Override
    @SessionRequired
    public TgMessage save(final TgMessage entity) {
        if (!entity.isPersisted()) {
            //System.out.println("x = " + entity.getX() + " y = " + entity.getY());
            final TgMessage saved = super.save(entity);

            //System.out.println("saved x = " + saved.getX() + " saved y = " + saved.getY());
            //final Message loaded = (Message) getSession().load(getEntityType(), saved.getId());
            //System.out.println("loaded x = " + loaded.getX() + " loaded y = " + loaded.getY());
            changeSubject.publish(saved);

            return saved;
        } else {
            try {
                // entity.getDirtyProperties() are ALWAYS empty in case of lightweight Messages!

                // 1. the duplicates validation should be performed earlier

                // If entity with id exists then the returned instance is proxied since it is retrieved using standard Hibernate session get method,
                // and thus is associated with current Hibernate session.
                // This seems to be advantageous since entity validation would work relying on standard Hibernate lazy initialisation.

                final TgMessage persistedEntity = getSession().load(getEntityType(), entity.getId());
                // first check any concurrent modification
                if (persistedEntity.getVersion() != null && persistedEntity.getVersion() > entity.getVersion()) {
                    throw new Result(entity, new IllegalStateException("Cannot save a stale Message " + entity.getKey() + " ("
                            + TitlesDescsGetter.getEntityTitleAndDesc(getEntityType()).getKey() + ") -- another user has changed it."));
                }

                // if there are changes persist them
                // Setting modified values triggers any associated validation.
                // An interesting case is with validation based on associative properties such as a work order for purchase order item.
                // If a purchase order item is being persisted some of its property validation might depend on the state of the associated work order.
                // When this purchase order item was validated at the client side it might have been using a stale work order.
                // In here revalidation occurs, which would definitely work with the latest data.
                for (final String propName : Arrays.asList("travelledDistance" /* TODO "predecessor" does not exist */)) {
                    //          logger.error("is dirty: " + propName + " of " + getEntityType().getSimpleName() + " old = " + ((MetaProperty) obj).getOriginalValue() + " new = " + ((MetaProperty) obj).getValue());
                    final Object value = entity.get(propName);
                    // it is essential that if a property is of an entity type it should be re-associated with the current session before being set
                    // the easiest way to do that is to load entity be id using the current session
//                    if (value instanceof AbstractEntity && !(value instanceof PropertyDescriptor) && !(value instanceof AbstractUnionEntity)) {
//                        final Object loaded = getSession().load(((AbstractEntity) value).getType(), ((AbstractEntity) value).getId());
//                        persistedEntity.set(propName, loaded);
//                    } else {
                    persistedEntity.set(propName, value);
//                    }
                }
                // check if entity is valid after changes
                final Result res = persistedEntity.isValid();
                if (res.isSuccessful()) {
                    // during the update a StaleObjectStateException might be thrown.
                    // getSession().flush();
                    getSession().update(persistedEntity);
                } else {
                    throw res;
                }
                // set incremented version
                try {
                    final Method setVersion = Reflector.getMethod(entity/* .getType() */, "setVersion", Long.class);
                    setVersion.setAccessible(true);
                    setVersion.invoke(entity, entity.getVersion() + 1);
                } catch (final Exception e) {
                    throw new IllegalStateException("Could not set updated entity version.");
                }

                entity.setDirty(false);
                entity.resetMetaState();
            } finally {
                // logger.info("Finished saving entity " + entity + " (ID = " + entity.getId() + ")");
            }

            getSession().flush();
            getSession().clear();

            changeSubject.publish(entity);

            return entity;
        }
    }

}