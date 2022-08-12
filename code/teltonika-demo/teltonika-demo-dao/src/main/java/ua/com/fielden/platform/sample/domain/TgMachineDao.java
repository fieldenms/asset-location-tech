package ua.com.fielden.platform.sample.domain;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
/** 
 * DAO implementation for companion object {@link ITgMachine}.
 * 
 * @author Developers
 *
 */
@EntityType(TgMachine.class)
public class TgMachineDao extends CommonEntityDao<TgMachine> implements ITgMachine {

    @Inject
    public TgMachineDao(final IFilter filter) {
        super(filter);
    }

    @Override
    protected IFetchProvider<TgMachine> createFetchProvider() {
        // needed for autocompletion of 'this' property on corresponding centre
        return super.createFetchProvider().with("key", "desc");
    }
}