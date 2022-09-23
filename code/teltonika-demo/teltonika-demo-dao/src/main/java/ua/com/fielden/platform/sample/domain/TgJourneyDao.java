package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.security.user.IUser;
import ua.com.fielden.platform.security.user.IUserProvider;
import ua.com.fielden.platform.security.user.User;

/**
 * DAO implementation for companion object {@link TgJourneyCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgJourney.class)
public class TgJourneyDao extends CommonEntityDao<TgJourney> implements TgJourneyCo {
    private final IUserProvider userProvider;
    private final IUser userCo;

    @Inject
    public TgJourneyDao(final IFilter filter, final IUserProvider userProvider, final IUser userCo) {
        super(filter);
        this.userProvider = userProvider;
        this.userCo = userCo;
    }

    @Override
    @SessionRequired
    //@Authorise(TgJourney_CanSave_Token.class)
    public TgJourney save(final TgJourney entity) {
        // saving of TgJourneys on Netty GPS server threads still requires user;
        // this is because TgJourney is persistent entity with 'createdBy' property and will be later adjusted by real users;
        if (getUser() == null) {
            userProvider.setUsername(User.system_users.SU.toString(), userCo);
        }
        return super.save(entity);
    }

    @Override
    protected IFetchProvider<TgJourney> createFetchProvider() {
        return FETCH_PROVIDER;
    }

    @Override
    public TgJourney new_() {
        return super.new_()
            .setBusiness(true)
            .setPurpose(co(TgJourneyPurpose.class).findByKey("BM"))
            .setOverNightStay(co(TgJourneyOverNightStay.class).findByKey("HM"));
    }

}