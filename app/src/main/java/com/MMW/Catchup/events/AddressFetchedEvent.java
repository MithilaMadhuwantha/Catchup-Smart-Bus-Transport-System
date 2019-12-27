package com.MMW.Catchup.events;

import android.location.Address;

public class AddressFetchedEvent {
    public Address _address;
    public boolean isError = false;

    public AddressFetchedEvent(Address address){
        _address =address;
    }
}
