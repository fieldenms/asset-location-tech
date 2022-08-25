package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;

/** 
 * DAO implementation for companion object {@link ITgTempMessage}.
 * 
 * @author Developers
 *
 */
@EntityType(TgTempMessage.class)
public class TgTempMessageDao extends CommonEntityDao<TgTempMessage> implements ITgTempMessage {

    @Inject
    public TgTempMessageDao(final IFilter filter) {
        super(filter);
    }
    
    @Override
    protected IFetchProvider<TgTempMessage> createFetchProvider() {
        return super.createFetchProvider().with("machine.desc", "gpsTime", "travelledDistance", "vectorAngle", "vectorSpeed", "x", "y");
    }

}