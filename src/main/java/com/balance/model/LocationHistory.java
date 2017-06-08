package com.balance.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by da_20 on 5/6/2017.
 */
@Entity
public class LocationHistory{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="location_history_id")
    private Long id;

    private Float latitude;
    private Float longitude;
    private Integer user;
    private Date date;

    public LocationHistory(Float latitude, Float longitude, Integer user, Date date) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.user = user;
        this.date = date;
    }

    public LocationHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Float getLatitude() {
        return latitude;
    }

    public void setLatitude(Float latitude) {
        this.latitude = latitude;
    }

    public Float getLongitude() {
        return longitude;
    }

    public void setLongitude(Float longitude) {
        this.longitude = longitude;
    }

    public Integer getUser() {
        return user;
    }

    public void setUser(Integer user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
