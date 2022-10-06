package ua.com.fielden.platform.sample.domain;

import ua.com.fielden.platform.entity.annotation.CompanionObject;
import ua.com.fielden.platform.entity.annotation.MapEntityTo;
import ua.com.fielden.platform.gis.gps.AbstractAvlModule;

@CompanionObject(ITgModule.class)
@MapEntityTo
public class TgModule extends AbstractAvlModule {
}