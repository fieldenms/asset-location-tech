package ua.com.fielden.platform.sample.domain;

import static ua.com.fielden.platform.reflection.TitlesDescsGetter.getEntityTitleAndDesc;

import ua.com.fielden.platform.entity.AbstractPersistentEntity;
import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.DescRequired;
import ua.com.fielden.platform.entity.annotation.DescTitle;
import ua.com.fielden.platform.entity.annotation.DisplayDescription;
import ua.com.fielden.platform.entity.annotation.KeyTitle;
import ua.com.fielden.platform.entity.annotation.KeyType;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.utils.Pair;

/**
 * Represents {@link TgJourney}'s purpose.
 *
 * @author TG Team
 *
 */
@KeyType(String.class)
@KeyTitle("Code")
@CompanionObject(TgJourneyPurposeCo.class)
@MapEntityTo
@DescTitle("Description")
@DisplayDescription
@DescRequired
public class TgJourneyPurpose extends AbstractPersistentEntity<String> {

    private static final Pair<String, String> entityTitleAndDesc = getEntityTitleAndDesc(TgJourneyPurpose.class);
    public static final String ENTITY_TITLE = entityTitleAndDesc.getKey();
    public static final String ENTITY_DESC = entityTitleAndDesc.getValue();

}