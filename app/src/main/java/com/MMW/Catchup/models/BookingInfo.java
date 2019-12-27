package com.MMW.Catchup.models;

public class BookingInfo {

    private String bookedUser;
    private Integer seatNo;
    private String bookedUserName;

    public String getBookedUser() {
        return bookedUser;
    }

    public void setBookedUser(String bookedUser) {
        this.bookedUser = bookedUser;
    }

    public Integer getSeatNo() {
        return seatNo;
    }

    public void setSeatNo(Integer seatNo) {
        this.seatNo = seatNo;
    }

    public String getBookedUserName() {
        return bookedUserName;
    }

    public void setBookedUserName(String bookedUserName) {
        this.bookedUserName = bookedUserName;
    }
}
