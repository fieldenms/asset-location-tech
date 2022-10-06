package fielden.personnel;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;
import static ua.com.fielden.platform.error.Result.failure;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.fluent.fetch;

import java.util.Optional;

import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.security.user.User;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion for {@link Person}.
 *
 * @author Generated
 *
 */
public interface PersonCo extends IEntityDao<Person> {
    static final IFetchProvider<Person> FETCH_PROVIDER = EntityUtils.fetch(Person.class)
            .with("email", "desc", "user", "title", "employeeNo", "phone", "mobile");
    
    static final java.util.function.Supplier<Result> CURR_PERSON_IS_MISSING = () -> failure("Current person is missing.");
    static final String ERR_THERE_IS_NO_PERSON_ASSOCIATED_WITH_USER = "There is no " + Person.ENTITY_TITLE + " associated with User [%s].";
    
    /** Retrieves current person using the default fetch model. */
    Optional<Person> currentPerson();

    /** Retrieves current person using the fetch model provided. */
    Optional<Person> currentPerson(final fetch<Person> fetchPerson);

    /** Retrieves Person using its associated user. */
    default Person findPersonByUser(final User user) {
        final EntityResultQueryModel<Person> query = select(Person.class).where().prop("user").eq().val(user).model();
        return getEntityOptional(from(query).model()).orElseThrow(() -> Result.failuref(ERR_THERE_IS_NO_PERSON_ASSOCIATED_WITH_USER, user));
    }

}