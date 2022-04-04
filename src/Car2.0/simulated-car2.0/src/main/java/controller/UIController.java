package controller;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import org.eclipse.ditto.json.JsonObject;
import org.eclipse.ditto.json.JsonValue;
import org.eclipse.ditto.things.model.Feature;
import org.eclipse.ditto.things.model.Features;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * The Controller related to the main.fxml GUI.
 *
 */
public final class UIController {
    
    
    @FXML
    private TextArea textArea;
    
    @FXML
    private Label label_11;
    @FXML
    private Label label_12;
    @FXML
    private Label label_13;
    @FXML
    private Label label_21;
    @FXML
    private Label label_22;
    @FXML 
    private Label label_23;
    
    private CarSimController controller;
    
    @FXML
    private void initialize() {
        clearAllLabels();
        controller = new CarSimController(this);
    }
    
    @FXML
    public void startBtnClicked() {
        controller.getCarSimulation().startEngine();
    }
    
    @FXML
    public void stopBtnClicked() {
        controller.getCarSimulation().stopEngine();
    }
    
    @FXML
    public void chargeBtnClicked() {
        controller.getCarSimulation().chargeCar();
    }
    
    public void updateTextArea(Features features) {
        List<String> listStatus = getStatusLabels(features);
        List<String> listIndicator = getIndicatorLabels(features);
        List<String> listWear = getWearLabels(features);
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                label_11.setText(listStatus.get(0));
                label_12.setText(listStatus.get(1));
                label_13.setText(listIndicator.get(0));
                label_21.setText(listIndicator.get(1));
                label_22.setText(listWear.get(0));
                label_23.setText(listWear.get(1));
            }
            
        });
    }
    
    private List<String> getWearLabels(Features features){
        String str1 = "Battery Wear: ";
        String str2 = "Engine Wear: ";
        Optional<Feature> optWear = features.getFeature("wear-time");
        if(optWear.isPresent()) {
            JsonObject jsonObj = optWear.get().getProperties().get().toJson();
            Optional<JsonValue> optBatteryProp = jsonObj.getValue("battery-wear");
            Optional<JsonValue> optEngineProp = jsonObj.getValue("engine-wear");
            if(optBatteryProp.isPresent()) {
                str1 += optBatteryProp.get().asInt();
            }
            if(optEngineProp.isPresent()) {
                str2 += optEngineProp.get().asInt();
            }
        }
        LinkedList<String> list = new LinkedList<>();
        list.add(str1); list.add(str2);
        return list;
    }
    
    private List<String> getIndicatorLabels(Features features) {
        String str1 = "Battery Indicator: ";
        String str2 = "Engine Indicator: ";
        Optional<Feature> optIndicator = features.getFeature("indicator-light");
        if(optIndicator.isPresent()) {
            JsonObject jsonObj = optIndicator.get().getProperties().get().toJson();
            Optional<JsonValue> optEngineProp = jsonObj.getValue("engine-indicator");
            Optional<JsonValue> optBatteryProp = jsonObj.getValue("battery-indicator");
            if(optEngineProp.isPresent()) {
                if(optEngineProp.get().asBoolean()) { str1 += "On"; } else { str1 += "Off"; }
            }
            if(optBatteryProp.isPresent()) {
                if(optBatteryProp.get().asBoolean()) { str2 += "On"; } else { str2 += "Off"; }
            }
        }
        LinkedList<String> list = new LinkedList<>();
        list.add(str1); list.add(str2);
        return list;
    }

    private List<String> getStatusLabels(Features features) {
        String str1 = "Engine Status: ";
        String str2 = "Charge Status: ";
        Optional<Feature> optStatus = features.getFeature("status");
        if(optStatus.isPresent()) {
            JsonObject jsonObj = optStatus.get().getProperties().get().toJson();
            Optional<JsonValue> optEngineProp = jsonObj.getValue("engine");
            Optional<JsonValue> optChargeProp = jsonObj.getValue("charge-level");
            if(optEngineProp.isPresent()) {
                if(optEngineProp.get().asBoolean()) { str1 += "On"; } else { str1 += "Off"; }
            }
            if(optChargeProp.isPresent()) {
                str2 += (Math.round(optChargeProp.get().asDouble()*100.0)/100.0);
            }
        }
        
        LinkedList<String> list = new LinkedList<>();
        list.add(str1); list.add(str2);
        return list;
    }
    
    private void clearAllLabels() {
        label_11.setText("");
        label_12.setText("");
        label_13.setText("");
        label_21.setText("");
        label_22.setText("");
        label_23.setText("");
    }
    
    
}
