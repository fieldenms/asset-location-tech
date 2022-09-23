package ua.com.fielden.platform.sample.domain;

import java.util.Collection;
import java.util.List;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/**
 * DAO implementation for companion object {@link TgJourneyOverNightStayCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgJourneyOverNightStay.class)
public class TgJourneyOverNightStayDao extends CommonEntityDao<TgJourneyOverNightStay> implements TgJourneyOverNightStayCo {

    @Inject
    public TgJourneyOverNightStayDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    //@Authorise(TgJourneyOverNightStay_CanSave_Token.class)
    public TgJourneyOverNightStay save(final TgJourneyOverNightStay entity) {
        return super.save(entity);
    }

    @Override
    @SessionRequired
    //@Authorise(TgJourneyOverNightStay_CanDelete_Token.class)
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    @SessionRequired
    //@Authorise(TgJourneyOverNightStay_CanDelete_Token.class)
    public int batchDelete(final List<TgJourneyOverNightStay> entities) {
        return defaultBatchDelete(entities);
    }

    @Override
    protected IFetchProvider<TgJourneyOverNightStay> createFetchProvider() {
        return FETCH_PROVIDER;
    }
}