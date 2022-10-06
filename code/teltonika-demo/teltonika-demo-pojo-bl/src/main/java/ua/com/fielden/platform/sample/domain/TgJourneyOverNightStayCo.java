package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link TgJourneyOverNightStay}.
 *
 * @author TG Team
 *
 */
public interface TgJourneyOverNightStayCo extends IEntityDao<TgJourneyOverNightStay> {

    static final IFetchProvider<TgJourneyOverNightStay> FETCH_PROVIDER = EntityUtils.fetch(TgJourneyOverNightStay.class).with("key", "desc");

}