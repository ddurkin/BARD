package bard.core.rest.spring.experiment

import com.fasterxml.jackson.annotation.JsonProperty

public class ActivityConcentration extends ActivityData {
    @JsonProperty("concResponseSeries")
    private ConcentrationResponseSeries concentrationResponseSeries

    @JsonProperty("childElements")
    private List<ActivityData> childElements

    @JsonProperty("childElements")
    public List<ActivityData> getChildElements() {
        return childElements
    }

    @JsonProperty("childElements")
    public void setChildElements(List<ActivityData> childElements) {
        this.childElements = childElements
    }
    @JsonProperty("concResponseSeries")
    public ConcentrationResponseSeries getConcentrationResponseSeries() {
        return concentrationResponseSeries
    }
    @JsonProperty("concResponseSeries")
    public void setConcentrationResponseSeries(ConcentrationResponseSeries concentrationResponseSeries) {
        this.concentrationResponseSeries = concentrationResponseSeries
    }
    public boolean hasPlot(){
        if(this.concentrationResponseSeries|| this.primaryElements){
            return true
        }
        return false
    }
    public Double getSlope(){
        if(hasPlot()&&this.value){
            return new Double(this.value)
        }
        return null
    }
    public boolean hasChildElements(){
        if(this.childElements){
            return true
        }
        return false
    }
    public String toDisplay(){
        String display = ""
        if(this.qualifier && this.qualifier != "="){
            display = display + qualifier
        }
        if(this.value){
            return display +  " " + this.value
        }
        return ""
    }
}