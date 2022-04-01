package tasks;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;
import org.eclipse.ditto.things.model.signals.commands.query.RetrieveThing;

import application.SimCar;

public class GUIUpdateTask implements Runnable{

    private SimCar simulationCar;
    private int i;
    
    public GUIUpdateTask(SimCar simulation) {
        this.simulationCar = simulation;
        i = 0;
    }
    
    @Override
    public void run() {
        String allFeatureProperties = getThingFeatures().getFeature("status").get().getProperties().get().toString() + "\n"+ 
                getThingFeatures().getFeature("indicator-light").get().getProperties().get().toString() + "\n"+
                getThingFeatures().getFeature("wear-time").get().getProperties().get().toString();
        
        simulationCar.getController().getView().updateTextArea(allFeatureProperties); 
    }
     
    private Features getThingFeatures() {
        List<Thing> list = simulationCar.getController().getClientConnection().getDittoClient().twin()
                                 .search()
                                 .stream(queryBuilder -> queryBuilder.namespace("io.eclipseprojects.ditto"))
                                 .collect(Collectors.toList());
        return list.get(0).getFeatures().get();
    }
    
    
}
