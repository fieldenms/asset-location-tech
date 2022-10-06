package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ITgViolatingMessage}.
 * 
 * @author Developers
 *
 */
@EntityType(TgViolatingMessage.class)
public class TgViolatingMessageDao extends CommonEntityDao<TgViolatingMessage> implements ITgViolatingMessage {

    @Inject
    public TgViolatingMessageDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    protected IFetchProvider<TgViolatingMessage> createFetchProvider() {
        return super.createFetchProvider().with("machine.desc", "gpsTime", "travelledDistance", "vectorAngle", "vectorSpeed", "x", "y");
    }

}