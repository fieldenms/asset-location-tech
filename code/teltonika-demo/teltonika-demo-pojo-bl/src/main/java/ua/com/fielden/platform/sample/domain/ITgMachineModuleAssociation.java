package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.dao.IEntityDao;

/** 
 * Companion object for entity {@link TgModule}.
 * 
 * @author Developers
 *
 */
public interface ITgMachineModuleAssociation extends IEntityDao<TgMachineModuleAssociation> {

    TgMachineModuleAssociation regularSave(final TgMachineModuleAssociation entity);

}