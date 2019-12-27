package com.MMW.Catchup.events;

import com.MMW.Catchup.models.Directions;

public class DirectionsEvent {

    public Directions _directions;
    public boolean isError = false;

    public DirectionsEvent(Directions directions){
        _directions = directions;
    }
}
