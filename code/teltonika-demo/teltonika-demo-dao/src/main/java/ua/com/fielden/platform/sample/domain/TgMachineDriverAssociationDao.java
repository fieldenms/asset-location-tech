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
import ua.com.fielden.platform.utils.Validators;

/**
 * DAO implementation for companion object {@link MachineDriverAssociationCo}.
 *
 * @author TG Team
 *
 */
@EntityType(TgMachineDriverAssociation.class)
public class TgMachineDriverAssociationDao extends CommonEntityDao<TgMachineDriverAssociation> implements TgMachineDriverAssociationCo {
    protected static final Logger LOGGER = org.apache.logging.log4j.LogManager.getLogger(TgMachineDriverAssociationDao.class);
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");

    @Inject
    public TgMachineDriverAssociationDao(final IFilter filter) {
        super(filter);
    }

    @Override
    @SessionRequired
    public void delete(final TgMachineDriverAssociation entity) {
        defaultDelete(entity);
    }

    @Override
    @SessionRequired
    public void delete(final EntityResultQueryModel<TgMachineDriverAssociation> model, final Map<String, Object> paramValues) {
        defaultDelete(model, paramValues);
    }

    @Override
    @SessionRequired
    public TgMachineDriverAssociation regularSave(final TgMachineDriverAssociation entity) {
        return super.save(entity);
    }

    @Override
    @SessionRequired
    public TgMachineDriverAssociation save(final TgMachineDriverAssociation entity) {
        final Date serverCurrentDate = new Date();
        //final Result result;
        final TgMachineDriverAssociation assoc = entity;
        if (assoc.isPersisted()) {
            assoc.setChanged(serverCurrentDate);
            assoc.setChangedBy(getUser());

            final Exception ex = validateChangedMachineAssociation(assoc, serverCurrentDate);
            if (ex != null) {
                LOGGER.error(ex.getMessage(), ex);
                throw Result.failure(ex);
            } else {
                try {
                    final TgMachineDriverAssociation saved = super.save(assoc);
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
                    final TgMachineDriverAssociation saved = super.save(assoc);
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

    public Exception validateOverlappingWithExistingHistory(final TgMachineDriverAssociation assoc) {
        final TgMachineDriverAssociation overlappedMachineOrientedAssociation = Validators.findFirstOverlapping(assoc, associationsFetchModel(), this, "from", "to", "machine");
        if (overlappedMachineOrientedAssociation != null) {
            final IllegalStateException e = new IllegalStateException("Vehicle can not have several Drivers associated simultaneously. New association [" + assoc + " -> "
                    + toString(assoc.getTo()) + "] intersects with existing [" + overlappedMachineOrientedAssociation + " -> "
                    + toString(overlappedMachineOrientedAssociation.getTo()) + "].");
            LOGGER.error(e.getMessage(), e);
            return e;
        }
        return null;
    }

    private Exception validateInitialisationOfProps(final TgMachineDriverAssociation assoc, final List<String> shouldBeNotNull, final List<String> shouldBeNull) {
        for (final String prop : shouldBeNotNull) {
            if (assoc.get(prop) == null) {
                final IllegalStateException e = new IllegalStateException("Association should have prop [" + prop + "] initialised.");
                LOGGER.error(e.getMessage(), e);
                return e;
            }
        }
        for (final String prop : shouldBeNull) {
            if (assoc.get(prop) != null) {
                final IllegalStateException e = new IllegalStateException("Association should not have prop [" + prop + "] initialised.");
                LOGGER.error(e.getMessage(), e);
                return e;
            }
        }
        return null;
    }

    /**
     * Validates 'new' association to be correct from perspective of driver history and machine history.
     *
     * @param assoc
     * @param now
     * @return
     */
    public Exception validateNewMachineAssociation(final TgMachineDriverAssociation assoc, final Date now) {
        final Exception initialisationEx = validateInitialisationOfProps(assoc, Arrays.asList("machine", "driver", "from", "created", "createdBy"), Arrays.asList("to", "changed", "changedBy"));
        if (initialisationEx != null) {
            return initialisationEx;
        }
        final Exception overlappingEx = validateOverlappingWithExistingHistory(assoc);
        if (overlappingEx != null) {
            return overlappingEx;
        }
        return validateUnsupportedNewAssociationInThePast(assoc, now);
    }

    private Exception validateUnsupportedNewAssociationInThePast(final TgMachineDriverAssociation assoc, final Date now) {
        if (assoc.getFrom().getTime() <= now.getTime()) {
            final IllegalStateException e = new IllegalStateException("Vehicle association with Driver in the PAST is not supported. New association [" + assoc + " -> "
                    + toString(assoc.getTo()) + "] has From Date in the past [ < " + toString(now) + "].");
            LOGGER.error(e.getMessage(), e);
            return e;
        }
        return null;
    }

    private Exception validateUnsupportedChangedAssociationInThePast(final TgMachineDriverAssociation assoc, final Date now) {
        if (assoc.getTo().getTime() <= now.getTime()) {
            final IllegalStateException e = new IllegalStateException("Vehicle dissociation with Driver in the PAST is not supported. New association [" + assoc + " -> "
                    + toString(assoc.getTo()) + "] has To Date in the past [ < " + toString(now) + "].");
            LOGGER.error(e.getMessage(), e);
            return e;
        }
        return null;
    }

    /**
     * Validates 'changed' association to be correct from perspective of driver history and machine history.
     * <p>
     * Note: in this case changing of "to" property (with "changedBy" and "changed" and "desc") is supported (i.e. closing the association period). All other properties are
     * immutable.
     *
     * @param assoc
     * @param oldAssoc
     * @return
     */
    public Exception validateChangedMachineAssociation(final TgMachineDriverAssociation assoc, final Date now) {
        final Exception initialisationEx = validateInitialisationOfProps(assoc, Arrays.asList("machine", "driver", "from", "created", "createdBy", "to", "changed", "changedBy"), Arrays.<String> asList());
        if (initialisationEx != null) {
            return initialisationEx;
        }
        final Exception overlappingEx = validateOverlappingWithExistingHistory(assoc);
        if (overlappingEx != null) {
            return overlappingEx;
        }
        return validateUnsupportedChangedAssociationInThePast(assoc, now);
    }

    public static fetch<TgMachineDriverAssociation> associationsFetchModel() {
        // TODO ensure that "driver" and "machine" property is fully fetched! fetchAll(MachineDriverAssociation.class).with("machine", fetchAll(Machine.class).with("driver", fetchAll(Driver.class)));
        return fetchAll(TgMachineDriverAssociation.class);
    }
}