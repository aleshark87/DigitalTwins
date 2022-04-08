package connection;

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.ditto.things.model.Features;
import org.eclipse.ditto.things.model.Thing;

import controller.LampSimController;

public class GUIUpdateTask implements Runnable{

    private LampSimController controller;
    private String namespace;
    
    public GUIUpdateTask(LampSimController controller, String namespace) {
        this.controller = controller;
        this.namespace = namespace;
    }
    
    @Override
    public void run() {
        String allFeatureProperties = getThingFeatures().getFeature("status").get().getProperties().get().toString() + "\n";
        
        controller.getView().update(allFeatureProperties); 
    }
     
    private Features getThingFeatures() {
        List<Thing> list = controller.getConnection().getDittoClient().twin()
                                 .search()
                                 .stream(queryBuilder -> queryBuilder.namespace(namespace))
                                 .collect(Collectors.toList());
        return list.get(0).getFeatures().get();
    }
    
    
}
