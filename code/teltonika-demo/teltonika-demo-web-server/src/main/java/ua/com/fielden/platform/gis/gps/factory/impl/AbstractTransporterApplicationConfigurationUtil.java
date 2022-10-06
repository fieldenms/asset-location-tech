package ua.com.fielden.platform.gis.gps.factory.impl;

import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.fetchAll;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.from;
import static ua.com.fielden.platform.entity.query.fluent.EntityQueryUtils.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.inject.Injector;

import ua.com.fielden.platform.entity.query.fluent.fetch;
import ua.com.fielden.platform.entity.query.model.AggregatedResultQueryModel;
import ua.com.fielden.platform.entity.query.model.EntityResultQueryModel;
import ua.com.fielden.platform.gis.gps.actors.AbstractActors;
import ua.com.fielden.platform.gis.gps.actors.impl.MachineActor;
import ua.com.fielden.platform.gis.gps.actors.impl.ModuleActor;
import ua.com.fielden.platform.gis.gps.actors.impl.TransporterMachineMonitoringProvider;
import ua.com.fielden.platform.gis.gps.actors.impl.ViolatingMessageResolverActor;
import ua.com.fielden.platform.gis.gps.factory.ApplicationConfigurationUtil;
import ua.com.fielden.platform.gis.gps.monitoring.impl.ITransporterMachineMonitoringProvider;
import ua.com.fielden.platform.sample.domain.ITgMachine;
import ua.com.fielden.platform.sample.domain.ITgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.ITgMessage;
import ua.com.fielden.platform.sample.domain.ITgModule;
import ua.com.fielden.platform.sample.domain.TgMachine;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociation;
import ua.com.fielden.platform.sample.domain.TgMachineModuleAssociationDao;
import ua.com.fielden.platform.sample.domain.TgMessage;
import ua.com.fielden.platform.sample.domain.TgModule;

public abstract class AbstractTransporterApplicationConfigurationUtil extends ApplicationConfigurationUtil<TgMessage, TgMachine, TgModule, TgMachineModuleAssociation, MachineActor, ModuleActor, ViolatingMessageResolverActor> {

    private Map<TgMachine, TgMessage> readLastMessages(final ITgMessage coMessage) {
        final AggregatedResultQueryModel lastGpsTimePerMachine = select(TgMessage.class). //
        groupBy().prop("machine"). //
        yield().prop("machine").as("machine"). //
        yield().maxOf().prop("gpsTime").as("lastGpsTime"). //
        modelAsAggregate();

        final EntityResultQueryModel<TgMessage> lastMessagePerMachine = select(TgMessage.class).as("m").join(lastGpsTimePerMachine).as("l").on().prop("m.machine").eq().prop("l.machine").and().prop("m.gpsTime").eq().prop("l.lastGpsTime").model();

        final Map<TgMachine, TgMessage> result = new HashMap<>();
        for (final TgMessage message : coMessage.getAllEntities(from(lastMessagePerMachine).model())) {
            result.put(message.getMachine(), message);
        }

        return result;
    }

    private Map<TgModule, List<TgMachineModuleAssociation>> fetchModulesWithAssociations(final ITgModule coModule, final ITgMachineModuleAssociation coMachineModuleAssociation) {
        // all associations
        getLogger().info("\t\tMachineModuleAssociations fetching...");
        final fetch<TgMachineModuleAssociation> assocFetch = TgMachineModuleAssociationDao.associationsFetchModel();
        final List<TgMachineModuleAssociation> associations = coMachineModuleAssociation.getAllEntities(from(select(TgMachineModuleAssociation.class).model()).lightweight().with(assocFetch).model());
        getLogger().info("\t\tMachineModuleAssociations [" + associations.size() + "] fetched.");
        // all modules
        getLogger().info("\t\tModules fetching...");
        final fetch<TgModule> moduleFetch = fetchAll(TgModule.class);
        final List<TgModule> modules = coModule.getAllEntities(from(select(TgModule.class).model()).lightweight().with(moduleFetch).model());
        getLogger().info("\t\tModules [" + modules.size() + "] fetched.");

        final Map<TgModule, List<TgMachineModuleAssociation>> cache = new HashMap<>();
        for (final TgModule module : modules) {
            cache.put(module, new ArrayList<TgMachineModuleAssociation>());
        }
        for (final TgMachineModuleAssociation assoc : associations) {
            cache.get(assoc.getModule()).add(assoc);
        }
        return cache;
    }

    private Map<TgMachine, TgMessage> fetchMachinesWithLastMessages(final ITgMachine coMachine, final ITgMessage coMessage) {
        getLogger().info("\t\tMachines fetching...");
        final fetch<TgMachine> machineFetch = fetchAll(TgMachine.class);
        final List<TgMachine> machines = coMachine.getAllEntities(from(select(TgMachine.class).model()).lightweight().with(machineFetch).model());
        getLogger().info("\t\tMachines [" + machines.size() + "] fetched.");

        final Map<TgMachine, TgMessage> cache = new HashMap<>();
        for (final TgMachine machine : machines) {
            cache.put(machine, null);
        }

        getLogger().info("\t\tLast messages fetching...");
        final Map<TgMachine, TgMessage> lastMessages = readLastMessages(coMessage);
        getLogger().info("\t\tLast messages [" + lastMessages.size() + "] fetched.");

        for (final Entry<TgMachine, TgMessage> entry : lastMessages.entrySet()) {
            cache.put(entry.getKey(), entry.getValue());
        }
        return cache;
    }

    @Override
    protected Map<TgMachine, TgMessage> fetchMachinesWithLastMessages(final Injector injector) {
        getLogger().info("\tMachines fetching...");
        final Map<TgMachine, TgMessage> cache = fetchMachinesWithLastMessages(injector.getInstance(ITgMachine.class), injector.getInstance(ITgMessage.class));
        getLogger().info("\tMachines [" + cache.size() + "] fetched.");
        return cache;
    }

    @Override
    protected Map<TgModule, List<TgMachineModuleAssociation>> fetchModulesWithAssociations(final Injector injector) {
        getLogger().info("\tModules fetching...");
        final Map<TgModule, List<TgMachineModuleAssociation>> cache = fetchModulesWithAssociations(injector.getInstance(ITgModule.class), injector.getInstance(ITgMachineModuleAssociation.class));
        getLogger().info("\tModules [" + cache.size() + "] fetched.");
        return cache;
    };

    @Override
    protected void promoteActors(final Injector injector, final AbstractActors<TgMessage, TgMachine, TgModule, TgMachineModuleAssociation, MachineActor, ModuleActor, ViolatingMessageResolverActor> actors) {
        final TransporterMachineMonitoringProvider mmProvider = (TransporterMachineMonitoringProvider) injector.getInstance(ITransporterMachineMonitoringProvider.class);
        mmProvider.setActors(actors);
    }
}
