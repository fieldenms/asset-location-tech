package ua.com.fielden.platform.sample.domain;

import java.util.Optional;

import ua.com.fielden.platform.dao.IEntityDao;
import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.types.either.Either;

/** 
 * Companion object for entity {@link TgMessage}.
 * 
 * @author Developers
 *
 */
public interface ITgMessage extends IEntityDao<TgMessage> {
    Either<Long, TgMessage> save(final TgMessage entity, final Optional<fetch<TgMessage>> maybeFetch);
}