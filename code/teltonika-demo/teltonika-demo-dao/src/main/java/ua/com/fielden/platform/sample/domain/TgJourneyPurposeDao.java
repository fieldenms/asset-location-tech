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
 * DAO implementation for companion object {@link TgJourneyPurposeCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgJourneyPurpose.class)
public class TgJourneyPurposeDao extends CommonEntityDao<TgJourneyPurpose> implements TgJourneyPurposeCo {

    @Inject
    public TgJourneyPurposeDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    //@Authorise(TgJourneyPurpose_CanSave_Token.class)
    public TgJourneyPurpose save(final TgJourneyPurpose entity) {
        return super.save(entity);
    }

    @Override
    @SessionRequired
    //@Authorise(TgJourneyPurpose_CanDelete_Token.class)
    public int batchDelete(final Collection<Long> entitiesIds) {
        return defaultBatchDelete(entitiesIds);
    }

    @Override
    @SessionRequired
    //@Authorise(TgJourneyPurpose_CanDelete_Token.class)
    public int batchDelete(final List<TgJourneyPurpose> entities) {
        return defaultBatchDelete(entities);
    }

    @Override
    protected IFetchProvider<TgJourneyPurpose> createFetchProvider() {
        return FETCH_PROVIDER;
    }
}