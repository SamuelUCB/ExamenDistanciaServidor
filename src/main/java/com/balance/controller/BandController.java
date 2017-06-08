package com.balance.controller;

import com.balance.model.*;
import com.balance.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.*;

/**
 * Created by da_20 on 31/5/2017.
 */
@RestController
public class BandController {

    private BandService bandService;
    private UserService userService;
    private CaloriesHistoryService caloriesHistoryService;
    private PulseHistoryService pulseHistoryService;
    private StepsHistoryService stepsHistoryService;
    private LocationHistoryService locationHistoryService;

    @Autowired
    public void setStepsHistoryService(StepsHistoryService stepsHistoryService) {
        this.stepsHistoryService = stepsHistoryService;
    }

    @Autowired
    public void setLocationHistoryService(LocationHistoryService locationHistoryService) {
        this.locationHistoryService = locationHistoryService;
    }

    @Autowired
    public void setPulseHistoryService(PulseHistoryService pulseHistoryService) {
        this.pulseHistoryService = pulseHistoryService;
    }

    @Autowired
    public void setCaloriesHistoryService(CaloriesHistoryService caloriesHistoryService) {
        this.caloriesHistoryService = caloriesHistoryService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setBandService(BandService bandService) {
        this.bandService = bandService;
    }

    @RequestMapping(value = "/band", method = RequestMethod.POST)
    public Band saveBand(@Valid Band band, BindingResult bindingResult, Model model) {
        //band.setFecha_registro(new Date());
        caloriesHistoryService.saveCaloriesHistory(new CaloriesHistory(band.getCalories(), band.getUser(), band.getFecha_registro()));
        pulseHistoryService.savePulseHistory(new PulseHistory(band.getBpm(), band.getFecha_registro(), band.getUser()));
        stepsHistoryService.saveStepsHistory(new StepsHistory(band.getSteps(), band.getDistance(), band.getUser(), band.getFecha_registro()));




        User user = userService.getUserById(band.getUser());

        LocationHistory guardar = new LocationHistory(band.getLatitude(), band.getLongitude(), band.getUser(), band.getFecha_registro());
        boolean puedoGuardar = true;
        Iterator<LocationHistory> iterator = locationHistoryService.listAllLocationHistory().iterator();
        ArrayList<LocationHistory> listaUsuario = new ArrayList<>();
        while(iterator.hasNext()){
            LocationHistory aux = iterator.next();
            if(aux.getUser().equals(user.getId())) {
                listaUsuario.add(aux);
            }
        }
        listaUsuario.add(guardar);
        Collections.sort(listaUsuario,(l1,l2) -> l1.getDate().compareTo(l2.getDate()));

        // x -> longitud
        // y -> latitud

        float resp = 0;
        for(int index = 0;index < listaUsuario.size() - 1; index++) {
            float valorActual = (float)Math.sqrt(Math.pow(listaUsuario.get(index).getLongitude() - listaUsuario.get(index+1).getLongitude(),2)
                    + Math.pow(listaUsuario.get(index).getLatitude() - listaUsuario.get(index+1).getLongitude(),2));

            if(valorActual > 1000) {
                puedoGuardar = false;
            }
        }

        if(puedoGuardar) {
            locationHistoryService.saveLocationHistory(guardar);
        }
        else {
            System.out.println("No se guardo una locacion");
        }

        bandService.saveBand(band);

        return band;
    }

    @RequestMapping(value = "/bands", method = RequestMethod.GET)
    public ResponseEntity<Iterable<Band>> getBands() {
        return new ResponseEntity(bandService.listAllBands(), HttpStatus.NOT_FOUND);
    }

}