package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;

/** 
 * Companion object for entity {@link TgMachineDriverAssociation}.
 * 
 * @author TG Team
 *
 */
public interface TgMachineDriverAssociationCo extends IEntityDao<TgMachineDriverAssociation> {

    TgMachineDriverAssociation regularSave(final TgMachineDriverAssociation entity);

}