package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.error.Result;
import ua.com.fielden.platform.gis.gps.monitoring.impl.ITransporterMachineMonitoringProvider;
import ua.com.fielden.platform.utils.Validators;

/**
 * DAO implementation for companion object {@link IMachineModuleAssociation} based on a common with DAO mixin.
 *
 * @author Developers
 *
 */
@EntityType(TgMachineModuleAssociation.class)
public class TgMachineModuleAssociationDao extends CommonEntityDao<TgMachineModuleAssociation> implements ITgMachineModuleAssociation {
    private final ITransporterMachineMonitoringProvider machineMonitoringProvider;
    protected static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(TgMachineModuleAssociationDao.class);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

    @Inject
    public TgMachineModuleAssociationDao(final IFilter filter, final ITransporterMachineMonitoringProvider machineMonitoringProvider) {
        super(filter);
        this.machineMonitoringProvider = machineMonitoringProvider;
    }

    @Override
    @SessionRequired
    public void delete(final TgMachineModuleAssociation entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgMachineModuleAssociation> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }

    @Override
    @SessionRequired
    public TgMachineModuleAssociation regularSave(final TgMachineModuleAssociation entity) {
        return super.save(entity);
    }

    @Override
    @SessionRequired
    public TgMachineModuleAssociation save(final TgMachineModuleAssociation entity) {
        final Date serverCurrentDate = new Date();
        //final Result result;
        final TgMachineModuleAssociation assoc = entity;
        if (assoc.isPersisted()) {
            assoc.setChanged(serverCurrentDate);
            assoc.setChangedBy(getUser());

            final Exception ex = validateChangedMachineAssociation(assoc, serverCurrentDate);
            if (ex != null) {
                LOGGER.error(ex.getMessage(), ex);
                throw Result.failure(ex);
            } else {
                try {
                    final TgMachineModuleAssociation saved = super.save(assoc);
                    // changed association needs to be promoted to server cache to correctly handle machine / module processing
                    this.machineMonitoringProvider.promoteChangedMachineAssociation(saved);
                    return saved;
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw Result.failure(e);
                }
            }

        } else { // new association
            assoc.setCreated(serverCurrentDate);
            assoc.setCreatedBy(getUser());

            final Exception ex = validateNewMachineAssociation(assoc, serverCurrentDate);
            if (ex != null) {
                LOGGER.error(ex.getMessage(), ex);
                throw Result.failure(ex);
            } else {
                try {
                    final TgMachineModuleAssociation saved = super.save(assoc);
                    // new association needs to be promoted to server cache to correctly handle machine / module processing
                    this.machineMonitoringProvider.promoteNewMachineAssociation(saved);
                    return saved;
                } catch (final Exception e) {
                    LOGGER.error(e.getMessage(), e);
                    throw Result.failure(e);
                }
            }
        }
    }

    private static String toString(final Date date) {
        return date == null ? "+\u221E" : dateFormatter.format(date);
    }

    public Exception validateOverlappingWithExistingHistory(final TgMachineModuleAssociation assoc) {
        final TgMachineModuleAssociation overlappedModuleOrientedAssociation = Validators.findFirstOverlapping(assoc, associationsFetchModel(), this, "from", "to", "module");
        if (overlappedModuleOrientedAssociation != null) {
            final IllegalStateException e = new IllegalStateException("Модуль не може знаходитись на кількох машинах одночасно. Нова асоціація [" + assoc + " до "
                    + toString(assoc.getTo()) + "] перетинається із уже існуючою [" + overlappedModuleOrientedAssociation + " до "
                    + toString(overlappedModuleOrientedAssociation.getTo()) + "].");
            LOGGER.error(e.getMessage(), e);
            return e;
        }
        final TgMachineModuleAssociation overlappedMachineOrientedAssociation = Validators.findFirstOverlapping(assoc, associationsFetchModel(), this, "from", "to", "machine");
        if (overlappedMachineOrientedAssociation != null) {
            final IllegalStateException e = new IllegalStateException("Машина не може мати кілька приєднаних модулів одночасно. Нова асоціація [" + assoc + " до "
                    + toString(assoc.getTo()) + "] перетинається із уже існуючою [" + overlappedMachineOrientedAssociation + " до "
                    + toString(overlappedMachineOrientedAssociation.getTo()) + "].");
            LOGGER.error(e.getMessage(), e);
            return e;
        }
        return null;
    }

