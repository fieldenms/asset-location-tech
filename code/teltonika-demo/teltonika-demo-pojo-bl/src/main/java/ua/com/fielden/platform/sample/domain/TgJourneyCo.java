package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.utils.EntityUtils;

/**
 * Companion object for entity {@link TgJourney}.
 *
 * @author TG Team
 *
 */
public interface TgJourneyCo extends IEntityDao<TgJourney> {

    static final IFetchProvider<TgJourney> FETCH_PROVIDER = EntityUtils.fetch(TgJourney.class).with(
        "desc",
        "machine",
        "startDate",
        "finishDate",
        "startOdometer",
        "finishOdometer",
        "startAddress",
        "finishAddress",
        "business",
        "distance",
        "businessDistance",
        "privateDistance",
        "driver",
        "purpose",
        "overNightStay",
        "active",
        "startLatitude",
        "startLongitude",
        "finishLatitude",
        "finishLongitude"
    );

}