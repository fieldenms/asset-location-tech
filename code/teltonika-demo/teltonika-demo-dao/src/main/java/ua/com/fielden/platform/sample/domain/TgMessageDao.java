package ua.com.fielden.platform.sample.domain;

import static java.util.Optional.of;

import java.util.Optional;

import com.google.inject.Inject;

import ua.com.fielden.platform.dao.CommonEntityDao;
import ua.com.fielden.platform.dao.annotations.SessionRequired;
import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.entity.fetch.IFetchProvider;
import ua.com.fielden.platform.entity.query.IFilter;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.sample.domain.observables.TgMessageChangeSubject;
import ua.com.fielden.platform.types.either.Either;
/** 
 * DAO implementation for companion object {@link ITgMessage}.
 * 
 * @author Developers
 *
 */
@EntityType(TgMessage.class)
public class TgMessageDao extends CommonEntityDao<TgMessage> implements ITgMessage {
    private final TgMessageChangeSubject changeSubject;

    @Inject
    public TgMessageDao(final IFilter filter, final TgMessageChangeSubject changeSubject) {
        super(filter);
        this.changeSubject = changeSubject;
    }
    
    @Override
    protected IFetchProvider<TgMessage> createFetchProvider() {
        return super.createFetchProvider().with(
            "machine.desc",
            "gpsTime",
            "x",
            "y",
            "vectorAngle",
            "vectorSpeed",
            "altitude",
            "visibleSattelites",
            "din1",
            "powerSupplyVoltage",
            "batteryVoltage",
            "gpsPower",
            "packetReceived",
            "ignition",
            "totalOdometer",
            "tripOdometer",
            "trip"
        );
    }

    @Override
    @SessionRequired
    public Either<Long, TgMessage> save(final TgMessage entity, final Optional<fetch<TgMessage>> maybeFetch) {
        final var result = super.save(entity, maybeFetch);
        changeSubject.publish(result.isRight() ? result.asRight().value : entity);
        return result;
    }

    @Override
    @SessionRequired
    public TgMessage save(final TgMessage entity) {
        return save(entity, of(getFetchProvider().fetchModel())).asRight().value;
    }

}