    private Exception validateInitialisationOfProps(final TgMachineModuleAssociation assoc, final List<String> shouldBeNotNull, final List<String> shouldBeNull) {
        for (final String prop : shouldBeNotNull) {
            if (assoc.get(prop) == null) {
                final IllegalStateException e = new IllegalStateException("Асоціація повинна мати ініціалізовану властивість [" + prop + "].");
                LOGGER.error(e.getMessage(), e);
                return e;
            }
        }
        for (final String prop : shouldBeNull) {
            if (assoc.get(prop) != null) {
                final IllegalStateException e = new IllegalStateException("Асоціація повинна мати НЕініціалізовану властивість [" + prop + "].");
                LOGGER.error(e.getMessage(), e);
                return e;
            }
        }
        return null;
    }

    /**
     * Validates 'new' association to be correct from perspective of module history and machine history.
     *
     * @param assoc
     * @param now
     * @return
     */
    public Exception validateNewMachineAssociation(final TgMachineModuleAssociation assoc, final Date now) {
        final Exception initialisationEx = validateInitialisationOfProps(assoc, Arrays.asList("machine", "module", "from", "created", "createdBy"), Arrays.asList("to", "changed", "changedBy"));
        if (initialisationEx != null) {
            return initialisationEx;
        }
        final Exception overlappingEx = validateOverlappingWithExistingHistory(assoc);
        if (overlappingEx != null) {
            return overlappingEx;
        }
        return validateUnsupportedNewAssociationInThePast(assoc, now);
    }

    private Exception validateUnsupportedNewAssociationInThePast(final TgMachineModuleAssociation assoc, final Date now) {
        if (assoc.getFrom().getTime() <= now.getTime()) {
            final IllegalStateException e = new IllegalStateException("Асоціація машини з модулем в МИНУЛОМУ не підтримуються. Нова асоціація [" + assoc + " до "
                    + toString(assoc.getTo()) + "] має час початку, який є в минулому [ < " + toString(now) + "].");
            LOGGER.error(e.getMessage(), e);
            return e;
        }
        return null;
    }

    private Exception validateUnsupportedChangedAssociationInThePast(final TgMachineModuleAssociation assoc, final Date now) {
        if (assoc.getTo().getTime() <= now.getTime()) {
            final IllegalStateException e = new IllegalStateException("Від'єднання модуля від машини в МИНУЛОМУ не підтримується. Нова асоціація [" + assoc + " до "
                    + toString(assoc.getTo()) + "] має час завершення, який є в минулому [ < " + toString(now) + "].");
            LOGGER.error(e.getMessage(), e);
            return e;
        }
        return null;
    }

    /**
     * Validates 'changed' association to be correct from perspective of module history and machine history.
     * <p>
     * Note: in this case changing of "to" property (with "changedBy" and "changed" and "desc") is supported (i.e. closing the association period). All other properties are
     * immutable.
     *
     * @param assoc
     * @param oldAssoc
     * @return
     */
    public Exception validateChangedMachineAssociation(final TgMachineModuleAssociation assoc, final Date now) {
        final Exception initialisationEx = validateInitialisationOfProps(assoc, Arrays.asList("machine", "module", "from", "created", "createdBy", "to", "changed", "changedBy"), Arrays.<String> asList());
        if (initialisationEx != null) {
            return initialisationEx;
        }
        final Exception overlappingEx = validateOverlappingWithExistingHistory(assoc);
        if (overlappingEx != null) {
            return overlappingEx;
        }
        return validateUnsupportedChangedAssociationInThePast(assoc, now);
    }

    public static fetch<TgMachineModuleAssociation> associationsFetchModel() {
        // TODO ensure that "module" and "machine" property is fully fetched! fetchAll(MachineModuleAssociation.class).with("machine", fetchAll(Machine.class).with("module", fetchAll(Module.class)));
        return fetchAll(TgMachineModuleAssociation.class);
    }
}