package fielden.config;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fielden.personnel.Person;
import ua.com.fielden.platform.basic.config.IApplicationDomainProvider;
import ua.com.fielden.platform.domain.PlatformDomainTypes;
import ua.com.fielden.platform.entity.AbstractEntity;
import ua.com.fielden.platform.sample.domain.TgCoordinate;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineRealtimeMonitorMap;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgMessageMap;
import ua.com.fielden.platform.sample.domain.TgOrgUnit;
import ua.com.fielden.platform.sample.domain.TgPolygon;
import ua.com.fielden.platform.sample.domain.TgPolygonMap;
import ua.com.fielden.platform.sample.domain.TgStop;
import ua.com.fielden.platform.sample.domain.TgStopMap;

/**
 * A class to register domain entities.
 * 
 * @author Generated
 * 
 */
public class ApplicationDomain implements IApplicationDomainProvider {
    private static final Set<Class<? extends AbstractEntity<?>>> entityTypes = new LinkedHashSet<>();
    private static final Set<Class<? extends AbstractEntity<?>>> domainTypes = new LinkedHashSet<>();

    static {
        entityTypes.addAll(PlatformDomainTypes.types);
        add(Person.class);

        add(TgMessage.class);
        add(TgMessageMap.class);

        add(TgOrgUnit.class);
        add(TgMachine.class);
        add(TgMachineRealtimeMonitorMap.class);

        add(TgStop.class);
        add(TgStopMap.class);

        add(TgPolygon.class);
        add(TgCoordinate.class);
        add(TgPolygonMap.class);
    }

    private static void add(final Class<? extends AbstractEntity<?>> domainType) {
        entityTypes.add(domainType);
        domainTypes.add(domainType);
    }

    @Override
    public List<Class<? extends AbstractEntity<?>>> entityTypes() {
        return Collections.unmodifiableList(entityTypes.stream().collect(Collectors.toList()));
    }

    public List<Class<? extends AbstractEntity<?>>> domainTypes() {
        return Collections.unmodifiableList(domainTypes.stream().collect(Collectors.toList()));
    }
}
