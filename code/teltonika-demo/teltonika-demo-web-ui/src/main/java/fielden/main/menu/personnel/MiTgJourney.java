package fielden.main.menu.personnel;

import ua.com.fielden.platform.entity.annotation.EntityType;
import ua.com.fielden.platform.sample.domain.TgJourney;
import ua.com.fielden.platform.ui.menu.MiWithConfigurationSupport;

/**
 * Main menu item representing an entity centre for {@link TgJourney}.
 *
 * @author TG Team
 *
 */
@EntityType(TgJourney.class)
public class MiTgJourney extends MiWithConfigurationSupport<TgJourney> {
}