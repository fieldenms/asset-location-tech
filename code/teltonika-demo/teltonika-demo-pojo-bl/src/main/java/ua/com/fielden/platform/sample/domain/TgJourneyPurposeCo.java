package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link TgJourneyPurpose}.
 *
 * @author TG Team
 *
 */
public interface TgJourneyPurposeCo extends IEntityDao<TgJourneyPurpose> {

    static final IFetchProvider<TgJourneyPurpose> FETCH_PROVIDER = EntityUtils.fetch(TgJourneyPurpose.class).with("key", "desc");

}